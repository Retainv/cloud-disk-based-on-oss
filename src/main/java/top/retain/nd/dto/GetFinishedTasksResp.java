package top.retain.nd.dto;

import lombok.Data;

/**
 * @author Retain
 * @date 2021/11/24 10:54
 */
@Data
public class GetFinishedTasksResp {
    private String id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件大小
     */
    private Double size;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件上传路径
     */
    private String filePath;

    /**
     * 是否已完成
     */
    private Boolean isFinished;

    private String createTimeStr;

    /**
     * 是否被取消
     */
    private Boolean isCancelled;

    private Long userId;
}
