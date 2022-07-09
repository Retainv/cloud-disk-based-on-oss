package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.retain.nd.entity.SafeFile;
import top.retain.nd.entity.UserFile;

import java.util.List;

public interface ISafeFileMapper extends BaseMapper<SafeFile> {

    List<UserFile> list(Long userId);
}