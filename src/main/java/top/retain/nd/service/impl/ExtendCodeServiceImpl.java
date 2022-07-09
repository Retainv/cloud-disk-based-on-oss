package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.entity.ExtendCode;
import top.retain.nd.entity.User;
import top.retain.nd.entity.UserCode;
import top.retain.nd.mapper.IExtendCodeMapper;
import top.retain.nd.service.IExtendCodeService;
import top.retain.nd.service.IUserCodeService;
import top.retain.nd.service.IUserService;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class ExtendCodeServiceImpl extends ServiceImpl<IExtendCodeMapper, ExtendCode> implements IExtendCodeService {
    
    @Resource
    IUserService userService;
    
    @Resource
    IUserCodeService userCodeService;
    @Override
    @Transactional
    public CommonResult match(String account, String code) {
        User user = getUser(account);
        ExtendCode extendCode = getByCode(code);
        if (Objects.isNull(extendCode)) {
            return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "暗号不存在噢");
        }
        if (System.currentTimeMillis() > extendCode.getExpireTime().getTime()) {
            return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "暗号已经过期啦");
        }
        UserCode userCode = userCodeService.getUserCode(user.getId(), code);
        if (Objects.nonNull(userCode)) {
            return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "您已经使用过了该暗号了噢");
        }

        boolean b = userService.extendUserSpace(user.getId(), extendCode.getExtendSize());
        if (!b) {
            throw new RuntimeException("扩容失败");
        }
        UserCode newUserCode = new UserCode();
        newUserCode.setCode(code);
        newUserCode.setUserId(user.getId());

        boolean save = userCodeService.save(newUserCode);
        if (!save) {
            throw new RuntimeException("扩容失败");
        }
        return CommonResultTool.success("扩容成功！将会很快生效");
    }

    @Override
    public CommonResult add(ExtendCode code) {
        if (code.getExpireTime().getTime() < System.currentTimeMillis()) {
            return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "过期时间不能小于当前时间");
        }
        ExtendCode one = getByCode(code.getCode());
        if (Objects.nonNull(one)) {
            if (one.getExpireTime().getTime() > System.currentTimeMillis()) {
                return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "暗号已经存在了噢");
            }
        }
        boolean save = this.save(code);
        if (!save) {
            return CommonResultTool.fail(StatusCode.SYNTAX_ERROR, "暗号添加失败");
        }
        return CommonResultTool.success("暗号添加成功");
    }

    private User getUser(String account) {
        return userService.getOne(new QueryWrapper<User>().eq("account", account));
    }

    // 根据code获取暗号
    public ExtendCode getByCode(String code) {
        return this.getOne(new QueryWrapper<ExtendCode>().eq("code", code));
    }
}
