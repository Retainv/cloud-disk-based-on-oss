package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import top.retain.nd.entity.UserFile;

import java.util.List;

/**
 * @author Retain
 * @date 2021/11/6 19:37
 */
public interface IFileMapper extends BaseMapper<UserFile> {
    List<UserFile>  listFilePrefix(@Param("prefix") String prefix, @Param("userId") Long userId);
    List<UserFile> listRootFiles(@Param("userId") Long userId);
    UserFile getParentDirByPath(@Param("prefix") String prefix,@Param("userId") Long userId);
//    String selectParentId(@Param("name") String name, @Param("userId") Long userId);


    UserFile getFileByPath(@Param("userId") Long userId,@Param("ossPath")String ossPath);
    UserFile getDeletedFileByPath(@Param("userId") Long userId,@Param("ossPath")String ossPath);

    List<UserFile> getChildFilesById(@Param("userId") Long userId,@Param("parentId") String parentId);
}
