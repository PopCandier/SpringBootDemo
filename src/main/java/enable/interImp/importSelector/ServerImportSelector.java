package enable.interImp.importSelector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author Pop
 * @date 2019/7/6 16:05
 */
public class ServerImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        //读取EnableServer 中所有的属性方法，本例中只有type()属性方法
        //其中 key 为属性方法的名称，value 为属性返回对象
        Map<String,Object> annotationAttributes =
                importingClassMetadata.getAnnotationAttributes(EnableServer.class.getName());
        //获取名字为type的属性和方法，并强制转换成Server.Type类型
        Server.Type type= (Server.Type) annotationAttributes.get("type");
        //导入类名和数组
        String[] importClassNames = new String[0];
        switch (type){
            case HTTP:
                importClassNames = new String[]{HttpServer.class.getName()};
                break;
            case FTP:
                importClassNames = new String[]{FtpServer.class.getName()};
                break;
        }
        return importClassNames;
    }
}
