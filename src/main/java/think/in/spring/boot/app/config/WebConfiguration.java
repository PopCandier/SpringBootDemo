package think.in.spring.boot.app.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Indexed;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author Pop
 * @date 2019/6/24 22:19
 */
@Configuration
@Indexed
//    @EnableAutoConfiguration
//    @SpringBootApplication
public class WebConfiguration {

    @Bean
    public RouterFunction<ServerResponse> helloWorld(){
        return route(RequestPredicates.GET("/hello-world"),
                request-> ServerResponse.ok().body(Mono.just("Hello,World"),String.class)
        );
    }

    @Bean
    public ApplicationRunner runner(BeanFactory beanFactory){
        return args->{
            System.out.println(" hello World Bean 的实现类是 "+beanFactory.getBean("helloWorld").getClass().getName());
            System.out.println(" WebConfiguration Bean 实现类为 "+beanFactory.getBean(WebConfiguration.class).getClass().getName());
        };
    }

    @EventListener(WebServerInitializedEvent.class)
    public void onWebServerReady(WebServerInitializedEvent event){
        System.out.println("当前 WebServer 实现类为 ;"+ event.getWebServer().getClass().getName());
    }

}
