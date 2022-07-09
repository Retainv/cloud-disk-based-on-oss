package top.retain.nd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.dto.ForgetPasswordReq;
import top.retain.nd.dto.ForgetSafeCodeReq;
import top.retain.nd.dto.UpdateUserBasicInfoReq;
import top.retain.nd.entity.User;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Retain
 * @date 2021/9/29 18:42
 */
@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    IUserService userService;


    @GetMapping("sts")
    public CommonResult getSts(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        return CommonResultTool.success(userService.getSts(account));
    }
    @GetMapping("/currentUser")
    public CommonResult currentUser(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        user.setPassword("");
        user.setBucketName("");
        user.setSafeCode("");
        user.setIsSafeOn(null);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        user.setLastLoginTimeStr(simpleDateFormat.format(user.getLastLoginTime()));
        user.setCreateTimeStr(simpleDateFormat.format(user.getCreateTime()));
        return CommonResultTool.success(user);
    }
    @RequestMapping("/register")
    public CommonResult register(@RequestBody User user) {
        Long userId = userService.register(user);
        return CommonResultTool.success(userId);
    }

    @RequestMapping("/sms/sendCode")
    public CommonResult login(@RequestParam("phoneNumber") String phoneNumber
                              ) {
        return CommonResultTool.success(userService.sendVerifyCode(phoneNumber));
    }

    @PostMapping("/updatePhoneNumber")
    @ApiOperation("更换手机号")
    public CommonResult updatePhoneNumber(@RequestParam("phoneNumber") String phoneNumber,
                                          @RequestParam("code") String code,
                                          @RequestParam("userId") Long userId
    ) {
        return CommonResultTool.success(userService.updatePhoneNumber(phoneNumber,code, userId));
    }

    @PostMapping("/updatePassword")
    @ApiOperation("修改密码")
    public CommonResult updatePassword(@RequestParam("oldPassword") String oldPassword,
                                          @RequestParam("newPassword") String newPassword,
                                          @RequestParam("userId") Long userId
    ) {
        return CommonResultTool.success(userService.updatePassword(oldPassword,newPassword, userId));
    }

    @PostMapping("/forgetPassword")
    @ApiOperation("忘记登录密码")
    public CommonResult forgetPassword(@RequestBody ForgetPasswordReq req) {
        String phoneNumber = req.getPhoneNumber();
        String code = req.getVerifyCode();
        String newPassword = req.getNewPassword();
        return CommonResultTool.success(userService.forgetPassword(phoneNumber,code,newPassword));
    }

    @PostMapping("/forgetSafeCode")
    @ApiOperation("忘记保险箱密码")
    public CommonResult forgetSafeCode(@RequestBody ForgetSafeCodeReq req) {
        String newSafeCode = req.getNewSafeCode();
        String code = req.getVerifyCode();
        Long userId = req.getUserId();
        return CommonResultTool.success(userService.forgetSafeCode(newSafeCode,code,userId));
    }

    @ApiOperation("修改用户基本信息，防止接口暴露修改重要信息")
    @PostMapping("/update")
    public CommonResult update(@RequestBody UpdateUserBasicInfoReq req, @RequestParam("userId") Long userId) {
        if (Objects.isNull(req) || Objects.isNull(userId)) {
            throw new RuntimeException("内容不能为空！");
        }
        User user = new User();
        BeanUtils.copyProperties(req, user);
        user.setId(userId);
        return CommonResultTool.success(userService.updateById(user));
    }

    @ApiOperation("上传头像，返回url")
    @PostMapping("/avator")
    public CommonResult uploadAvator(@RequestParam MultipartFile file, @RequestParam("userId") Long userId) {
        if (Objects.isNull(file) || Objects.isNull(userId)) {
            throw new RuntimeException("内容不能为空！");
        }
        return CommonResultTool.success(userService.uploadAvator(file, userId));
    }
}
