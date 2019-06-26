package think.in.spring.boot.app.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Pop
 * @date 2019/6/26 21:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface StringRepository {
    //自定义一个我们自己的 Repository

    /**
     * 属性名名称必须与Component value 一致
     * @return
     */
    String value() default "";
}
