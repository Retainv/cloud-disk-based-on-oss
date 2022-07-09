package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.entity.SafeFile;
import top.retain.nd.entity.UserFile;

import java.util.List;

/**
 * @author Retain
 * @date 2021/11/22 12:41
 */
public interface ISafeFileService extends IService<SafeFile> {
    boolean activate(Long account, String safeCode);

    boolean moveToSafe(Long account, String ossPath);

    boolean matchCode(Long account, String safeCode);

    List<UserFile> list(Long account);

    boolean check(Long account);

    boolean moveOutSafe(Long account, String ossPath);

    boolean checkEntered(Long account);

    boolean lockSafe(Long account);
}
