package top.retain.nd.dto;

import com.aliyuncs.auth.sts.AssumeRoleResponse;
import lombok.Data;

/**
 * @author Retain
 * @date 2021/12/18 18:09
 */
@Data
public class STSResp {
    private AssumeRoleResponse.Credentials credentials;

    private String region;

    private String bucketName;
}
