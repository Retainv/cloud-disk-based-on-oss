package top.retain.nd.dto;

import lombok.Data;

/**
 * @author Retain
 * @date 2021/11/22 19:15
 */
@Data
public class GetFileDetailResp {
    private String name;
    private Long size;
    private String type;
    private String createTimeStr;
    private String ossPath;
    private String lastModified;
}
