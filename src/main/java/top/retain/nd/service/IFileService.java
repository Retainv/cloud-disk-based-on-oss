package top.retain.nd.service;

import com.aliyun.oss.model.OSSObjectSummary;
import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.dto.DownloadDirResp;
import top.retain.nd.dto.GetFileDetailResp;
import top.retain.nd.dto.GetUserFIleSpaceResp;
import top.retain.nd.dto.ListFilesPagedResp;
import top.retain.nd.entity.UserFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author Retain
 * @date 2021/9/29 18:43
 */
public interface IFileService extends IService<UserFile> {
    List<ListFilesPagedResp> listFilesPaged(String account, Integer currentPage, Integer maxKeys);
    Boolean deleteFile(String account,String filePath);
    Boolean deleteDir(String account,String filePath);
    void streamDownload(String account, String objectPath, HttpServletResponse response);
    String browserDownload(String account, String objectPath);
    List<OSSObjectSummary> listFilesByPrefixPaged(String account, Integer currentPage, Integer size, String prefix);
    Boolean saveUploadFile(Long userId,UserFile file);

    List<UserFile> listFilesByPrefix(String account, String prefix);

    UserFile getFileByPath(Long userId, String ossPath);

    String generateUrl(String account, String ossPath, Date expireSeconds);

    String createQr(String shareUrl);

    boolean rename(String account, String ossPath, String newName);

    boolean moveToBin(String account, String ossPath);

    void sortWithDir(List<UserFile> list);

    GetFileDetailResp detail(String account, String ossPath);

    List<UserFile> listBinFiles(String account);

    boolean clearBinFiles(String account);

    boolean withdrawBinFile(String account, String ossPath);

    List<DownloadDirResp> downloadDir(String account,String shareId, String objectPath);


    String generatePreviewUrl(String account, String ossPath, Date date);

    boolean doesFileExists(String account, String ossPath);

    GetUserFIleSpaceResp getUserFileSpace(String account);

    List<UserFile> getDirFiles(Long userId, UserFile dirFile);


}
