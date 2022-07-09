package top.retain.nd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.common.ShareStatus;
import top.retain.nd.entity.Share;
import top.retain.nd.entity.User;
import top.retain.nd.service.IShareService;
import top.retain.nd.util.RedisUtils;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static top.retain.nd.service.impl.FileService.LINK_PREFIX;

/**
 * @author Retain
 * @date 2021/12/4 16:31
 */
@RestController
@RequestMapping("/share")
@Api("分享接口")
public class ShareController {

    @Resource
    RedisUtils redisUtils;

    // 外部分享接口

    @Resource
    private IShareService shareService;
    @GetMapping("/getShareDetail")
    @ApiOperation("获取分享链接信息")
    public CommonResult getShareDetail(@RequestParam("shareCode") String shareCode) throws IOException {
        return CommonResultTool.success(shareService.getShareDetail(shareCode));
    }

    @PostMapping("/transfer")
    @ApiOperation("转存文件")
    public CommonResult transfer(@RequestParam("shareId") String shareId,
                                 @RequestParam("savePath") String savePath,
                                 @ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);

        return CommonResultTool.success(shareService.transferFile(shareId, savePath, account));
    }

    @GetMapping("/exist")
    @ApiOperation("文件是否仍存在")
    public CommonResult exist(@RequestParam("shareId") String shareId) {
        return CommonResultTool.success(shareService.exist(shareId));
    }

    @PostMapping("/download")
    @ApiOperation("非下载，记录下载量")
    public CommonResult downloadCount(@RequestParam("shareId") String shareId) {
        return CommonResultTool.success(shareService.downloadCount(shareId));
    }


    // 分享人接口

    @GetMapping("/list")
    @ApiOperation("查询我的分享")
    public CommonResult list(@ApiIgnore HttpSession session) {
        String account = SpringSecurityUtil.currentUser(session);
        User user = shareService.getUser(account);
        QueryWrapper<Share> wrapper = new QueryWrapper<Share>();
        wrapper.eq("user_id", user.getId());
        List<Share> list = shareService.list(wrapper);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        for (Share share : list) {
            share.setStatusDesc(ShareStatus.getDesc(share.getStatus()));
            share.setCreateTimeStr(simpleDateFormat.format(share.getCreateTime()));
            share.setExpireTimeStr(simpleDateFormat.format(share.getExpireTime()));
            String shareCode = (String)redisUtils.get(share.getId());
            if (shareCode != null) {
                share.setShareUrl(LINK_PREFIX + shareCode);
            }else {
                share.setShareUrl("");
            }
            share.setUrl("");
        }
        return CommonResultTool.success(list);
    }

    @PostMapping("/delete")
    @ApiOperation("取消分享")
    public CommonResult delete(@RequestParam String id) {
        return CommonResultTool.success(shareService.deleteShare(id));
    }

    @PostMapping("/encodeUrl")
    @ApiOperation("设置文件提取码-url短链用")
    public CommonResult encodeByUrl(@RequestParam String code) {
        String shareId = (String) redisUtils.get(code);
        return CommonResultTool.success(shareService.encode(shareId));
    }
    @PostMapping("/encode")
    @ApiOperation("设置文件提取码")
    public CommonResult encodeById(@RequestParam String id) {
        return CommonResultTool.success(shareService.encode(id));
    }
    @GetMapping("/extract")
    @ApiOperation("提取文件")
    public CommonResult extract(@RequestParam String id, @RequestParam String code) {
        return CommonResultTool.success(shareService.extract(id, code));
    }

}
