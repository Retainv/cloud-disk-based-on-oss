package top.retain.nd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisListenerConfig {
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory){
        RedisMessageListenerContainer container=new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }

}
