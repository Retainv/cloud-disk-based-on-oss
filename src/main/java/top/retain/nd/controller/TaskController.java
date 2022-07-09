package top.retain.nd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import top.retain.nd.common.CommonResult;
import top.retain.nd.common.CommonResultTool;
import top.retain.nd.entity.Task;
import top.retain.nd.entity.User;
import top.retain.nd.service.ITaskService;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.SpringSecurityUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Retain
 * @date 2021/11/24 9:04
 */
@RestController
@RequestMapping("/task")
@Api("任务类接口")
@Slf4j
public class TaskController {

    @Resource
    private IUserService userService;

    @Resource
    private ITaskService taskService;

    @PostMapping("/add")
    public CommonResult addTask(HttpServletRequest request,
                               @RequestParam Long userId,
                                @RequestBody Task task) {
        System.out.println(request);
        return CommonResultTool.success(taskService.addTask(task, userId));
    }

    @GetMapping("/list/finished")
    public CommonResult listFinished(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(taskService.getFinishedTasks(user.getId()));
    }
    @PostMapping("/update")
    public CommonResult update(@RequestParam Long userId,
                               @RequestBody Task task,
                               @RequestParam Boolean finished) {
        return CommonResultTool.success(taskService.updateTask(userId, task, finished));
    }
    @PostMapping("/clear")
    public CommonResult clear(@ApiIgnore HttpSession session) {
        Long account = Long.valueOf(SpringSecurityUtil.currentUser(session));
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        return CommonResultTool.success(taskService.clear(user.getId()));
    }
}
