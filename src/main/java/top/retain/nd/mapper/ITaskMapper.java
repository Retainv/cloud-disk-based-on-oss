package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.retain.nd.entity.Task;

public interface ITaskMapper extends BaseMapper<Task> {

    Task getLatestTask(Long userId,Task task);

}