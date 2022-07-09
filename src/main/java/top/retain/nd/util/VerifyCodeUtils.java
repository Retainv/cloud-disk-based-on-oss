package top.retain.nd.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 验证码生成工具类
 *
 * @author Retain
 */
@Component
@Slf4j
public class VerifyCodeUtils {



    public static final String CODE_PREFIX="code:";

    @Autowired
    RedisUtils redisUtils;

    public static final int VERIFY_CODE_LEN = 6;

    public static String generate() {
        return String.valueOf((int) ((Math.random() * 9 + 1) * Math.pow(10, VERIFY_CODE_LEN - 1)));
    }

    /**
     * 验证验证码
     * @param phoneNumber
     * @param code
     * @return
     */
    public boolean matchVerifyCode(String phoneNumber,String code) {
        // 不存在验证码或验证码错误
        boolean match = redisUtils.hasKey(CODE_PREFIX + phoneNumber) && code.equals(redisUtils.get(CODE_PREFIX + phoneNumber));
        if (match) {
            redisUtils.delete(CODE_PREFIX + phoneNumber);
        }
        return match;
    }

    /**
     * 发送验证码
     * @param phoneNumber
     * @return
     */
    public boolean sendVerifyCode(String phoneNumber) {
        String code = generate();
        log.info("手机号："+phoneNumber+"验证码："+code);
        String[] params={code,"10"};
        // 存入缓存
        boolean set = redisUtils.set(CODE_PREFIX + phoneNumber, code);
        // 有效时间10分钟
        redisUtils.expire(CODE_PREFIX + phoneNumber,600L);
        if (set) {
            log.info("验证码成功存入缓存");
            if (SmsUtil.sendSms(879648, new String[]{phoneNumber}, params)){
                log.info("短信成功发送到 "+phoneNumber);
                return true;
            }
        }
        return false;
        // TODO: 2021/3/1 这里执行完毕后不会关闭连接，
    }
}
