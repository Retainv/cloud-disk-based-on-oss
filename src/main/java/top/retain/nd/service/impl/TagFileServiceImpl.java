package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.retain.nd.entity.TagFile;
import top.retain.nd.mapper.ITagFileMapper;
import top.retain.nd.service.ITagFileService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Retain
 * @date 2021/11/17 18:20
 */
@Service
public class TagFileServiceImpl extends ServiceImpl<ITagFileMapper, TagFile> implements ITagFileService {

    @Resource
    private ITagFileMapper tagFileMapper;
    @Override
    public TagFile getTagFile(String fileId, String tagId) {
        if (StringUtils.isBlank(fileId)) {
            return null;
        }
        return this.getOne(new QueryWrapper<TagFile>().eq("file_id", fileId)
                .eq("tag_id", tagId));
    }

    @Override
    public List<TagFile> getTagFiles(String tagId) {
        return tagFileMapper.getTagFiles(tagId);
    }

}
