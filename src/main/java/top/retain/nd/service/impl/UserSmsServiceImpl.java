package top.retain.nd.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.retain.nd.entity.User;
import top.retain.nd.mapper.IUserMapper;

import javax.annotation.Resource;

/**
 * @author Retain
 * @date 2021/10/19 16:22
 */
@Service("UserSmsServiceImpl")
@Slf4j
public class UserSmsServiceImpl implements UserDetailsService {

    @Resource
    IUserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        if (phoneNumber == null || "".equals(phoneNumber)) {
            throw new UsernameNotFoundException("用户未找到");
        }
        //根据用户名查询用户
        User user = userMapper.selectByPhone(phoneNumber);
        if (user == null) {
            throw new UsernameNotFoundException("账号未找到");
        }
        log.info("查询到当前登陆用户id<" + user.getAccount() + ">");
        return user;
    }
}
