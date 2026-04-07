package info.akb21.ofd2pdf.service;

import info.akb21.ofd2pdf.model.BatchSummary;
import info.akb21.ofd2pdf.model.ConversionResult;
import info.akb21.ofd2pdf.model.ConversionTask;
import info.akb21.ofd2pdf.model.TaskStatus;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConversionManager {
    private final OfdConverterService converterService;
    private final int parallelism;

    public ConversionManager(OfdConverterService converterService) {
        this(converterService, Math.max(1, Runtime.getRuntime().availableProcessors()));
    }

    public ConversionManager(OfdConverterService converterService, int parallelism) {
        this.converterService = converterService;
        this.parallelism = Math.max(1, parallelism);
    }

    public BatchSummary convertAll(
            List<ConversionTask> tasks,
            Function<ConversionTask, Boolean> overwriteDecider,
            BiConsumer<ConversionTask, TaskUpdate> updateHandler
    ) {
        List<PreparedTask> executableTasks = new ArrayList<>();
        int skipped = 0;

        for (ConversionTask task : tasks) {
            if (Files.exists(task.getTargetPath())) {
                boolean overwrite = overwriteDecider.apply(task);
                if (!overwrite) {
                    updateHandler.accept(task, new TaskUpdate(TaskStatus.SKIPPED, "目标文件已存在，已跳过"));
                    skipped++;
                    continue;
                }
                executableTasks.add(new PreparedTask(task, true));
            } else {
                executableTasks.add(new PreparedTask(task, false));
            }
        }

        BatchSummary executionSummary = executePreparedTasks(executableTasks, updateHandler);

        return new BatchSummary(
            tasks.size(),
            executionSummary.success(),
            executionSummary.failed(),
            skipped + executionSummary.skipped()
        );
    }

    private BatchSummary executePreparedTasks(
            List<PreparedTask> preparedTasks,
            BiConsumer<ConversionTask, TaskUpdate> updateHandler
    ) {
        if (preparedTasks.isEmpty()) {
            return new BatchSummary(0, 0, 0, 0);
        }

        ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(parallelism, preparedTasks.size())
        );
        try {
            List<Callable<TaskStatus>> callables = preparedTasks
                .stream()
                .<Callable<TaskStatus>>map(task ->
                    () -> runTask(task.task(), task.overwrite(), updateHandler)
                )
                .toList();

            List<Future<TaskStatus>> futures = executor.invokeAll(callables);
            int success = 0;
            int failed = 0;
            int skipped = 0;

            for (Future<TaskStatus> future : futures) {
                TaskStatus status = getTaskStatus(future);
                if (status == TaskStatus.SUCCESS) {
                    success++;
                } else if (status == TaskStatus.SKIPPED) {
                    skipped++;
                } else {
                    failed++;
                }
            }
            return new BatchSummary(preparedTasks.size(), success, failed, skipped);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("转换被中断", ex);
        } finally {
            executor.shutdownNow();
        }
    }

    private TaskStatus getTaskStatus(Future<TaskStatus> future) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("转换被中断", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException("转换执行失败", ex.getCause());
        }
    }

    private TaskStatus runTask(
            ConversionTask task,
            boolean overwrite,
            BiConsumer<ConversionTask, TaskUpdate> updateHandler
    ) {
        updateHandler.accept(task, new TaskUpdate(TaskStatus.RUNNING, "正在转换"));

        ConversionResult result = converterService.convert(task.getSourcePath(), task.getTargetPath(), overwrite);
        updateHandler.accept(task, new TaskUpdate(result.status(), result.message()));
        return result.status();
    }

    public record TaskUpdate(TaskStatus status, String message) {
    }

    private record PreparedTask(ConversionTask task, boolean overwrite) {
    }
}
