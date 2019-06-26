package think.in.spring.boot.app;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import think.in.spring.boot.app.repository.NameRepository;

/**
 * @author Pop
 * @date 2019/6/26 22:06
 */
public class DerivedComponentAnnotationBootStrap {
    //自定义StringRepository的引导类
    static {
        /**
         * 解决spring 2.5 不兼容 java 8 的问题
         * 同时，请注意Java Seurity策略 ，必须具备 PropertyPermission
         */
        System.setProperty("java.version","1.7.0");
    }

    public static void main(String[] args) {

        //构建驱动上下文
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        context.setConfigLocation("classpath:/META-INF/spring/context.xml");
        //启动
        context.refresh();

        //获取实例
        NameRepository nameRepository = (NameRepository) context.getBean("chineseNameRepository");
        System.out.println(nameRepository.findAll());
    }

}
