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
        // ???????????????????????????,???????????????
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
            // ????????????????????????
            sendPasswordSms(phoneNumber, password);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        user = userService.selectByPhone(phoneNumber);
        user.setLastLoginTime(new Date());
        userService.updateById(user);
        // ???????????????????????????????????? new ????????????????????? authenticationResult ??????
        SmsCodeAuthenticationToken authenticationResult = new SmsCodeAuthenticationToken(userDetails, userDetails.getAuthorities());

        authenticationResult.setDetails(authenticationToken.getDetails());

        return authenticationResult;
    }

    public void checkSmsCode(String phoneNumber, String code){
        if (StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(code)) {
            throw new BadCredentialsException("?????????????????????????????????");
        }
        boolean matchResult = verifyCodeUtils.matchVerifyCode(phoneNumber, code);
        if(!matchResult) {
            throw new BadCredentialsException("???????????????");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // ?????? authentication ????????? SmsCodeAuthenticationToken ?????????????????????
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
                // ?????????????????????????????????
                TimeUnit.SECONDS.sleep(35);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (SmsUtil.sendSms(1176542, new String[]{phoneNumber}, new String[]{password})){
                log.info(phoneNumber + "?????????????????? "+password + ", ???????????????");
            }else {
            }
        }).start();

    }
}
