package top.retain.nd.common;


public enum StatusCode {
    //成功
    SUCCESS(200, "请求成功"),
    //客户端
    SYNTAX_ERROR(400, "客户端请求语法错误"),
    UN_AUTHORIZED(401, "用户未认证"),
    NO_PERMISSION(403, "权限不足"),
    NOT_FOUND(404, "未找到相关资源"),
    METHOD_ERROR(405, "请求方法错误"),
    UNKNOWN_USERNAME(410, "用户名未找到"),
    WRONG_USERNAME_OR_PASSWORD(411, "用户名或密码错误"),
    CODE_WRONG(411, "验证码错误"),
    USER_DISABLED(412, "用户已被禁用"),
    USER_LOCKED(413, "用户已被锁定"),
    NO_ACTION_JOIN(414, "用户未参与任何行动"),
    USER_NOT_BINDING(415, "用户未绑定"),
    VERIFY_CODE_ERROR(416, "验证码错误"),
    MISSING_PARAM(417, "参数不足"),
    UPLOAD_FAIL(445, "上传失败"),
    FILE_EXSIT(422, "文件夹已存在"),
    //服务端错误
    INTERNAL_SERVER_ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;
    private final Class<? extends Exception> exceptionClass;

    StatusCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
        this.exceptionClass = Exception.class;
    }

    StatusCode(int code, String msg, Class<? extends Exception> exceptionClass) {
        this.code = code;
        this.msg = msg;
        this.exceptionClass = exceptionClass;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }
}
