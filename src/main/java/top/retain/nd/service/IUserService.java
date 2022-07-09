package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import top.retain.nd.dto.STSResp;
import top.retain.nd.entity.User;

/**
 * @author Retain
 * @date 2021/9/29 18:43
 */
public interface IUserService extends IService<User> {

    Long register(User user);
    String getUserBucket(String account);
    Boolean sendVerifyCode(String phoneNumber);
    public User selectByPhone(String phone);

    String uploadAvator(MultipartFile multipartFile, Long userId);

    boolean updatePhoneNumber(String phoneNumber, String code, Long userId);

    boolean updatePassword(String oldPassword, String newPassword, Long userId);

    boolean forgetPassword(String phoneNumber, String code, String newPassword);

    boolean forgetSafeCode(String newSafeCode, String code, Long userId);

    STSResp getSts(String account);

    boolean extendUserSpace(Long userId,Long spaceSize);
}
