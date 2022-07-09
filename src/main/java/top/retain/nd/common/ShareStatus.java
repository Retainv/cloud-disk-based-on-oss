package top.retain.nd.common;

public enum ShareStatus {

    /**
     * 分享状态
     */
    NORMAL(1, "分享有效"),
    DESABLED(2, "分享已过期"),
    CANCELLED(3, "分享被取消"),
    DELETED(4, "原文件被删除,分享失效");


    private final int status;
    private final String desc;
    private final Class<? extends Exception> exceptionClass;



    public static String getDesc(int status) {
        ShareStatus[] values = values();
        for (ShareStatus value : values) {
            if (value.getCode() == status) {
                return value.getMsg();
            }
        }
        return null;
    }
    ShareStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
        this.exceptionClass = Exception.class;
    }

    ShareStatus(int status, String desc, Class<? extends Exception> exceptionClass) {
        this.status = status;
        this.desc = desc;
        this.exceptionClass = exceptionClass;
    }

    public int getCode() {
        return status;
    }

    public String getMsg() {
        return desc;
    }

    public Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }
}
