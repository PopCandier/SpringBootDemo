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

具体参考代码



#####  Spring Web 自动装配

Spring Framework 3.1 里程碑意义不但在于提供了**模块装配**的能力

还有一个另外能力 **Web自动装配**

同时 Spring Boot的自动装配大致可以分为两种

第一种是Web应用，还一种是非Web应用，从3.1以及更高版本，支持的自动装配，仅仅限于Web应用场景，同时依赖与Servlet3.0+容器，Tomcat7.x和Jetty7.x

新引入的WebApplicationinitializer构建在ServletContainerInitializer之上

此外 WebApplicationinitialize 属于SpringMVC提供的接口，确保WebApplicationInitializer自定义实现，能够被任何Servlet3容器检测并初始化。P253有WebApplicationinitialize实现方法。

如果你觉得实现WebApplicationinitialize比较困哪，也提供另外一种简单的方法

即AbstractDispacherServletInitializer，除此之外，还有另外一种实现。

AbstractAnnotationConfigDispatcherServletInitizlizer.

AbstractAnnotationConfigDispatcherServletInitizlizer是AbstractDispacherServletInitializer的子类。

不过要记住的是，这个是mvc的包，所以需要导入一下

```xml
<dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.2.0.BUILD-SNAPSHOT</version>
        </dependency>
```

```java
public class MyWebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[0];
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
```

```java
public class MyWebApplicationContext extends AbstractDispatcherServletInitializer {
    @Override
    protected WebApplicationContext createServletApplicationContext() {
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
        return context;
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
       return null;
    }
}
```

##### 自定义的Web自动装配

```java
@Controller
public class HelloWorldController {

    @RequestMapping
    @ResponseBody
    public String helloWorld(){
        return "hello world";
    }

}
//再定义配置
/**
 * @author Pop
 * @date 2019/7/7 23:03
 */
@EnableWebMvc//之前提过，激活webmVC模块
@Configuration
@ComponentScan(basePackageClasses = SpringWebMvcConfiguration.class)
public @interface SpringWebMvcConfiguration {
}

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
```

接着打包成出来，运行，就可以发现其中的即便不依赖Spring Boot，也是可以运行的

##### Web 自动装配原理

原文中，作者说明，其实Spring框架本身并不具备 **web自动装配**原生能力。

这个技术的来源其实来自Servlet3.0的技术

1. ServletContext配置方法

在传统的javaweb项目中，装配serlvet，Filter和各种Listener的时候，都需要web.xml文件，但是一旦运行就无法调整，不够灵活

所以这个从Serlvet3.0开始，这个限制就被打破了。

因为Serlvet3.0的各种api可以让我们用编程的方式，使用这个配置。

以下为ServletContext的使用配置

| 配置组件 | 配置方法                   | 配置对象                                         |
| -------- | -------------------------- | ------------------------------------------------ |
| Servlet  | ServletContext#addServlet  | ServletRegistration或ServletRegistration.Dynamic |
| Filter   | ServletContext#addFilter   | FilterRegistration或FilterRegistration.Dynamic   |
| Listener | ServletContext#addListener | 无                                               |

以Servlet为例子，有三种重载类型

* addServlet(String,Class)
* addServlet(String,Servlet)
* addServlet(String.className)

当然，ServletContext对人为我们提供了运行时的装配能力，但是要自动装配还差一点。

2.运行时插拔

要达到自动装配的目的，关键还是要在适当的时间加以装配

其实，当Servlet，Filter，Listener动态装配钱，都需要在某个时间点调用ServletContext配置方法

因为这是Serlvet3.0规范

在文中，指的是，ServletContext配置方法，他们**只能**在ServletContextListener#contextInitialized或者ServletContextListener#onStartup方法中被调用。

同时，规范也定义了ServletContextListener的职责，他负责监听Servlet上下文(ServletContext)的生命周期时间，包括 **初始化**和**销毁**两个事件

其实**初始化**事件由ServletContextListener#contextInitialized监听。当然，servlet和Filter对外提供服务的时候，必然经过Servlet上下文ServletContext初始化事件。

当容器启动的时候，ServletContainerInitializer#onStartup(Set<Class<?>>,ServletContext)方法将会被回调，同时为了选择关系的类型，可以选择HandesTypes来进行过滤，也就是通过value属性来指定。 

##### 再说说自动装配的话题

回到如何实现Web自动装配，按照目前已知的部分，如果你需要一个Servlet需要装配并提供web服务，那么首先应该是用ServletContext去装配这些东西，也就是ServeltContext#addServlet方法。最后再使用ServletContainerInitializer#onStartup方法加以实现。

