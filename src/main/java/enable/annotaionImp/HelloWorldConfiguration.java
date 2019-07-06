package enable.annotaionImp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pop
 * @date 2019/7/6 15:46
 */
@Configuration
public class HelloWorldConfiguration {

    @Bean
    public String helloWorld(){//创建名为 “hello world”String 类型的bean
        return "hello ,Pop";
    }

}
