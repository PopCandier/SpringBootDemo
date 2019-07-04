package autoconfig;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Pop
 * @date 2019/7/4 16:26
 */
public class CacheImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //可以获得源信息，也就是配置的信息
        Map<String,Object> attribute=
                importingClassMetadata.getAnnotationAttributes(EnableDefineService.class.getName());



        return new String[]{CacheService.class.getName()};//固定,这里面的内容将会被spring托管
    }
}
