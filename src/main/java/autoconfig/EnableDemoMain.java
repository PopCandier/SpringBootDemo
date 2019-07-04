package autoconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Pop
 * @date 2019/7/4 17:00
 */
@SpringBootApplication
@EnableDefineService
public class EnableDemoMain {

    public static void main(String[] args) {
        ConfigurableApplicationContext context=SpringApplication.run(EnableDemoMain.class,args);

//        System.out.println(context.getBean(CacheService.class));
//        System.out.println(context.getBean(LoggerService.class));
        context.getBean(DemoService.class).test();
    }

}
