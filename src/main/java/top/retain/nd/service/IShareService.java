package top.retain.nd.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.retain.nd.entity.Share;
import top.retain.nd.entity.User;

/**
 * @author Retain
 * @date 2021/12/5 11:39
 */
public interface IShareService extends IService<Share> {
    Share getShareDetail(String shareCode);

    boolean transferFile(String shareId, String savePath, String account);

    boolean exist(String shareId);

    boolean downloadCount(String shareId);

    User getUser(String account);

    boolean deleteShare( String id);

    boolean cancelShareByPath(Long userId,String ossPath);

    String encode(String id);

    boolean extract(String id, String code);
}
