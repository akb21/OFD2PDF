package info.akb21.ofd2pdf.model;

public enum TaskStatus {
    PENDING("待处理"),
    RUNNING("转换中"),
    SUCCESS("成功"),
    FAILED("失败"),
    SKIPPED("已跳过");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
