package enable.annotaionImp;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pop
 * @date 2019/7/6 15:50
 */
@EnableHelloWorld
@Configuration
public class EnableHelloWorldBootStrap {

    public static void main(String[] args) {
        //配置注解启动上下文
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();
        //注册当前引导类，(被@Configuration标注)到Spring上下文
        context.register(EnableHelloWorldBootStrap.class);
        context.refresh();//启动
        //获取对象
        String helloWorld = context.getBean("helloWorld",String.class);//正常打印
        System.out.println(helloWorld);
        context.close();
    }

}
