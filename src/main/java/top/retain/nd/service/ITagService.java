package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.entity.Tag;
import top.retain.nd.entity.UserFile;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

/**
 * @author Retain
 * @date 2021/11/11 9:40
 */
public interface ITagService  extends IService<Tag> {
    Tag getTagById( String tagId, Long userId);
    Tag getTagByName(String tagName, Long userId);
    int addTag(Tag tag, Long userId) throws FileAlreadyExistsException;

    int addFileToTag(String tagName, Long userId, String ossPath) throws FileNotFoundException;

    List<UserFile> listTagFiles(String tagName, Long userId);

    List<Tag> getAllTags(Long userId);

    boolean rename(Long userId, String tagId, String newName);

    boolean delete(Long userId, String tagId);

    boolean moveOutTag(String tagName, Long userId, String ossPath);
}
