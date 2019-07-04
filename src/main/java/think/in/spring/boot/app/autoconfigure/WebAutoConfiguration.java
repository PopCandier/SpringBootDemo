package think.in.spring.boot.app.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;
import think.in.spring.boot.app.config.WebConfiguration;

/**
 * @author Pop
 * @date 2019/6/24 23:45
 */
//@ConditionalOnWebApplication 是web容器才会装配
//@Configuration
@Indexed
@Import(WebConfiguration.class)
public class WebAutoConfiguration {
    /*
    * 由于注解编程简化了配置，但是解析是会有性能消耗的，所以 springframk5.0加入了@Inedexed
    * 它将会为@Component和派生注解添加索引，减少运行时性能消耗。
    * */
}
