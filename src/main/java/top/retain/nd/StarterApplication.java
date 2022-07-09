package top.retain.nd;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Retain
 * @date 2021/9/29 12:28
 */
@SpringBootApplication
@EnableSwagger2
@MapperScan(value = "top.retain.nd.mapper")
public class StarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }

}
