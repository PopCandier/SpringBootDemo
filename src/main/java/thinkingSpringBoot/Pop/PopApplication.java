package thinkingSpringBoot.Pop;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class PopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PopApplication.class, args);
	}

	/*
	* Spring -boot2.0新引入了一周年Application Context的实现
	* WebServerApplicationContext 他提供了获取 WebServer的接口方法 getWebServer()
	* 只需要注入WebServerApplicationContext对象，并且在Springboot应用启动后，在输出到关联的WebServer实现类即可
	* */
//	@Bean
//	public ApplicationRunner runner(WebServerApplicationContext context){
//		return args->{
//			System.out.println(context.getWebServer().getClass().getName());
//		};
//
//	}

	@EventListener(WebServerInitializedEvent.class)
	public void onWebServerReady(WebServerInitializedEvent event){
		System.out.println("当前 WebServer 实现类为 ;"+ event.getWebServer().getClass().getName());
	}

}
