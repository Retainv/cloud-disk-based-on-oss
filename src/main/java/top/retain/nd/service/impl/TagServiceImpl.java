package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.retain.nd.entity.Tag;
import top.retain.nd.entity.TagFile;
import top.retain.nd.entity.UserFile;
import top.retain.nd.exception.TagFileAlreadyExistException;
import top.retain.nd.exception.TagNotExistException;
import top.retain.nd.mapper.ITagFileMapper;
import top.retain.nd.mapper.ITagMapper;
import top.retain.nd.service.IFileService;
import top.retain.nd.service.ITagFileService;
import top.retain.nd.service.ITagService;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * @author Retain
 * @date 2021/11/11 9:40
 */
@Service
public class TagServiceImpl extends ServiceImpl<ITagMapper, Tag> implements ITagService {

    @Resource
    ITagFileService tagFileService;
    @Resource ITagMapper tagMapper;


    @Resource
    IFileService fileService;

    @Resource
    ITagFileMapper tagFileMapper;
    @Override
    public Tag getTagById(String tagId, Long userId) {
        return tagMapper.getTagById(tagId, userId);
    }

    @Override
    public Tag getTagByName(String tagName, Long userId) {
        if (StringUtils.isEmpty(tagName)) {
            return null;
        }
        return tagMapper.selectOne(new QueryWrapper<Tag>().eq("name", tagName).eq("user_id", userId));
    }

    @Override
    public int addTag(Tag tag, Long userId) throws FileAlreadyExistsException {
        Tag tag1 = getTagByName(tag.getName(), userId);
        if (Objects.nonNull(tag1)) {
            throw new FileAlreadyExistsException("标签已存在");
        }
        tag.setUserId(userId);
        return tagMapper.insert(tag);
    }
    @Override
    public int addFileToTag(String tagName, Long userId, String ossPath) throws FileNotFoundException {
        UserFile file = fileService.getFileByPath(userId, ossPath);

        if (Objects.isNull(file)) {
            throw new FileNotFoundException("文件不存在!");
        }
        Tag tag = getTagByName(tagName, userId);
        if (Objects.isNull(tag)) {
            throw new TagNotExistException("标签不存在!");
        }
        TagFile tagFile1 = tagFileService.getTagFile(file.getId(), tag.getId());
        if (Objects.nonNull(tagFile1)) {
            throw new TagFileAlreadyExistException("该标签下已存在该文件！");
        }
        TagFile tagFile = new TagFile();
        tagFile.setFileId(file.getId());
        tagFile.setTagId(tag.getId());
        return tagFileMapper.insert(tagFile);
    }

    @Override
    public List<UserFile> listTagFiles(String tagName, Long userId) {
        Tag tag = getTagByName(tagName, userId);
        if (Objects.isNull(tag)) {
            throw new TagNotExistException("标签不存在!");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        List<UserFile> userFiles = tagFileMapper.listTagFiles(tagName, tag.getId());
        userFiles.forEach(f -> f.setLastModified(simpleDateFormat.format(f.getUpdateTime())));
        fileService.sortWithDir(userFiles);
        return userFiles;
    }

    @Override
    public List<Tag> getAllTags(Long userId) {
        return this.list(new QueryWrapper<Tag>().eq("user_id", userId));
    }

    @Override
    public boolean rename(Long userId, String tagId, String newName) {
        Tag tag = getTagById(tagId, userId);
        if (Objects.isNull(tag)) {
            throw new TagNotExistException("标签不存在!");
        }
        Tag tagByName = getTagByName(newName, userId);
        if (Objects.nonNull(tagByName)) {
            throw new TagFileAlreadyExistException("标签已存在，请重新输入!");
        }
        tag.setName(newName);
        return updateById(tag);
    }

    @Override
    public boolean delete(Long userId, String tagId) {
        Tag tag = getTagById(tagId, userId);
        if (Objects.isNull(tag)) {
            throw new TagNotExistException("标签不存在!");
        }
        List<TagFile> tagFiles = tagFileService.getTagFiles(tag.getId());
        if (!CollectionUtils.isEmpty(tagFiles)) {
            List<String> ids = tagFiles.stream().map(TagFile::getId).collect(Collectors.toList());
            boolean tagFile = tagFileService.removeByIds(ids);
            if (!tagFile) {
                throw new RuntimeException("删除标签文件失败，请稍后再试");
            }
        }

        return this.removeById(tag.getId());
    }

    @Override
    public boolean moveOutTag(String tagName, Long userId, String ossPath) {
        Tag tag = this.getTagByName(tagName, userId);
        if (Objects.isNull(tag)) {
            throw new TagNotExistException("标签不存在!");
        }

        UserFile file = fileService.getFileByPath(userId, ossPath);
        if (Objects.isNull(file) || file.getDeleted()) {
            throw new RuntimeException("文件不存在！");
        }
        TagFile tagFile = tagFileService.getTagFile(file.getId(), tag.getId());
        if (Objects.isNull(tagFile)) {
            throw new RuntimeException("该收藏夹下不存在该文件!");
        }
        return tagFileService.removeById(tagFile.getId());
    }


//    public List<Tag> getAllTags(Long userId) {
//
//    }
}
