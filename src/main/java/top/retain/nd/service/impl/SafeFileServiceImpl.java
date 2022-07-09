package top.retain.nd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.retain.nd.entity.SafeFile;
import top.retain.nd.entity.User;
import top.retain.nd.entity.UserFile;
import top.retain.nd.mapper.ISafeFileMapper;
import top.retain.nd.service.IFileService;
import top.retain.nd.service.ISafeFileService;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.RedisUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author Retain
 * @date 2021/11/22 12:42
 */
@Service
public class SafeFileServiceImpl extends ServiceImpl<ISafeFileMapper, SafeFile> implements ISafeFileService {

    public static final String SAFE_PERMIT = "SAFE-";
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private IUserService userService;

    @Resource
    private IFileService fileService;

    @Resource
    private ISafeFileMapper safeFileMapper;

    private User getUser(Long account) {
        return userService.getOne(new QueryWrapper<User>().eq("account", account));
    }

    @Override
    public boolean activate(Long account, String safeCode) {
        User user = getUser(account);
        user.setSafeCode(safeCode);
        user.setIsSafeOn(true);
        return userService.updateById(user);
    }

    @Override
    public boolean moveToSafe(Long account, String ossPath) {
        User user = getUser(account);
        if (!user.getIsSafeOn()) {
            throw new RuntimeException("保险箱还未激活！");
        }
        UserFile file = fileService.getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        if (file.getDeleted()) {
            throw new RuntimeException("文件夹已被移到回收站！");
        }
        file.setInSafe(true);
        fileService.updateById(file);
        SafeFile safeFile = new SafeFile();
        safeFile.setFileId(file.getId());
        safeFile.setUserId(user.getId());
        return this.save(safeFile);
    }

    @Override
    public boolean matchCode(Long account, String safeCode) {
        User user = getUser(account);

        if (StringUtils.isBlank(safeCode)) {
            throw new RuntimeException("密钥不能为空！");
        }
        boolean equals = safeCode.equals(user.getSafeCode());
        // 密钥正确，存入redis，后面10分钟不需要输入密码
        String key = SAFE_PERMIT + user.getPhoneNumber();
        if (equals && !redisUtils.hasKey(key)) {
            redisUtils.set(key,"1", 600L);
        }
        return equals;
    }

    @Override
    public List<UserFile> list(Long account) {
        User user = getUser(account);
        return safeFileMapper.list(user.getId());
    }

    @Override
    public boolean check(Long account) {
        User user = getUser(account);
        return user.getIsSafeOn();
    }

    @Override
    public boolean moveOutSafe(Long account, String ossPath) {
        User user = getUser(account);
        UserFile file = fileService.getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        if (file.getDeleted()) {
            throw new RuntimeException("文件夹已被移到回收站！");
        }
        file.setInSafe(false);
        this.remove(new QueryWrapper<SafeFile>().eq("user_id", user.getId()).eq("file_id", file.getId()));
        return fileService.updateById(file);

    }

    @Override
    public boolean checkEntered(Long account) {
        User user = getUser(account);
        if (redisUtils.hasKey(SAFE_PERMIT + user.getPhoneNumber())){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean lockSafe(Long account) {
        User user = getUser(account);
        String key = SAFE_PERMIT + user.getPhoneNumber();
        if (redisUtils.hasKey(key)){
            redisUtils.delete(key);
        }
        return true;
    }
}
