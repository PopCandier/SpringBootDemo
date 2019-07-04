package autoconfig;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

/**
 * @author Pop
 * @date 2019/7/4 16:53
 */
public class LoggerDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Class beanClass = LoggerService.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
        String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());//首字母小写
        registry.registerBeanDefinition(beanName,beanDefinition);
    }
    //另外一种注入bean的方法
}
