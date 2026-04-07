package info.akb21.ofd2pdf.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.akb21.ofd2pdf.model.BatchSummary;
import info.akb21.ofd2pdf.model.ConversionResult;
import info.akb21.ofd2pdf.model.ConversionTask;
import info.akb21.ofd2pdf.model.TaskStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ConversionManagerTest {
    @Test
    void shouldConvertTasksInParallel() throws Exception {
        AtomicInteger runningCount = new AtomicInteger();
        AtomicInteger maxRunningCount = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        ConversionManager manager = new ConversionManager((ofdPath, pdfPath, overwrite) -> {
            int currentRunning = runningCount.incrementAndGet();
            maxRunningCount.updateAndGet(previous -> Math.max(previous, currentRunning));
            started.countDown();
            try {
                assertTrue(started.await(1, TimeUnit.SECONDS));
                assertTrue(release.await(1, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return new ConversionResult(ofdPath, pdfPath, TaskStatus.FAILED, "被中断", 0);
            } finally {
                runningCount.decrementAndGet();
            }
            return new ConversionResult(ofdPath, pdfPath, TaskStatus.SUCCESS, "转换成功", 0);
        }, 2);

        ConversionTask firstTask = task("parallel-1");
        ConversionTask secondTask = task("parallel-2");

        Thread releaseThread = new Thread(() -> {
            try {
                assertTrue(started.await(1, TimeUnit.SECONDS));
                release.countDown();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
        releaseThread.start();

        BatchSummary summary = manager.convertAll(
            List.of(firstTask, secondTask),
            task -> true,
            (task, update) -> {
            }
        );

        releaseThread.join();

        assertEquals(2, summary.success());
        assertEquals(0, summary.failed());
        assertEquals(0, summary.skipped());
        assertEquals(2, maxRunningCount.get());
    }

    @Test
    void shouldSkipExistingTargetWhenOverwriteRejected() throws IOException {
        ConversionManager manager = new ConversionManager((ofdPath, pdfPath, overwrite) ->
            new ConversionResult(ofdPath, pdfPath, TaskStatus.SUCCESS, "转换成功", 0), 2
        );
        ConversionTask existingTargetTask = tempTask("existing-target");

        Files.createFile(existingTargetTask.getTargetPath());

        AtomicInteger updateCount = new AtomicInteger();
        BatchSummary summary = manager.convertAll(
            List.of(existingTargetTask),
            task -> false,
            (task, update) -> {
                task.setStatus(update.status());
                task.setMessage(update.message());
                updateCount.incrementAndGet();
            }
        );

        assertEquals(0, summary.success());
        assertEquals(0, summary.failed());
        assertEquals(1, summary.skipped());
        assertEquals(TaskStatus.SKIPPED, existingTargetTask.getTaskStatus());
        assertEquals(1, updateCount.get());
    }

    @Test
    void shouldReportFailuresWithoutBlockingOtherTasks() {
        ConversionManager manager = new ConversionManager((ofdPath, pdfPath, overwrite) -> {
            if (ofdPath.getFileName().toString().contains("fail")) {
                return new ConversionResult(ofdPath, pdfPath, TaskStatus.FAILED, "转换失败", 0);
            }
            return new ConversionResult(ofdPath, pdfPath, TaskStatus.SUCCESS, "转换成功", 0);
        }, 2);
        ConversionTask successTask = task("ok");
        ConversionTask failedTask = task("fail");

        BatchSummary summary = manager.convertAll(
            List.of(successTask, failedTask),
            task -> true,
            (task, update) -> {
                task.setStatus(update.status());
                task.setMessage(update.message());
            }
        );

        assertEquals(1, summary.success());
        assertEquals(1, summary.failed());
        assertEquals(0, summary.skipped());
        assertEquals(TaskStatus.SUCCESS, successTask.getTaskStatus());
        assertEquals(TaskStatus.FAILED, failedTask.getTaskStatus());
    }

    private static ConversionTask task(String name) {
        return new ConversionTask(
            Path.of("/tmp", name + ".ofd"),
            Path.of("/tmp", name + ".pdf")
        );
    }

    private static ConversionTask tempTask(String name) throws IOException {
        Path directory = Files.createTempDirectory("ofd2pdf-" + name);
        return new ConversionTask(
            Files.createFile(directory.resolve(name + ".ofd")),
            directory.resolve(name + ".pdf")
        );
    }
}
