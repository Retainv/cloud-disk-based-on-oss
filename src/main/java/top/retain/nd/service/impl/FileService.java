package top.retain.nd.service.impl;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.SimplifiedObjectMeta;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.retain.nd.common.ShareStatus;
import top.retain.nd.dto.DownloadDirResp;
import top.retain.nd.dto.GetFileDetailResp;
import top.retain.nd.dto.GetUserFIleSpaceResp;
import top.retain.nd.dto.ListFilesPagedResp;
import top.retain.nd.entity.Share;
import top.retain.nd.entity.User;
import top.retain.nd.entity.UserFile;
import top.retain.nd.mapper.IFileMapper;
import top.retain.nd.service.IFileService;
import top.retain.nd.service.IShareService;
import top.retain.nd.service.IUserService;
import top.retain.nd.util.FileUtils;
import top.retain.nd.util.OSSUtils;
import top.retain.nd.util.QRCodeUtils;
import top.retain.nd.util.RedisUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.FileAlreadyExistsException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Retain
 * @date 2021/9/30 10:04
 */
@Service
@Slf4j
public class FileService extends ServiceImpl<IFileMapper, UserFile> implements IFileService {


//    public static final String LINK_PREFIX = "https://www.retainblog.top:8090/link?shareId=";
    public static final String LINK_PREFIX = "https://www.retainblog.top:8090/s/";
    public static final String REDIS_SHARE_PREFIX = "share:";

    @Resource
    private OSSUtils ossUtil;

    @Resource
    private IUserService userService;

    @Resource
    private IFileMapper fileMapper;

    @Resource
    private IShareService shareService;

    @Resource QRCodeUtils qrCodeUtils;

    @Resource
    private RedisUtils redisUtils;

    private ThreadPoolExecutor threadPool;

    private final AtomicInteger submittedCount = new AtomicInteger(0);

    @Override
    public List<ListFilesPagedResp> listFilesPaged(String account, Integer currentPage, Integer maxKeys) {
        String userBucket = userService.getUserBucket(account);
        List<OSSObjectSummary> summaries = ossUtil.listFilesPaged(userBucket,currentPage, maxKeys);
        ArrayList<ListFilesPagedResp> listFilesPagedResps = new ArrayList<>();
        for (OSSObjectSummary sum : summaries) {
            ListFilesPagedResp res = new ListFilesPagedResp();
            BeanUtils.copyProperties(sum, res);
            listFilesPagedResps.add(res);
        }
        return listFilesPagedResps;
    }

    public Boolean deleteSingleFile(String account, String filePath) {
        User user = getUser(account);
        if (StringUtils.isEmpty(filePath)) {
            throw new RuntimeException("文件路径错误！");
        }
        UserFile file = fileMapper.getDeletedFileByPath(user.getId(), filePath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        this.deleteFile(account, filePath);

        // 更新有分享文件的状态
        boolean b = shareService.cancelShareByPath(user.getId(), filePath);
        log.info("路径为：" + filePath + "的分享链接状态修改结果：" + b);
        return this.removeById(file.getId());
    }


    @Override
    public Boolean deleteFile(String account, String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            throw new RuntimeException("文件路径错误！");
        }
        String bucketName = userService.getUserBucket(account);

        // 更新有分享文件的状态为已删除文件
        User user = getUser(account);
        boolean b = shareService.cancelShareByPath(user.getId(), filePath);
        log.info("路径为：" + filePath + "的分享链接状态修改结果：" + b);
        return ossUtil.deleteFile(bucketName, filePath);
    }

