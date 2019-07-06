package enable.interImp.importSelector;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pop
 * @date 2019/7/6 16:14
 */
@EnableServer(type = Server.Type.HTTP)//设置http
@Configuration
public class EnableServerBootstrap {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new
                AnnotationConfigApplicationContext();
        context.register(EnableServerBootstrap.class);
        context.refresh();

        Server server  = context.getBean(Server.class);
        server.start();
        server.stop();

        context.close();

    }

}
