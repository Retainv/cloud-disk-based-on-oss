package top.retain.nd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.entity.Tag;
import top.retain.nd.entity.User;
import top.retain.nd.service.ITagService;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;

/**
 * @author Retain
 * @date 2021/11/11 9:47
 */
@RestController
@RequestMapping("/tag")
@Api("标签类接口")
@Slf4j
public class TagController {

    @Resource
    private ITagService tagService;


    @Resource
    private IUserService userService;

    @PostMapping("/add")
    public CommonResult addTag(@ApiIgnore HttpSession session,
                               @RequestBody Tag tag) throws FileAlreadyExistsException {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.addTag(tag, user.getId()));
    }

    @PostMapping("/add/file")
    public CommonResult addTagFile(@ApiIgnore HttpSession session,
                                   @RequestParam String tagName,
                                   @RequestParam String ossPath) throws FileAlreadyExistsException, FileNotFoundException {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.addFileToTag(tagName,user.getId(), ossPath));
    }

    @GetMapping("/list")
    public CommonResult listTagFiles(@ApiIgnore HttpSession session,
                                     @RequestParam("tagName") String tagName) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.listTagFiles(tagName,user.getId()));
    }

    @GetMapping("/all")
    public CommonResult getAllTags(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.getAllTags(user.getId()));
    }

    @PostMapping("/rename")
    public CommonResult rename(@ApiIgnore HttpSession session
            ,@RequestParam("tagId") String tagId
            ,@RequestParam("newName") String newName) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.rename(user.getId(), tagId, newName));
    }

    @PostMapping("/delete")
    public CommonResult delete(@ApiIgnore HttpSession session
            ,@RequestParam("tagId") String tagId) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.delete(user.getId(), tagId));
    }

    @PostMapping("/moveOutTag")
    public CommonResult moveOutTag(@ApiIgnore HttpSession session,
                                   @RequestParam String tagName,
                                   @RequestParam String ossPath) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(tagService.moveOutTag(tagName,user.getId(), ossPath));
    }
}
