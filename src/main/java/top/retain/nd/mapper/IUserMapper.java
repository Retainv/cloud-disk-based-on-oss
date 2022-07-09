package top.retain.nd.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.PathVariable;
import top.retain.nd.entity.User;

/**
 * @author Retain
 * @date 2021/9/29 17:00
 */
@Mapper
public interface IUserMapper extends BaseMapper<User> {
    Boolean login(@PathVariable("account") String account, @PathVariable("password") String password);
    String getUserBucket(@PathVariable("account") String account);
    User selectByPhone(@PathVariable("phoneNumber") String phoneNumber);

    User selectByAccount(@PathVariable("account") String account);

}
