#### Chanpter-8 Spring 注解编程驱动

在第八章，作者介绍了在Spring-Boot中的一种设计模式

和平时我们提到的单例模式，代理模式不同

这里的设计模式更像是将某个功能作为一个单独模块并制作成一个可以通过@Enable注解来决定是否要开启这个功能的设计模式。

例如WebMvc模块，AspectJ代理模块，Caching缓存模块等。

@Enable模块驱动在Spring Framework、Spring Boot、Spring Cloud中都有设计到，这种模块都用@Enable注解作为前缀

| 框架实现         | @Eanble注解模块               | 激活模块             |
| ---------------- | ----------------------------- | -------------------- |
| Spring Framework | @EnableWebMvc                 | WebMvn模块           |
|                  | @EnableTransactionManagement  | 事务管理模块         |
|                  | @EnableCaching                | Caching模块          |
|                  | @EnableMBeanExport            | JMX模块              |
|                  | @EnableAsync                  | 异步处理模块         |
|                  | @EnableWebFlux                | WebFlux模块          |
|                  | @EnableAspectJAutoProxy       | AspectJ代理模块      |
| Spring Boot      | @EnableAutoConfiguration      | 自动装配模块         |
|                  | @EnableManagementContext      | Actuator管理模块     |
|                  | @EnableConfigurationProperies | 配置属性属性绑定模块 |
|                  | @Enable0Auth2Sso              | OAuth2单点登录模块   |
| Spring Cloud     | @EnableEurekaServer           | Eureka服务模块       |
|                  | @EnableConfigServer           | 配置服务器模块       |
|                  | @EnableFeignClients           | Feign客户端模块      |
|                  | @EnableZuulProxy              | 服务网关Zuul模块d    |
|                  | @EnableCiruitBreaker          | 服务熔断模块         |

引入这么多模块，主要是为了**按需转配**

作者认为，Spring Framework 分为两大类实现，按照实现难易程度，从易到难分，分为**注解驱动**和**接口编程**

同时，无论依靠按照那种你方法，都需要spring3.0引入的@Import，之前有说道一个@ImportResource，那个主要用来导入XML配置文件。
而这个，则用于导入一个或者多个ConfigurationClass，将其注册为Spring Bean，不过在3.0中，只支持@Configuration 标注的类。

但是在3.1中，@Import的职责有多扩大，那就是ImportSelector和ImportBeanDefinitionRegistrar的实现类。

这个详细解释，可以看看这个项目的autoconfig包下的实现。

##### 自定义的@Enable模块驱动

```java
@Configuration
public class HelloWorldConfiguration {

    @Bean
    public String helloWorld(){//创建名为 “hello world”String 类型的bean
        return "hello ,Pop";
    }

}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(HelloWorldConfiguration.class)//支持相应模块
public @interface EnableHelloWorld {
}

@EnableHelloWorld
@Configuration
public class EnableHelloWorldBootStrap {

    public static void main(String[] args) {
        //配置注解启动上下文
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();
        //注册当前引导类，(被@Configuration标注)到Spring上下文
        context.register(EnableHelloWorldBootStrap.class);
        context.refresh();//启动
        //获取对象
        String helloWorld = context.getBean("helloWorld",String.class);//正常打印
        System.out.println(helloWorld);
        context.close();
    }

}
```

##### 自定义接口