但是如何实现还是个问题，那么我们回到Springframek的3.1

这个版本中增加了ServletContainerInitizlizer的实现类SpringServletContainerInitializer

```java
@HandlesTypes(WebApplicationInitializer.class)
//这将意味着，onstartup方法，将会传入WebApplicationInitializer的子类（包括抽象类）的集合
public class SpringServletContainerInitializer implements ServletContainerInitializer {

	@Override
    //由于使用了HandlesTypes注解进行过滤，所以，这里将会是WebApplicationInitializer的集合作为入参，也就是webAppInitializerClasses
	public void onStartup(@Nullable Set<Class<?>> webAppInitializerClasses, ServletContext servletContext)
			throws ServletException {

		List<WebApplicationInitializer> initializers = new LinkedList<>();

		if (webAppInitializerClasses != null) {
			for (Class<?> waiClass : webAppInitializerClasses) {
				if (!waiClass.isInterface() && !Modifier.isAbstract(waiClass.getModifiers()) &&
						WebApplicationInitializer.class.isAssignableFrom(waiClass)) {
					try {
                        //添加
						initializers.add((WebApplicationInitializer)
								ReflectionUtils.accessibleConstructor(waiClass).newInstance());
					}
					catch (Throwable ex) {
						throw new ServletException("Failed to instantiate WebApplicationInitializer class", ex);
					}
				}
			}
		}

		if (initializers.isEmpty()) {
			servletContext.log("No Spring WebApplicationInitializer types detected on classpath");
			return;
		}

		servletContext.log(initializers.size() + " Spring WebApplicationInitializers detected on classpath");
		AnnotationAwareOrderComparator.sort(initializers);
		for (WebApplicationInitializer initializer : initializers) {
            //一个一个启动
			initializer.onStartup(servletContext);
		}
	}

}
```

当然Spring Framework 3.1 没有提供具体实现，而是将这种弹性的能力提供给了开发者，随着Spring Framework 3.2发布，框架内提供了三种抽象的实现

* AbstractContextLoaderInitializer
  * AbstractDispatcherServletInitializer
    * AbstactAnnotaionConfigDispatcherServletInitializer

为什么这三种都是抽象类呢。

1. 由于这三者都是WebApplicationInitizlier的实现类，那么这三个类均会被SpringServletContainerInitializer识别，并作为具体实现添加到WebApplicationInitializer集合initializers中，然后迭代启动startup
2. 抽象实现提供模版话的配置接口，最终决策权在于开发者

简单说一下三个抽象类的使用场景

* AbstractContextLoaderInitializer ： 如果构建了WebRoot应用上你下文（WebApplicationContext）成功则代替web.xml注册ContextLoaderListener
* AbstractDispatcherServletInitializer ： 代替web.xml注册DispatcherServlet，并且如果必要的话，创建Web Root应用上下文 WebApplicationContext
* AbstactAnnotaionConfigDispatcherServletInitializer：具备annotaion配置驱动能力的AbstractDispatcherServletInitializer 



2019/7/8 	0：21的总结

今天主要对之前@Import的几种自动装配的补充，也就是ImportSelector和ImportBeanDefintionRegister的接口编程，让spring托管我们自己的自定义模块，也就是spring中的Enable设计模式。

然后，谈到spring web的自动装配。

在这里我们知道了spring中的自动装配，并不是自己发明，而是站在巨人的肩膀上进行改良的。这个巨人的规范就是serlvet3.0规范，其中提出了ServletContext的api，此api代替了传统意义上的web.xml中比较不灵活的servlet，filter和listener的装配，取而代之的就是addServlet、addFilter、和addListener，以及他的启动流程

负责监听Servlet容器的**初始化**和**销毁**的ServletContextListener

* 初始化：ServletContextListener#contextInitializer
* 销毁：ServletContextListener#contextDestroyed
* 启动：ServletContainerInitializer#onStartup

**请注意**以上都是serlvet3.0的规范。我们可以通过这些api来动态装配Servlet、Filter、Listener

然后，启动都会走onStartup流程。那么关于如何实现，spring3.1给出了

`SpringServletContainerInitializer`的具体实现，那么其中就按照serlvet3.0的规定，在

@HandlesTypes中指定了WebApplicationInitializer作为将会被启动的类。也就是会被调用onstartup的类。同时spring3.1给出了三个抽象的类，也就是

AbstractContextLoaderInitializer

- AbstractDispatcherServletInitializer
  - AbstactAnnotaionConfigDispatcherServletInitializer

每个类的环境不同，但都是spring中按照serlvet3.0的规范作为自我拓展的产物。







