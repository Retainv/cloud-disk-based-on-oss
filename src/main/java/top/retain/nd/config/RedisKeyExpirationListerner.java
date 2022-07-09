package top.retain.nd.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import top.retain.nd.common.ShareStatus;
import top.retain.nd.entity.Share;
import top.retain.nd.service.IShareService;

import javax.annotation.Resource;

import static top.retain.nd.service.impl.FileService.REDIS_SHARE_PREFIX;

/**
 * 监听redisKey过期回调
 */
@Component
@Slf4j
public class RedisKeyExpirationListerner extends KeyExpirationEventMessageListener {


    @Resource
    IShareService shareService;
    /**
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisKeyExpirationListerner(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 使用该方法监听 ,当我们的key失效的时候执行改方法
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String keyExpire = message.toString();
        String id = keyExpire.substring(keyExpire.indexOf(':') + 1);
        log.info(keyExpire);
        // 对于指定前缀的key进行处理
        if (keyExpire.startsWith(REDIS_SHARE_PREFIX)) {
            Share share = new Share();
            share.setId(id);
            Share byId = shareService.getById(id);
            BeanUtils.copyProperties(byId, share);
            share.setStatus(ShareStatus.DESABLED.getCode());
            boolean b = shareService.updateById(share);
            log.info("key为：" + keyExpire + "的分享已经过期！更新状态结果：" + b);
        }

    }

}
