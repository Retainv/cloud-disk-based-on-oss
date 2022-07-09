package top.retain.nd.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.retain.nd.dto.STSResp;
import top.retain.nd.entity.User;
import top.retain.nd.exception.CodeWrongException;
import top.retain.nd.exception.UserExistException;
import top.retain.nd.mapper.IUserMapper;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.OSSUtils;
import top.retain.nd.util.RedisUtils;
import top.retain.nd.util.VerifyCodeUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Retain
 * @date 2021/9/29 18:47
 */
@Service("UserService")
@Slf4j
public class UserServiceImpl extends ServiceImpl<IUserMapper, User> implements UserDetailsService, IUserService  {

    public static final String STS_PREFIX = "sts:";
    @Resource
    IUserMapper userMapper;


    @Resource
    OSSUtils ossUtil;

    @Resource
    private RedisUtils redisUtils;
    @Resource
    VerifyCodeUtils verifyCodeUtils;

    @Override
    public User selectByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    @Override
    public String uploadAvator(MultipartFile multipartFile, Long userId) {
        String url = "";
        try {
            url = ossUtil.uploadAvator(multipartFile, userId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("上传头像失败！");
        }
        User user = new User();
        user.setId(userId);
        user.setAvator(url);
        boolean b = this.updateById(user);
        if (!b) {
            throw new RuntimeException("上传头像失败！");
        }
        return url;
    }

    @Override
    public boolean updatePhoneNumber(String phoneNumber, String code,Long userId) {
        boolean match = this.matchVerifyCode(phoneNumber, code);
        if (!match) {
            throw new CodeWrongException("验证码错误！");
        }
        User selectUser = this.selectByPhone(phoneNumber);
        if (Objects.nonNull(selectUser)) {
            throw new RuntimeException("该手机号已被注册！");
        }
        User user = new User();
        user.setId(userId);
        user.setPhoneNumber(phoneNumber);
        user.setAccount(phoneNumber);
        return this.updateById(user);
    }

    @Override
    public boolean updatePassword(String oldPassword, String newPassword, Long userId) {
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            throw new RuntimeException("内容不能为空！");
        }

        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户不存在！");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword,user.getPassword())) {
            throw new RuntimeException("旧密码不正确！");
        }
        user.setPassword(newPassword);
        encryptPassword(user);

        return this.updateById(user);
    }

    @Override
    public boolean forgetPassword(String phoneNumber, String code, String newPassword) {
        if (StringUtils.isBlank(phoneNumber) || StringUtils.isBlank(code)
        || StringUtils.isBlank(newPassword)) {
            throw new RuntimeException("内容不能为空！");
        }

        boolean match = matchVerifyCode(phoneNumber, code);
        if (!match) {
            throw new CodeWrongException("验证码错误！");
        }
        User user = getUserByPhone(phoneNumber);
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户不存在！");
        }
        user.setPassword(newPassword);
        encryptPassword(user);
        return this.updateById(user);
    }

    @Override
    public boolean forgetSafeCode(String newSafeCode, String code, Long userId) {
        User user = getById(userId);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户不存在！");
        }
        if (StringUtils.isBlank(newSafeCode) || StringUtils.isBlank(code)) {
            throw new IllegalArgumentException("内容不能为空！");
        }

        boolean match = matchVerifyCode(user.getPhoneNumber(), code);
        if (!match) {
            throw new CodeWrongException("验证码错误！");
        }
        user.setSafeCode(newSafeCode);
        return this.updateById(user);

    }
    private User getUser(String account) {
        return this.getOne(new QueryWrapper<User>().eq("account", account));
    }
    @Override
    public STSResp getSts(String account) {
        User user = getUser(account);
        String stsResp = (String) redisUtils.get(STS_PREFIX + user.getBucketName());
        if ( stsResp!= null) {
            return JSONUtil.toBean(stsResp,STSResp.class);
        }
        STSResp sts = ossUtil.generateSTS();
        sts.setRegion("oss-cn-chengdu");
        sts.setBucketName(user.getBucketName());
        redisUtils.set(STS_PREFIX + user.getBucketName(), JSONUtil.toJsonStr(sts), 43200L);
        return sts;
    }

    @Override
    public boolean extendUserSpace(Long userId, Long spaceSize) {
        User user = this.getById(userId);
        if (Objects.isNull(user)) {
            throw new RuntimeException("用户不存在！");
        }
        user.setTotalSpace(user.getTotalSpace() + spaceSize);
        return this.updateById(user);
    }

    public User getUserByPhone(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber)) {
            return null;
        }
        return this.getOne(new QueryWrapper<User>().eq("phone_number", phoneNumber).eq("deleted", false));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(User user) {
        User selectUser = userMapper.selectByPhone(user.getPhoneNumber());
        if (Objects.nonNull(selectUser)) {
            throw new UserExistException("用户手机号已注册！");
        }
        encryptPassword(user);
        int success = userMapper.insert(user);
        if (success == 0) {
            throw new RuntimeException("注册用户失败！");
        }
        String bucketName = ossUtil.createBucket(user.getId());
        log.info("Bucket创建成功：" + bucketName);
        user.setBucketName(bucketName);
        userMapper.updateById(user);
        log.info("用户注册成功！id：" + user.getId());
        return user.getId();

    }


    @Override
    public String getUserBucket(String account) {
        return userMapper.getUserBucket(account);
    }

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        if (account == null || "".equals(account)) {
            throw new UsernameNotFoundException("用户未找到");
        }
        //根据用户名查询用户
        User user = userMapper.selectByAccount(account);
        if (user == null) {
            throw new UsernameNotFoundException("账号未找到");
        }
        log.info("查询到当前登陆用户id<" + user.getAccount() + ">");
        return user;
    }
    private void encryptPassword(User userInfo){
        String password = userInfo.getPassword();
        password = new BCryptPasswordEncoder().encode(password);
        userInfo.setPassword(password);
    }


    @Override
    public Boolean sendVerifyCode(String phoneNumber) {

        return verifyCodeUtils.sendVerifyCode(phoneNumber);
    }

    public boolean matchVerifyCode(String phoneNumber, String code) {
        return verifyCodeUtils.matchVerifyCode(phoneNumber, code);
    }
}
