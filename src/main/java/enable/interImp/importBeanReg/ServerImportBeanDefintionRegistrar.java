package enable.interImp.importBeanReg;

import enable.interImp.importSelector.ServerImportSelector;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.stream.Stream;

/**
 * @author Pop
 * @date 2019/7/7 22:28
 */
public class ServerImportBeanDefintionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
       //复用一下
        ImportSelector importSelector = new ServerImportSelector();
        String[] selectedNames = importSelector.selectImports(importingClassMetadata);

        //创建Bean的定义
        Stream.of(selectedNames).map(BeanDefinitionBuilder::genericBeanDefinition)
                .map(BeanDefinitionBuilder::getBeanDefinition)
                .forEach(beanDefinition->{
                    //注册BeanDefinition到BeanDefinitionRegistry
                    BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition,registry);
                });

    }
}