    /**
     * @param account
     * @param filePath
     * @return
     */
    @Override
    public Boolean deleteDir(String account, String filePath) {
        User user = getUser(account);
        UserFile dirFile = fileMapper.getDeletedFileByPath(user.getId(), filePath);
        List<UserFile> dirFiles = getDirFiles(user.getId(), dirFile);
        // 如果是多级的空文件夹
        boolean emptyDir = true;
        if (CollectionUtils.isEmpty(dirFiles)) {
            emptyDir = false;
        }else {
            for (UserFile file : dirFiles) {
                if (!file.getIsDir()) {
                    // 更新有分享文件的状态为已删除文件
                    boolean b = shareService.cancelShareByPath(user.getId(), file.getOssPath());
                    log.info("路径为：" + file.getOssPath() + "的分享链接状态修改结果：" + b);

                    emptyDir = false;
                }
            }
        }
        List<String> ids;
        if (!CollectionUtils.isEmpty(dirFiles)) {
            ids= dirFiles.stream().map(UserFile::getId).collect(Collectors.toList());
        }else {
            // 如果空文件夹
            ids = new ArrayList<>();
        }
        ids.add(dirFile.getId());
        String bucketName = userService.getUserBucket(account);
        log.info("正在删除" + filePath + ",空文件夹:" + emptyDir);

        boolean b = this.removeByIds(ids);
        if (!b) {
            throw new RuntimeException("删除文件夹失败！");
        }
        if (!emptyDir) {
            try {
                ossUtil.deleteDir(bucketName, filePath);
            }catch (Exception e) {
                throw new RuntimeException("删除文件夹失败！");
            }
        }

        return true;

    }

    public Boolean download(String account, String localFilePath, String objectPath) throws Throwable {
        String bucketName = userService.getUserBucket(account);
        return ossUtil.resumeDownload(bucketName, localFilePath, objectPath);
    }

    @Override
    public void streamDownload(String account, String objectPath, HttpServletResponse response) {
        String bucketName = userService.getUserBucket(account);
        ossUtil.streamDownload(bucketName, objectPath, response);
    }

    @Override
    public String browserDownload(String account, String objectPath) {
        String bucketName = userService.getUserBucket(account);
        return ossUtil.generateUrl(bucketName, objectPath, new Date(System.currentTimeMillis() + 3600L * 1000 * 24));
    }

//    public Boolean uploadDir(String account,String localFilePath, String prefix) {
//        String bucketName = userService.getUserBucket(account);
//        try {
//            return ossUtil.resumeUpload(bucketName, localFilePath, prefix);
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        return true;
//    }

    @Override
    @Deprecated
    public List<OSSObjectSummary> listFilesByPrefixPaged(String account, Integer currentPage, Integer size, String prefix) {
        String bucketName = userService.getUserBucket(account);
        return ossUtil.listFilesByPrefixPaged(bucketName, currentPage, size, prefix);
    }

    /**
     *  sychronized 防止上传文件夹时同时查询不存在父目录而创建多个相同目录导致异常
     */
    @Override
    public Boolean saveUploadFile(Long userId, UserFile file) {
        boolean space = checkUserSpace(userId);
        if (!space) {
            throw new RuntimeException("用户空间容量不足！");
        }
        synchronized (FileService.class){
            file.setUserId(userId);
            String ossPath = file.getOssPath();
            Boolean dir = FileUtils.hasDir(ossPath);
            UserFile fileByPath = getFileByPath(userId, ossPath);
            if (Objects.nonNull(fileByPath)) {
                file.setId(fileByPath.getId());
            }
            // 实际文件
            file.setIsDir(false);
            // 第一级目录
            String parentId = "-1";
            if (dir) {
                String[] seperatePath = FileUtils.seperate(ossPath);
                // 创建根目录下的文件夹或文件
                String tmpPath = seperatePath[0] +"/";
                UserFile parentDir = this.getOne(new QueryWrapper<UserFile>().eq("oss_path", tmpPath).eq("user_id", userId));
                if (Objects.isNull(parentDir)) {
                    parentDir = createParentDir(seperatePath[0], "-1", userId, tmpPath);
                }
                parentId = parentDir.getId();

                for (int i = 1; i < seperatePath.length - 1; i++) {
                    tmpPath += seperatePath[i] + "/";
                    UserFile currentPath = this.getOne(new QueryWrapper<UserFile>().eq("oss_path", tmpPath).eq("user_id", userId));
                    // 路径文件夹不存在
                    if (Objects.isNull(currentPath)) {
                        // 存储每级的路径名称
                        UserFile path =createParentDir(seperatePath[i], parentId, userId, tmpPath);
                        parentId = path.getId();
                    }else {
                        parentId = currentPath.getId();
                    }
                }
            }
            String type = FileUtil.getSuffix(file.getName());
            file.setType(type);
            file.setParentId(parentId);
            return this.saveOrUpdate(file);
        }

    }


