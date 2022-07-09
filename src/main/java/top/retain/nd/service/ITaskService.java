package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.dto.GetFinishedTasksResp;
import top.retain.nd.entity.Task;

import java.util.List;

/**
 * @author Retain
 * @date 2021/11/24 9:02
 */
public interface ITaskService extends IService<Task> {
    boolean addTask(Task task, Long userId);
    List<GetFinishedTasksResp> getFinishedTasks(Long userId);

    boolean updateTask(Long userId, Task task, Boolean finished);

    boolean clear(Long userId);
}
