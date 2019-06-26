package think.in.spring.boot.app.config;

import org.springframework.context.annotation.*;
import think.in.spring.boot.app.User;

/**
 * @author Pop
 * @date 2019/6/26 20:54
 */
@ImportResource(value = "classpath:/META-INF/spring/others.xml")
@Configuration
public class SpringContextConfiguration {

    @Lazy
    @Primary
    @DependsOn("springContextConfiguration")//依赖springContextConfiguration
    @Bean(name = "user") //Bean的名称为user
    public User user(){
        User user = new User();
        user.setName("Pop");
        return user;
    }

}
