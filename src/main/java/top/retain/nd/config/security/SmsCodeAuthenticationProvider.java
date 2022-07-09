package top.retain.nd.config.security;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.retain.nd.entity.User;
import top.retain.nd.service.IUserService;
import top.retain.nd.service.impl.UserSmsServiceImpl;
import top.retain.nd.util.SmsUtil;
import top.retain.nd.util.VerifyCodeUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Retain
 * @date 2021/10/19 15:44
 */
@Component
@Slf4j
public class SmsCodeAuthenticationProvider implements AuthenticationProvider {
    private UserDetailsService userDetailsService;
    @Resource
    UserSmsServiceImpl userSmsService;

    @Resource
    IUserService userService;
    private final VerifyCodeUtils verifyCodeUtils;

    @Value("${user.init-space}")
    private Long initSpace;

    private final SmsUtil smsUtil;

    public SmsCodeAuthenticationProvider(VerifyCodeUtils verifyCodeUtils, SmsUtil smsUtil) {
        this.verifyCodeUtils = verifyCodeUtils;
        this.smsUtil = smsUtil;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsCodeAuthenticationToken authenticationToken = (SmsCodeAuthenticationToken) authentication;

        String phoneNumber = (String) authenticationToken.getPrincipal();
        String code = (String) authenticationToken.getCredentials();

        checkSmsCode(phoneNumber, code);
        // 根据手机号查询用户,没有则新建
        User user;
        if (Objects.isNull(userService.selectByPhone(phoneNumber))) {
            user = new User();
            user.setName(RandomUtil.randomString(8));
            user.setAccount(phoneNumber);
            String password = RandomUtil.randomString(6);
            user.setPassword(password);
            user.setPhoneNumber(phoneNumber);
            user.setTotalSpace(initSpace);
            userService.register(user);
            // 发送初始密码短信
            sendPasswordSms(phoneNumber, password);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        user = userService.selectByPhone(phoneNumber);
        user.setLastLoginTime(new Date());
        userService.updateById(user);
        // 此时鉴权成功后，应当重新 new 一个拥有鉴权的 authenticationResult 返回
        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(userDetails, userDetails.getAuthorities());

        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    public void checkSmsCode(String phoneNumber, String code){
        if (StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(code)) {
            throw new BadCredentialsException("手机号或验证码不能为空");
        }
        boolean matchResult = verifyCodeUtils.matchVerifyCode(phoneNumber, code);
        if(!matchResult) {
            throw new BadCredentialsException("验证码错误");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 判断 authentication 是不是 SmsCodeAuthenticationToken 的子类或子接口
        return SmsCodeAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private void sendPasswordSms(String phoneNumber, String password) {
        new Thread(() -> {
            try {
                // 防止发送太频繁丢失短信
                TimeUnit.SECONDS.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (SmsUtil.sendSms(1176542, new String[]{phoneNumber}, new String[]{password})){
                log.info(phoneNumber + "初始密码为： "+password + ", 已发送短信");
            }else {
            }
        }).start();

    }
}