    /**
     * 查询用户空间容量是否超过限制
     * @return
     */
    public boolean checkUserSpace(Long userId) {
        User user = userService.getById(userId);
        Long userUsedSpace = ossUtil.getUserUsedSpace(user.getBucketName());
        return userUsedSpace < user.getTotalSpace();
    }
    @Override
    public List<UserFile> listFilesByPrefix(String account, String prefix) {
        User user = userService.getOne(new QueryWrapper<User>().eq("account", account));
        // 为空列举根目录文件
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        if (StringUtils.isEmpty(prefix)) {
            List<UserFile> userFiles = fileMapper.listRootFiles(user.getId());
            sortWithDir(userFiles);
            userFiles.forEach(f -> f.setLastModified(simpleDateFormat.format(f.getUpdateTime())));
            return userFiles;
        }else {
            List<UserFile> userFiles = fileMapper.listFilePrefix(prefix, user.getId());
            sortWithDir(userFiles);
            userFiles.forEach(f -> f.setLastModified(simpleDateFormat.format(f.getUpdateTime())));
            return userFiles;
        }
    }
    private UserFile createParentDir(String name, String parentId, Long userId, String tmpPath) {
        UserFile dir = getFileByPath(userId, tmpPath);
        UserFile parentDir = new UserFile();
        if (Objects.nonNull(dir)) {
            parentDir.setId(dir.getId());
        }
        parentDir.setName(name);
        parentDir.setIsDir(true);
        parentDir.setParentId(parentId);
        parentDir.setType("dir");
        parentDir.setUserId(userId);
        parentDir.setOssPath(tmpPath);
        this.saveOrUpdate(parentDir);
        return parentDir;
    }

    /**
     * 文件夹显示在前面
     */
    @Override
    public void sortWithDir(List<UserFile> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.sort((o1, o2) -> {
            int i1 = o1.getIsDir() ? 1 : 0;
            int i2 = o2.getIsDir() ? 1 : 0;
            return i2 - i1;
        });
    }

    @Override
    public GetFileDetailResp detail(String account, String ossPath) {
        User user = getUser(account);
        UserFile file = getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        file.setLastModified(simpleDateFormat.format(file.getUpdateTime()));
        GetFileDetailResp resp = new GetFileDetailResp();
        BeanUtils.copyProperties(file, resp);
        resp.setCreateTimeStr(simpleDateFormat.format(file.getCreateTime()));
        resp.setType(file.getIsDir() ? "文件夹" : file.getType());
        if(file.getIsDir()) {
            Long dirSize = calculateDirSize(user.getId(), file);
            resp.setSize(dirSize);
        }else{
            String bucketName = userService.getUserBucket(account);
            SimplifiedObjectMeta ossDetail = ossUtil.getDetail(bucketName, ossPath);
            resp.setSize(ossDetail.getSize());
        }
        return resp;
    }


    public Long calculateDirSize(Long userId, UserFile dirFile) {
        List<UserFile> dirFiles = getDirFiles(userId, dirFile);
        if (CollectionUtils.isEmpty(dirFiles)) {
            return 0L;
        }
        // 过滤掉文件夹对象和在回收站、保险箱的文件
        return dirFiles.stream()
                .filter(e -> !e.getIsDir())
                .filter(e -> !e.getDeleted())
                .filter(e -> !e.getInSafe())
                .filter(e -> e.getSize() != null)
                .flatMapToLong(e -> LongStream.of(e.getSize()))
                .reduce(0L, Long::sum);
    }
    @Override
    public List<UserFile> listBinFiles(String account) {
        User user = getUser(account);
        return this.list(new QueryWrapper<UserFile>().eq("user_id", user.getId())
                .eq("deleted", true));
    }

    @Override
    public boolean clearBinFiles(String account) {
        List<UserFile> binFiles = listBinFiles(account);
        for (UserFile binFile : binFiles) {
            if (binFile.getIsDir()) {
                deleteDir(account,binFile.getOssPath());
            }else {
                deleteFile(account, binFile.getOssPath());
            }
        }

        List<String> ids = binFiles.stream().map(UserFile::getId).collect(Collectors.toList());
        return this.removeByIds(ids);
    }

