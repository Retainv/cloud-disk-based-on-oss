package top.retain.nd.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.StatusCode;
import top.retain.nd.entity.ExtendCode;
import top.retain.nd.service.IExtendCodeService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController
@Api("暗号接口")
@RequestMapping("/extendCode")
@Slf4j
public class ExtendCodeController {

    @Resource
    private IExtendCodeService extendCodeService;

    @PostMapping("/match")
    @ApiOperation("扩容暗号匹配")
    public CommonResult match(@ApiIgnore HttpSession session
            , @RequestParam() String code) {
        // 校验code是否为空
        if (code == null || code.trim().length() == 0) {
            return CommonResultTool.fail(StatusCode.CODE_WRONG,"暗号不能为空");
        }

        String account = SpringSecurityUtil.currentUser(session);
        return extendCodeService.match(account, code);
    }

    @PostMapping("/add")
    public CommonResult add(@RequestBody() ExtendCode code) {
        // 校验code是否为空
        if (code == null || code.getCode().length() == 0) {
            return CommonResultTool.fail(StatusCode.CODE_WRONG,"暗号不能为空");
        }
        return extendCodeService.add(code);
    }
}
