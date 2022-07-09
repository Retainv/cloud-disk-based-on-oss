package top.retain.nd.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.retain.nd.common.ShareStatus;
import top.retain.nd.entity.Share;
import top.retain.nd.entity.User;
import top.retain.nd.entity.UserFile;
import top.retain.nd.exception.FileHasExpiredOrNonExist;
import top.retain.nd.mapper.IShareMapper;
import top.retain.nd.service.IFileService;
import top.retain.nd.service.IShareService;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.OSSUtils;
import top.retain.nd.util.RedisUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import static top.retain.nd.service.impl.FileService.REDIS_SHARE_PREFIX;

/**
 * @author Retain
 * @date 2021/12/5 11:39
 */
@Service
@Slf4j
public class ShareServiceImpl extends ServiceImpl<IShareMapper, Share> implements IShareService {

    @Resource
    private IUserService userService;

    @Resource
    private RedisUtils redisUtil;

    @Resource
    private OSSUtils ossUtils;

    @Resource
    private IFileService fileService;

    @Override
    public Share getShareDetail(String shareCode) {
        String shareId = (String) redisUtil.get(shareCode);
        String url = (String) redisUtil.get(REDIS_SHARE_PREFIX + shareId);
        if (StringUtils.isEmpty(url)) {
            throw new FileHasExpiredOrNonExist("文件不存在");
        }
        log.info("正在访问分享文件：" + url);
        Share share = getById(shareId);
        // 浏览量+1
        int viewCount = share.getViewCount();
        share.setViewCount(++viewCount);
        this.updateById(share);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        share.setExpireTimeStr(format.format(share.getExpireTime()));
        // 过滤字段
        share.setFilePath("");
        share.setUserId(null);
        share.setCode("");
        return share;
    }

    @Override
    public boolean transferFile(String shareId, String savePath, String account) {
        Share share = getById(shareId);
        User srcUser = userService.getById(share.getUserId());
        if (Objects.isNull(srcUser)) {
            throw new RuntimeException("分享用户不存在");
        }
        String srcBucket = userService.getUserBucket(srcUser.getAccount());

        User destUser = getUser(account);
        if (Objects.isNull(destUser)) {
            throw new RuntimeException("当前用户不存在");
        }
        String destBucket = userService.getUserBucket(account);
        //  如果转存文件夹
        if ("dir".equals(share.getFileType())) {
            UserFile srcDir = fileService.getFileByPath(srcUser.getId(), share.getFilePath());
            if (Objects.isNull(srcDir)) {
                throw new RuntimeException("分享文件夹不存在");
            }
            // 转存文件夹内每个文件
            List<UserFile> dirFiles = fileService.getDirFiles(srcUser.getId(), srcDir);
            dirFiles.forEach(f -> {
                // 子文件夹不进行转存,自动生成
                if (!f.getIsDir()){
                    String destPath = savePath + f.getOssPath().substring(f.getOssPath().indexOf(share.getFileName()));
                    transferSingleFile(f.getName(), f.getOssPath(), destPath, srcBucket, destBucket, destUser, f.getSize());
                }
            });
        }else {
            transferSingleFile(share.getFileName(),share.getFilePath(), savePath + share.getFileName(),srcBucket, destBucket, destUser, share.getSize());
        }
        // 转存量+1
        int transferCount = share.getTransferCount();
        share.setTransferCount(++transferCount);
        return this.updateById(share);

    }
    public void transferSingleFile(String fileName,String srcPath, String destPath, String srcBucket, String destBucket, User destUser, Long size) {
//        String srcPath = share.getFilePath();
//        String destPath = savePath +  share.getFileName();

        // oss复制
        boolean copy = ossUtils.copySameRegionBucketFile(srcBucket, srcPath, destBucket, destPath);
        if (!copy) {
            throw new RuntimeException("转存文件失败");
        }

        // 上传数据库
        UserFile file = new UserFile();
        file.setOssPath(destPath);
        file.setName(fileName);

        fileService.saveUploadFile(destUser.getId(), file);
    }

    @Override
    public boolean exist(String shareId) {
        Share share = getById(shareId);
        User srcUser = userService.getById(share.getUserId());
        if (Objects.isNull(srcUser)) {
            throw new RuntimeException("分享用户不存在");
        }
        // 被放入回收站仍然可以查看分享
        UserFile file = fileService.getOne(new QueryWrapper<UserFile>().eq("user_id", srcUser.getId()).eq("oss_path", share.getFilePath()));
        // 被删除，不能查看分享
        if (Objects.isNull(file)) {
            // 设置分享状态
            share.setStatus(ShareStatus.DELETED.getCode());
            this.updateById(share);
            return false;
        }
        return true;
    }

    @Override
    public boolean downloadCount(String shareId) {
        Share share = getById(shareId);
        int downloadCount = share.getDownloadCount();
        share.setDownloadCount(++downloadCount);
        return this.updateById(share);
    }

    @Override
    public User getUser(String account) {
        return userService.getOne(new QueryWrapper<User>().eq("account", account));
    }

    @Override
    public boolean deleteShare(String id) {
        String key = REDIS_SHARE_PREFIX + id;
        Boolean delete = redisUtil.delete(key);
        if (delete) {
            return this.removeById(id);
        }
        return false;

    }

    @Override
    public boolean cancelShareByPath(Long userId, String ossPath) {
        QueryWrapper<Share> wrapper = new QueryWrapper<Share>().eq("user_id", userId).eq("file_path", ossPath);
        List<Share> list = this.list(wrapper);
        if (list.size() > 0) {
            for (Share share : list) {
                share.setStatus(ShareStatus.DELETED.getCode());
            }
            return updateBatchById(list);
        }
        return true;
    }

    @Override
    public String encode(String id) {
        Share share = this.getById(id);
        if (share == null) {
            throw new RuntimeException("分享不存在！");
        }
        share.setEncoded(true);
        String code = RandomUtil.randomString(4);
        share.setCode(code);

        if (!this.updateById(share)) {
            throw new RuntimeException("生成密钥失败！");
        }
        return code;
    }

    @Override
    public boolean extract(String id, String code) {
        Share share = this.getById(id);
        if (share == null) {
            throw new RuntimeException("分享不存在！");
        }
        if (code.equals(share.getCode())) {
            return true;
        }
        return false;
    }
}
