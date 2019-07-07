package enable.webautoconfig.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Pop
 * @date 2019/7/7 23:03
 */
@EnableWebMvc//之前提过，激活webmVC模块
@Configuration
@ComponentScan(basePackageClasses = SpringWebMvcConfiguration.class)
public @interface SpringWebMvcConfiguration {
}
