package top.retain.nd.dto;

import lombok.Data;

@Data
public class GetUserFIleSpaceResp {

    private long used;
    private long total;
    private String spaceStr;
}