    @Override
    public boolean withdrawBinFile(String account, String ossPath) {
        User user = getUser(account);
        UserFile file = fileMapper.getDeletedFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        file.setDeleted(false);
        return this.updateById(file);
    }

    @Override
    public List<DownloadDirResp> downloadDir(String account, String shareId, String objectPath) {
        // 如果不是文件夹
        User user;
        if (account != null) {
            user = getUser(account);
        }else {
            Share share = shareService.getById(shareId);
            user = userService.getById(share.getUserId());
        }

        if (!objectPath.endsWith("/")) {
            throw new RuntimeException("非文件夹！");
        }
        // 获取文件夹下所有文件
        String userBucket = user.getBucketName();
        UserFile dir = getFileByPath(user.getId(), objectPath);
        List<UserFile> dirFiles = getDirFiles(user.getId(), dir);
        ArrayList<DownloadDirResp> downloadDirResps = new ArrayList<>();
        dirFiles.forEach(f -> {
            if (!f.getIsDir()) {
                DownloadDirResp resp = new DownloadDirResp();
                resp.setUrl(ossUtil.generateUrl(userBucket, f.getOssPath(), new Date(System.currentTimeMillis() + 1000 * 60 * 60)));
                resp.setName(f.getName());

                String filedirPath = f.getOssPath().substring(0, f.getOssPath().lastIndexOf('/') + 1);
                resp.setFoldPath(truncateHeadString(filedirPath, dir.getName()));
                downloadDirResps.add(resp);
            }

        });
        return downloadDirResps;

    }


     // 去除文件夹名称前面的某个字符串
    private String truncateHeadString(String filedirPath, String dirName) {
        // 当前文件夹位置
        int idx = filedirPath.indexOf(dirName);
        // 从之后开始截取
        filedirPath = filedirPath.substring(idx );
        return filedirPath;
    }

//    @Override
//    @Deprecated()
//    public boolean downloadDir(String account, String objectPath, String localPath) {
//        // 如果不是文件夹
//
//        if (!objectPath.endsWith("/")) {
//            throw new RuntimeException("非文件夹！");
//        }
//
//        // 创建根文件夹
//        String rootPath =  objectPath.substring(0, objectPath.lastIndexOf('/'));
//        rootPath = rootPath.substring(rootPath.lastIndexOf('/') + 1);
//        File file = new File(localPath + rootPath);
//        if (!file.exists()) {
//            file.mkdir();
//        }else {
//            // 存在，防止覆盖，生成新文件夹名
//            file = new File(localPath + rootPath + RandomUtil.randomString(6));
//            if (!file.exists()) {
//                file.mkdir();
//            }
//        }
//
//        // 下载文件到根文件夹
//        User user = getUser(account);
//        List<UserFile> dirFiles = getDirFiles(user.getId(), null);
//        try {
//            for (UserFile f : dirFiles) {
//                // 空文件夹
//                if (f.getIsDir()) {
//                    File tmpFile = new File(localPath + f.getOssPath());
//                    if (!tmpFile.exists()) {
//                        tmpFile.mkdir();
//                    }
//                }else {
//                    log.info("正在下载" + f.getOssPath());
//                    download(account, localPath + f.getOssPath(), f.getOssPath());
//                }
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return true;
//    }


    @Override
    public String generatePreviewUrl(String account, String ossPath, Date date) {
        User user = getUser(account);
        UserFile file = getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        String bucketName = userService.getUserBucket(account);

        return ossUtil.generateUrl(bucketName, ossPath, date);
    }

    @Override
    public boolean doesFileExists(String account, String ossPath) {
        User user = getUser(account);
        UserFile file = this.getFileByPath(user.getId(), ossPath);
        // 在保险箱或者回收站
        if (file.getInSafe() || file.getDeleted()) {
            return true;
        }
        return false;
    }

    @Override
    public GetUserFIleSpaceResp getUserFileSpace(String account) {
        User user = getUser(account);
        Long userUsedSpace = ossUtil.getUserUsedSpace(user.getBucketName());
        log.info("usedSpace:" + userUsedSpace);
        GetUserFIleSpaceResp resp = new GetUserFIleSpaceResp();
        resp.setUsed(userUsedSpace);
        resp.setTotal(user.getTotalSpace());
        String usedSizeStr = FileUtils.getFileSize(userUsedSpace);
        String totalSizeStr = FileUtils.getFileSize(user.getTotalSpace());
        resp.setSpaceStr(usedSizeStr + " / " + totalSizeStr);
        return resp;
    }


