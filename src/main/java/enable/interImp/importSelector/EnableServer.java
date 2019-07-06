package enable.interImp.importSelector;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Pop
 * @date 2019/7/6 16:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ServerImportSelector.class)
public @interface EnableServer {

    /**
     * 读取服务器类型
     * @return non-null
     */
    Server.Type type();

}
