package info.akb21.ofd2pdf.service;

import info.akb21.ofd2pdf.model.BatchSummary;
import info.akb21.ofd2pdf.model.ConversionResult;
import info.akb21.ofd2pdf.model.ConversionTask;
import info.akb21.ofd2pdf.model.TaskStatus;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ConversionManager {
    private final OfdConverterService converterService;

    public ConversionManager(OfdConverterService converterService) {
        this.converterService = converterService;
    }

    public BatchSummary convertAll(
            List<ConversionTask> tasks,
            Function<ConversionTask, Boolean> overwriteDecider,
            BiConsumer<ConversionTask, TaskUpdate> updateHandler
    ) {
        int success = 0;
        int failed = 0;
        int skipped = 0;

        for (ConversionTask task : tasks) {
            if (Files.exists(task.getTargetPath())) {
                boolean overwrite = overwriteDecider.apply(task);
                if (!overwrite) {
                    updateHandler.accept(task, new TaskUpdate(TaskStatus.SKIPPED, "目标文件已存在，已跳过"));
                    skipped++;
                    continue;
                }
                TaskStatus status = runTask(task, true, updateHandler);
                if (status == TaskStatus.SUCCESS) {
                    success++;
                } else if (status == TaskStatus.SKIPPED) {
                    skipped++;
                } else {
                    failed++;
                }
            } else {
                TaskStatus status = runTask(task, false, updateHandler);
                if (status == TaskStatus.SUCCESS) {
                    success++;
                } else if (status == TaskStatus.SKIPPED) {
                    skipped++;
                } else {
                    failed++;
                }
            }
        }

        return new BatchSummary(tasks.size(), success, failed, skipped);
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
}
