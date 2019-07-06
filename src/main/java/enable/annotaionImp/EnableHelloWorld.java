package enable.annotaionImp;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Pop
 * @date 2019/7/6 15:48
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HelloWorldConfiguration.class)//支持相应模块
public @interface EnableHelloWorld {
}
