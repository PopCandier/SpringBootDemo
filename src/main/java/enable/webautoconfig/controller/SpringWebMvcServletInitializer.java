package enable.webautoconfig.controller;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * @author Pop
 * @date 2019/7/7 23:05
 */
public class SpringWebMvcServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[0];
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return of(SpringWebMvcConfiguration.class);
    }

    @Override
    protected String[] getServletMappings() {
        return of("/*");
    }

    private static <T> T[] of(T... values){//利用api，减少new T[] 代码
        return values;
    }
}
