package top.retain.nd.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author Retain
 * @date 2021/2/25 16:48
 */
@Component
@Slf4j
public class CustomMetaObjectHandler implements MetaObjectHandler{


        @Override
        public void insertFill(MetaObject metaObject) {
            log.info("**创建中...自动填充时间**");
            this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
            this.strictInsertFill(metaObject, "lastLoginTime", Date.class, new Date());
            this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐)

        }

        /**
         * 创建时不会填充！只会在更新时填充
         * @param metaObject
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            log.info("**自动填充更新时间**");
            this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date()); // 起始版本 3.3.0(推荐)
        }

}
