package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.retain.nd.entity.Tag;

/**
 * @author Retain
 * @date 2021/11/11 9:38
 */
public interface ITagMapper extends BaseMapper<Tag> {
    Tag getTagById(@Param("id") String tagId, @Param("userId") Long userId);
}