    public Boolean createDir(String account, String prefix, String dirName) throws FileAlreadyExistsException {
        User user = getUser(account);
        if (StringUtils.isEmpty(dirName)) {
            throw new IllegalArgumentException("文件夹名称不能为空！");
        }
        UserFile dirFile;
        // 根目录创建
        if (StringUtils.isEmpty(prefix)) {
            dirFile = fileMapper.selectOne(new QueryWrapper<UserFile>().eq("name", dirName).eq("parent_id", "-1").eq("user_id",user.getId()));
        }else {
            //子目录创建
            String ossPath = prefix + dirName + "/";
            dirFile = fileMapper.selectOne(new QueryWrapper<UserFile>().eq("oss_path", ossPath).eq("user_id",user.getId()));
        }
        if (Objects.nonNull(dirFile)) {
            log.error("文件夹 " + dirFile +"已存在！");
            throw new FileAlreadyExistsException("文件夹已存在！");
        }
        // 根目录下创建

        String bucketName = userService.getUserBucket(account);
        boolean dir = ossUtil.createDir(bucketName, prefix, dirName);
        if (!dir) {
            throw new RuntimeException("文件夹创建失败");
        }
        UserFile file = new UserFile();
        file.setUserId(user.getId());
        file.setOssPath(prefix + dirName + "/");
        file.setName(dirName);
        file.setIsDir(true);
        file.setType("dir");
        // 判断是否为根目录下
        if (!StringUtils.isEmpty(prefix)) {
            UserFile parentDir = fileMapper.getParentDirByPath(prefix, user.getId());
            if (Objects.isNull(parentDir)) {
                throw new RuntimeException("父文件夹不存在！");
            }
            file.setParentId(parentDir.getId());
        }else {
            file.setParentId("-1");
        }
        file.setSize(0L);
        boolean save = this.save(file);
        if (!save) {
            throw new RuntimeException("文件夹创建失败");
        }
        return save;

    }




    @Override
    public UserFile getFileByPath(Long userId, String ossPath) {
        if (StringUtils.isEmpty(ossPath)) {
            return null;
        }
        return fileMapper.getFileByPath(userId, ossPath);
    }

    @Override
    public String generateUrl(String account, String ossPath, Date expireTime) {
        User user = getUser(account);
        UserFile file = getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        String url;
        String bucketName = userService.getUserBucket(account);
        // 区分分享文件夹
        if (file.getIsDir()) {
            url = file.getOssPath();
        }else {
            url = ossUtil.generateUrl(bucketName, ossPath, expireTime);
        }
        Long expireSeconds = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
        // redis设置过期时间
        Share share = new Share();
        share.setFileName(file.getName());
        share.setFileType(file.getType());
        share.setUrl(url);
        share.setUserName(user.getName());
        share.setExpireTime(expireTime);
        share.setUserId(user.getId());
        share.setFilePath(file.getOssPath());
        if (file.getIsDir()) {
            share.setSize(calculateDirSize(user.getId(), file));
        }else {
            SimplifiedObjectMeta ossDetail = ossUtil.getDetail(bucketName, ossPath);
            share.setSize(ossDetail.getSize());
        }
        share.setStatus(ShareStatus.NORMAL.getCode());
        shareService.save(share);
        //  短链生成
        String shareCode = UUID.randomUUID().toString().substring(5, 16);
        redisUtils.set(shareCode, share.getId(),expireSeconds);
        redisUtils.set(share.getId(), shareCode,expireSeconds);
        redisUtils.set(REDIS_SHARE_PREFIX + share.getId(), url,expireSeconds);

        return LINK_PREFIX + shareCode;
    }


    @Override
    public String createQr(String shareUrl) {
        String fileName = UUID.randomUUID() + ".png";
        return qrCodeUtils.createQrCode(shareUrl,fileName);
    }

    @Override
    public boolean rename(String account, String ossPath, String newName) {
        User user = getUser(account);
        String bucketName = userService.getUserBucket(account);
        UserFile file = getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }

