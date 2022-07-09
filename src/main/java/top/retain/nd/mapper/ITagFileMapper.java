package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import top.retain.nd.entity.TagFile;
import top.retain.nd.entity.UserFile;

import java.util.List;

public interface ITagFileMapper extends BaseMapper<TagFile> {

    List<UserFile> listTagFiles(String tagName,String tagId);
    List<TagFile> getTagFiles(String tagId);


}