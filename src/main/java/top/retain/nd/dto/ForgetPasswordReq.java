package top.retain.nd.dto;

import lombok.Data;

/**
 * @author Retain
 * @date 2021/12/1 10:38
 */
@Data
public class ForgetPasswordReq {
    private String phoneNumber;

    private String verifyCode;

    private String newPassword;
}