        // 如果存在同名文件不允许修改
        String newOssPath = ossPath.substring(0, ossPath.lastIndexOf("/") + 1) + newName;
        if (getFileByPath(user.getId(), newOssPath) != null) {
            throw new RuntimeException("该路径已存在该文件名！");
        }
        // 如果文件名没有改变，直接返回
        if (file.getName().equals(newName)) {
            return true;
        }
        // 文件夹命名，oss拷贝再删除文件，性能消耗大
        if (file.getIsDir()) {
            String oldPath = file.getOssPath();
            file.setName(newName);
            file.setUpdateTime(new Date());
            String newPath = ossPath.substring(0, ossPath.substring(0,ossPath.length() - 1).lastIndexOf('/') + 1) + newName + "/";
            file.setOssPath(newPath);
            this.updateById(file);

            List<UserFile> dirFiles = getDirFiles(user.getId(), file);
            if (!CollectionUtils.isEmpty(dirFiles)) {
                dirFiles.forEach(f -> {
                    String oldFilePath = f.getOssPath();
                    String newFilePath = oldFilePath.replace(oldPath, newPath);
                    f.setOssPath(newFilePath);
                    if (!f.getIsDir()) {
                        ossUtil.copyObject(bucketName, oldFilePath, newFilePath);
                        ossUtil.deleteFile(bucketName, oldFilePath);
                    }
                    boolean b = this.updateById(f);
                    if (!b) {
                        throw new RuntimeException("重命名失败!");
                    }
                });
            }
            return true;
        }
        file.setName(newName);
        file.setUpdateTime(new Date());
        String newPath = ossPath.substring(0,ossPath.lastIndexOf('/') + 1) + newName;
        file.setOssPath(newPath);
        boolean b = this.updateById(file);
        if (!b) {
            throw new RuntimeException("重命名失败!");
        }
        ossUtil.copyObject(bucketName, ossPath, newPath);
        return ossUtil.deleteFile(bucketName, ossPath);
    }

    @Override
    public boolean moveToBin(String account, String ossPath) {
        User user = getUser(account);
        UserFile file = getFileByPath(user.getId(), ossPath);
        if (Objects.isNull(file)) {
            throw new RuntimeException("文件不存在！");
        }
        file.setDeleted(true);
        return this.updateById(file);
    }


    @Override
    public List<UserFile> getDirFiles(Long userId, UserFile dirFile) {
        List<UserFile> childFilesById = fileMapper.getChildFilesById(userId, dirFile.getId());
        // 如果文件夹下无文件
        if (CollectionUtils.isEmpty(childFilesById)) {
            return null;
        }
        List<UserFile> resFiles = new ArrayList <>();
        LinkedList<UserFile> queue = new LinkedList<>();
        // 添加一级文件夹下的文件夹进队列，文件进res
        childFilesById.forEach(f -> {
            if (f.getIsDir()) {
              queue.offer(f);
            }else {
                resFiles.add(f);
            }
        });

        // BFS
        while (!queue.isEmpty()) {
            UserFile dir = queue.poll();
            List<UserFile> childFiles = fileMapper.getChildFilesById(userId, dir.getId());
            // 该文件夹下无文件，直接添加，本地显示空文件夹
            // 添加该文件夹
            resFiles.add(dir);
            if (CollectionUtils.isEmpty(childFiles)) {
                continue;
            }
            // 有文件，添加该文件夹下的文件夹进队列，文件进res
            childFiles.forEach(f -> {
                if (f.getIsDir()) {
                    queue.offer(f);
                }else {
                    resFiles.add(f);
                }
            });
        }
        return resFiles;
    }
    private User getUser(String account) {
        return userService.getOne(new QueryWrapper<User>().eq("account", account));
    }

    public static void main(String[] args) {
//        System.out.println(UUID.randomUUID().toString().substring(5,16).replaceAll("-",""));
//        String s = "test/aaa/bbbbb/1.jpg";
//        String pathName = s.substring(0,s.indexOf("aaa") + 4);
//        System.out.println(pathName);
//        System.out.println(s.substring(0, s.indexOf("bbbbb") + 6));
//
//        System.out.println(FileUtil.getSuffix("test/aaa/1.jpg"));
//        System.out.println(FileUtil.getPrefix("test/aaa/1.jpg"));
    }
}
