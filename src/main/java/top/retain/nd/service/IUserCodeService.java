package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.entity.UserCode;

public interface IUserCodeService extends IService<UserCode> {

    UserCode getUserCode(Long userId,String code);
}
