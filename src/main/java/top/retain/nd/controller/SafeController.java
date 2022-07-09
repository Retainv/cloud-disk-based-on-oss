package top.retain.nd.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.service.ISafeFileService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author Retain
 * @date 2021/11/22 12:44
 */
@RestController
@RequestMapping("/safe")
@Api("保险箱接口")
@Slf4j
public class SafeController {



    @Resource
    private ISafeFileService safeService;

    @GetMapping("/check")
    @ApiOperation("查询是否激活保险箱")
    public CommonResult check(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.check(account));
    }
    @GetMapping("/checkEntered")
    @ApiOperation("查询是否进入过保险箱")
    public CommonResult checkEntered(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.checkEntered(account));
    }
    @PostMapping("/activate")
    @ApiOperation("激活保险箱")
    public CommonResult activateSafe(@RequestParam String safeCode
            ,@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.activate(account,safeCode));
    }

    @PostMapping("/match")
    @ApiOperation("校验保险箱密钥")
    public CommonResult matchCode(@RequestBody Map<String,String> map
            ,@ApiIgnore HttpSession session) {
        String safeCode = map.get("safeCode");
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.matchCode(account,safeCode));
    }


    @PostMapping("/moveToSafe")
    @ApiOperation("移到保险箱")
    public CommonResult moveToSafe(@RequestParam("ossPath") String ossPath
            ,@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.moveToSafe(account,ossPath));
    }

    @PostMapping("/moveOutSafe")
    @ApiOperation("移出保险箱")
    public CommonResult moveOutSafe(@RequestParam("ossPath") String ossPath
            ,@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.moveOutSafe(account,ossPath));
    }

    @GetMapping("/list")
    @ApiOperation("列举保险箱文件")
    public CommonResult list(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.list(account));
    }

    @PostMapping("/lockSafe")
    @ApiOperation("锁定保险箱")
    public CommonResult lockSafe(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        return CommonResultTool.success(safeService.lockSafe(account));
    }
}
