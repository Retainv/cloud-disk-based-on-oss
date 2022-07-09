package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import top.retain.nd.dto.GetFinishedTasksResp;
import top.retain.nd.entity.Task;
import top.retain.nd.mapper.ITaskMapper;
import top.retain.nd.service.ITaskService;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Retain
 * @date 2021/11/24 9:03
 */
@Service
public class TaskServiceImpl extends ServiceImpl<ITaskMapper, Task> implements ITaskService {

    @Resource
    private ITaskMapper taskMapper;

    @Override
    public boolean addTask(Task task, Long userId) {
        if (StringUtils.isBlank(task.getFilePath())) {
            throw new RuntimeException("任务路径不能为空！");
        }
        Task dbTask = taskMapper.getLatestTask(userId, task);
        if (Objects.nonNull(dbTask) && !dbTask.getIsFinished()) {
            throw new RuntimeException("该任务已提交且还未执行完成！");
        }
        task.setIsCancelled(false);
        task.setUserId(userId);
        return this.save(task);

    }


//    public Task getTaskByFileAttr(Long userId,Task task) {
//
//        return this.getOne(new QueryWrapper<Task>()
//                .eq("user_id", userId)
//                .eq("file_path", task.getFilePath())
//                .eq("file_name", task.getFileName())
//                .eq("size", task.getSize())
//                .eq("is_finished", task.getIsFinished())
//                .eq("is_cancelled", task.getIsCancelled()));
//    }

    @Override
    public List<GetFinishedTasksResp> getFinishedTasks(Long userId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        List<Task> list = this.list(new QueryWrapper<Task>().eq("user_id", userId)
                .eq("is_finished", true)
                .eq("is_cancelled", false)
                .orderByDesc("create_time"));
        List<GetFinishedTasksResp> res = new ArrayList<GetFinishedTasksResp>();
        list.forEach(e -> {
            GetFinishedTasksResp resp = new GetFinishedTasksResp();
            BeanUtils.copyProperties(e,resp);
            resp.setCreateTimeStr(simpleDateFormat.format(e.getCreateTime()));
            res.add(resp);
        });
        return res;
    }

    @Override
    public boolean updateTask(Long userId, Task task,Boolean finished) {
        task.setUserId(userId);
        task.setIsCancelled(false);
        Task dbTask = taskMapper.getLatestTask(userId, task);
        if (Objects.isNull(dbTask)) {
            throw new RuntimeException("任务不存在！");
        }
        if (finished) {
            task.setIsFinished(true);
        }else {
            task.setIsCancelled(true);
        }
        task.setId(dbTask.getId());
        return this.updateById(task);
    }

    @Override
    public boolean clear(Long userId) {
        return this.remove(new QueryWrapper<Task>()
                .eq("user_id", userId)
                .eq("is_finished", true)
                .eq("is_cancelled",false));
    }
}
