package autoconfig;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Pop
 * @date 2019/7/4 16:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({CacheImportSelector.class,LoggerDefinitionRegistrar.class})//首先，这里不是configuartion，而是importSelector
public @interface EnableDefineService {
    Class<?>[] exclude() default {};//定义来说，如果填写了，表示不希望注入的bean的class
}
