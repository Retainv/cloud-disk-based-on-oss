package top.retain.nd.dto;

import lombok.Data;

/**
 * @author Retain
 * @date 2021/12/1 11:14
 */
@Data
public class ForgetSafeCodeReq {
    private String newSafeCode;
    private Long userId;
    private String verifyCode;
}
