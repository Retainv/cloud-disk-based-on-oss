package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.retain.nd.entity.UserCode;
import top.retain.nd.mapper.IUserCodeMapper;
import top.retain.nd.service.IUserCodeService;

@Service
public class UserCodeServiceImpl extends ServiceImpl<IUserCodeMapper, UserCode> implements IUserCodeService {
    @Override
    public UserCode getUserCode(Long userId, String code) {
        return this.getOne(new QueryWrapper<UserCode>()
                .eq("user_id", userId)
                .eq("code", code));
    }
}
