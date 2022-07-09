package top.retain.nd.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author Retain
 * @date 2021/10/3 15:20
 */
@Data
public class ListFilesPagedResp {
    String key;
    long size;
    Date lastModified;
}
