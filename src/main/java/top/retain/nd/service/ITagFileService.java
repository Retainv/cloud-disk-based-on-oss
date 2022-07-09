package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.entity.TagFile;

import java.util.List;

/**
 * @author Retain
 * @date 2021/11/17 18:19
 */
public interface ITagFileService  extends IService<TagFile> {
//    int addFileToTag(Tag tag, Long userId, UserFile file);
    TagFile getTagFile(String fileId, String tagId);
    List<TagFile> getTagFiles(String tagId);
}
