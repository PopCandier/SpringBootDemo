#### Spring Boot 编程思想

[SpringBoot自动装配扩展](https://github.com/PopCandier/SpringBootDemo/blob/master/src/main/java/autoconfig/Spring%20Boot%20autoconfig.md)

[注解编程驱动](https://github.com/PopCandier/SpringBootDemo/blob/master/src/main/java/enable/Chanpter-8%20Spring%20%E6%B3%A8%E8%A7%A3%E7%BC%96%E7%A8%8B%E9%A9%B1%E5%8A%A8.md)

#### 从创建一个Spring Boot项目开始。

start.spring.io 我们可以很轻松的构建一个spring-boot项目

下载完，我们直接导入pom.xml文件，Maven将会帮我们直接构建spring-boot所需要的依赖。

**如何打包**

我们cd到spring-boot的根目录，执行

```
mvn package
```

不过前提是你需要拥有这个插件。

```xml
<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
```

当构建完成后，将会存在一个.jar文件，你可以直接使用

`java -jar xxxx.jar`

去运行这个打包的文件。

这里有两个打包

`.jar`文件和`.jar.original`，前者比较大，因为他将依赖的包打包进来，后者只是代码部分，所以比较小，我们可以通过tree ..看到目录结构。

```c
D:\IDEAWORKSPACE\POP\TARGET\TEMP
├─BOOT-INF
│  ├─classes   //存放应用编译后的class文件
│  │  └─thinkingSpringBoot
│  │      └─Pop
│  └─lib  //存放依赖的jar包
├─META-INF //存放应用相关的元信息 MANIFEST.MF
│  └─maven
│      └─thinkingSpringBoot
│          └─Pop
└─org  //目录存在Spring Boot 相关的 class 文件
    └─springframework
        └─boot
            └─loader
                ├─archive
                ├─data
                ├─jar
                └─util
                
D:\IDEAWORKSPACE\POP\TARGET\TEMP2
├─META-INF
│  └─maven
│      └─thinkingSpringBoot
│          └─Pop
└─thinkingSpringBoot
    └─Pop

```

为什么使用java -jar 会就会启动？

我们从解包出来的文件看出，这并不是标准的文件目录

而官方文档规定，javar -jar 命令引导的具体启动类必须配置在MANIFEST.MF资源的Main-Class中

而同时，根据 “JAR文件规范” MANIFEST.MF资源必须放在MATA-INF目录下

```mf
Manifest-Version: 1.0
Implementation-Title: Pop
Implementation-Version: 0.0.1-SNAPSHOT
Start-Class: thinkingSpringBoot.Pop.PopApplication
Spring-Boot-Classes: BOOT-INF/classes/
Spring-Boot-Lib: BOOT-INF/lib/
Build-Jdk-Spec: 1.8
Spring-Boot-Version: 2.1.5.RELEASE
Created-By: Maven Archiver 3.4.0
Main-Class: org.springframework.boot.loader.JarLauncher
```

所以很显然，能够使得这个能够运行的，奥秘在

`org.springframework.boot.loader.JarLauncher`这里面。

当然，有能够启动jar当然也有能够启动 war

`org.springframework.boot.loader.WarLauncher`

当然我们如果希望查看这个的实现，需要添加额外的依赖。

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-loader</artifactId>
			<scope>provided</scope>
		</dependency>
```

然后我们可以看到这个类的全貌

```java
public class JarLauncher extends ExecutableArchiveLauncher {

	static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";

	static final String BOOT_INF_LIB = "BOOT-INF/lib/";

	public JarLauncher() {
	}

	protected JarLauncher(Archive archive) {
		super(archive);
	}

	@Override
	protected boolean isNestedArchive(Archive.Entry entry) {
		if (entry.isDirectory()) {//对于资源的定位
			return entry.getName().equals(BOOT_INF_CLASSES);
		}
		return entry.getName().startsWith(BOOT_INF_LIB);
	}

	public static void main(String[] args) throws Exception {
		new JarLauncher().launch(args);//<----从这里开始
	}

}
```

很明显，这个是启动的关键。

```java
public abstract class Launcher {

	/**
	 * Launch the application. This method is the initial entry point that should be
	 * called by a subclass {@code public static void main(String[] args)} method.
	 * @param args the incoming arguments
	 * @throws Exception if the application fails to launch
	 */
	protected void launch(String[] args) throws Exception {
        //这一句话表示注册协议
		JarFile.registerUrlProtocolHandler();
		ClassLoader classLoader = createClassLoader(getClassPathArchives());
		launch(args, getMainClass(), classLoader);
	}
	
	//.....	
｝
```

JDK中，默认支持file、HTTP、jar等协议并且这些实现都在sun.net.ww.protocol类中，名字都为Handler,因为这是规定。

sun.net.www.protocel.${protocol}.Handler，中间表示协议名，file、jar、http、https、ftp并且以上都为java.net.URLStreamHandler实现

JarFile.registerUrlProtocolHandler();执行的时候，将会把spring-boot自己实现的Hanlder追加到java的系统属性，java.protocol.handler类中

问题在于jdk已经实现了自己的jar方法，为什么spring-boot还要有自己选择覆盖呢，这点在P49的set方法上有说明。

大概地意思是spring-boot中地jar文件是一个独立地应用，与其他地jar不一样，他除了自己地jar文件之外，还有依赖其余地jar文件，所以jdk那套不适用于他，当执行java jar 命令地时候，内部地sun.net.www.protocol.jar.Handler无法被当做class path，所以需要被覆盖。

```java
ClassLoader classLoader = createClassLoader(getClassPathArchives());
```

这一步主要是为了区别目前是通过jar驱动还是war驱动，前者将会使用sprint boot fat jar后者用于解压其内容。接着调用launcher()方法。

```java
launch(args, getMainClass(), classLoader);
```

```java
protected void launch(String[] args, String mainClass, ClassLoader classLoader)
			throws Exception {
		Thread.currentThread().setContextClassLoader(classLoader);
		createMainMethodRunner(mainClass, args, classLoader).run();
	}
```

这一步就是从/META-INF/MAINFEST.MF资源周年读取start-class文件，并调用main方法启动。

**探究WarLauncher和JarLauncher的区别**、

其实两者区别还是很小地，一个明显地差别就是，常量地设定

首先是JarLauncher

```java
static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";

	static final String BOOT_INF_LIB = "BOOT-INF/lib/";

```

其次是WarLauncher

```java
private static final String WEB_INF = "WEB-INF/";

	private static final String WEB_INF_CLASSES = WEB_INF + "classes/";

	private static final String WEB_INF_LIB = WEB_INF + "lib/";

	private static final String WEB_INF_LIB_PROVIDED = WEB_INF + "lib-provided/";
```

war得定义很类似传统地serlvet容器地定义 web-inf/classes 之类地

我们再打包一次，看看区别

```java
<groupId>thinkingSpringBoot</groupId>
	<artifactId>Pop</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>war</packaging>
```

执行 mvn clean package

```java
├─META-INF
│  └─maven
│      └─thinkingSpringBoot
│          └─Pop
├─org
│  └─springframework
│      └─boot
│          └─loader
│              ├─archive
│              ├─data
│              ├─jar
│              └─util
└─WEB-INF
    ├─classes
    │  └─thinkingSpringBoot
    │      └─Pop
    ├─lib
    └─lib-provided
```

其实

```java
private static final String WEB_INF_LIB_PROVIDED = WEB_INF + "lib-provided/";
```

是spring-boot地war湿度有地，

这个目录只有一个

spring-boot-loader-x.x.x.RELEASE.jar

文件,不过更具serlvet规范,WBE-INF/lib-provided中地jar不会被servlet容器读取，会被忽略。

这样设计好处是，war是一种兼容地措施，既可以被warLauncher启动，又可以兼容servlet容器环境。换言之，warLauncher与JarLauncher并没本质区别，所以建议Spring-boot应用使用非传统web部署地时候，尽可能使用jar地方式。



#### 理解固化地Maven

此固化仅仅限制于spring-boot应用。

当然我们通过单击继承地方式，会有些限制

```xml
<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.5.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
```

可以通过这种依赖地方式替换

```xml
<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-dependencies</artifactId>
				<version>2.1.5.RELEASE</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

不过，如果你这样打包会出问题，因为他会认为web.xml是必选，所以在你设置打包方式为

war地情况下，它会依赖maven-war-plugin插件，而web-inf/web.xml又是必须地，所以会报错。我们可以通过以下方式修改。

```java
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-war-plugin:2.2:war (default-war) on project Pop: Error assembling WAR:
 webxml attribute is required (or pre-existing WEB-INF/web.xml if executing in update mode) -> [Help 1]

```

```xml
 <plugin>
         <groupId>org.apache.maven.plugins</groupId>
         <artifactId>maven-war-plugin</artifactId>
         <configuration>
            <failOnMissingWebXml>false</failOnMissingWebXml>
         </configuration>
      </plugin>
```

##### spring-boot-starter-parent与spring-boot-dependencies的区别

首先从pom文件地继承方式上来看，dependencies是parent地父类，换言之，我们选择直接继承dependencies都是可以的

当然。这个插件地版本，我们可以进到dependencies中，看看对应地版本具体是什么样的，为了兼容。

不过当我们运行java -jar命令地时候

```
D:\IdeaWorkSpace\Pop\target>java -jar Pop-0.0.1-SNAPSHOT.war
Pop-0.0.1-SNAPSHOT.war中没有主清单属性
```

会有这样地提示，不过结合前面地理论，首先war肯定是执行地了地，但是真正启动地确实依赖spring-boot-maven-plugin插件，所有java -jar对应地插件没有执行，所以我们需要对插件指定版本。不过这个时候插件依旧无法执行，所以我们还需要额外地配置。

```xml
<plugin>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-maven-plugin</artifactId>
			<version>2.1.5.RELEASE</version>
			<executions>
				<execution>
					<goals>
						<goal>
							repackage
						</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
```

##### 总结

- 关于maven-war-plugin地差异，是因为版本地差异，2.2版本会出现web.xml必填写地选项，而3.1调整了其默认地必填行为
- 另外，在单独引用spring-boot-maven-plugin地时候，需要配置<goal>元素，否则不会将spring-boot地引导依赖重新打包（repackage）进jar，所以无法进行引导当前应用。、
- 最后一点是，一般来说我们不用使用spring-boot-dependencies来作为maven项目地parent，尽管spring-boot-starter-parent与spring-boot-dependencies，两者是继承关系。



```
2019-06-15 14:51:04.681  INFO 11348 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080
 (http) with context path ''
```

我们使用

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
```

就将tomcat容器引导加载我们地应用，明明都不要安装，这是为什么

#### 嵌入式容器

**对于tomcat的嵌入式总结**

Tomcat7+Maven插件可以构建可执行jar或者war文件，实现独立的web应用程序，也支持servlet组件的自动装配

如果是tomcat8更高版本的，需要借鉴更高的插件，P75

**对于jetty容器**

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
			<!--将tomcat容器排除掉-->
			<exclusions>
				<exclusion>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-tomcat</artifactId>
				</exclusion>
			</exclusions>
		</dependency>


		<!--引入Jetty-->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jetty</artifactId>
		</dependency>
```

对于undertow容器

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-undertow</artifactId>
		</dependency>
```

##### 嵌入式Reactive Web容器

原文里，表示Reactive web是springboot2.0的新特性，但是想要激活他，需要增加

spring-boot-starter-webflux依赖，并且不能与spring-boot-starter-web同时存在。

如果同时存在，webflux将会被忽略。

想要看到这个效果，指出 UndertowServletWebServer 是spring boot webserver 的实现类，这里强调servlet的原因是，undertow还有其他实现，那就是 reactive web 的实现 -undertowwebServer，想要这样除了只留下webflux依赖，还要把编译等级改成8

```java
/*
	* Spring -boot2.0新引入了一周年Application Context的实现
	* WebServerApplicationContext 他提供了获取 WebServer的接口方法 getWebServer()
	* 只需要注入WebServerApplicationContext对象，并且在Springboot应用启动后，在输出到关联的WebServer实现类即可
	* */
	@Bean
	public ApplicationRunner runner(WebServerApplicationContext context){
		return args->{
			System.out.println(context.getWebServer().getClass().getName());
		};

	}
```

不过，我们可以通过使用WebServerInitializedEvent，来监听更广的事件，原文中指出

ServletWebServerInitializedEvent是WebServerInitializedEvent的子类

```java
@EventListener(WebServerInitializedEvent.class)
	public void onWebServerReady(WebServerInitializedEvent event){
		System.out.println("当前 WebServer 实现类为 ;"+ event.getWebServer().getClass().getName());
	}
```

这样会稍微安全一点，因为避免了注入WebServerApplicationContext失败的原因

最后，比较主流的三个容器，tomcat，jetty,undertow都支持reative web容器，这个具体如何配置在P95有详细讲解。

|   容器   |          Maven依赖           |  WebServer实现类  |
| :------: | :--------------------------: | :---------------: |
|  Tomcat  |  spring-boot-starter-tomcat  |  TomcatWebServer  |
|  Jetty   |  spring-boot-starter-jetty   |  JettyWebServer   |
| Undertow | spring-boot-starter-undertow | UndertowWebServer |

#### 自动装配

我们常见的自动装配类的方法

* xml元素的`<context:component-scan>`
* 注解@Import
* 注解@Configuration

前者需要，ClassPathXmlApplicationContext加载，后者需要AnnotationConfigApplicationContext注册。

@import的使用

```java
public class Dog{}
public class Cat{}
@ComponentScan
/*把用到的资源导入到当前容器中*/
@Import({Dog.class, Cat.class})
public class App {

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        context.close();
    }
}
```

会有结果。

当然你也可以导入一个配置类

```java
public class MyConfig {

    @Bean
    public Dog getDog(){
        return new Dog();
    }

    @Bean
    public Cat getCat(){
        return new Cat();
    }

}
//@SpringBootApplication
@ComponentScan
/*导入配置类就可以了*/
@Import(MyConfig.class)
public class App {

    public static void main(String[] args) throws Exception {

        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        System.out.println(context.getBean(Dog.class));
        System.out.println(context.getBean(Cat.class));
        context.close();
    }
}
```

很显然，运行WebFlux和嵌入式Web容器均已自动装配，那么是否可以认为当前引导类

PopApplication充当了@Configuration类的角色呢，答案是肯定的。

原文中指出，SpringBootApplication等价于三种标签

@Configuration（标注为配置类）/@ComponentScan（激活@Component的扫描）/@EnableAutoConfiguartion（负责激活spring-boot自动装配功能）

我们将SpringBootApplication替换成以上三种依旧可以执行。

```java
//@SpringBootApplication
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class PopApplication {
	//少一个都不行
	public static void main(String[] args) {
		SpringApplication.run(PopApplication.class, args);
	}


}
```

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM,
				classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
```

* 2.0的spring-boot中ComponetScan并非使用了默认值，而是添加了排除的TypeFilter实现。前者1.4开始支持，`为了查找BeanFactory中已经注册的TypeExcludeFilter Bean`，后者从1.5开始支持，用于排除同时标注了@Configuration和@EnableAutoConfiguration的类。
* spring1.4开始，@SpringBootApplication 注解不再是@Configuration而是@SpringBootConfiguration，这种类似对象之间的继承关系，作者称之为“多层次@Component的‘’派生性”。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {

}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Configuration {
```

三者关系

* @Component
  * @Configuration
    * @SpringBootConfiguration

原文中，我们也可以用@EnableAutoConfiguration来代替@SpringBootApplication

那么两者有什么区别呢？

后者比前者多一个CGLIB提升的过程，即，使用SpringBootApplication修饰过。也就是"继承"了Configuration的类，装载时，将会使用cglib，也就是用@Bean修饰的

```java
//@Configuration
//    @EnableAutoConfiguration
    @SpringBootApplication
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
		think.in.spring.boot.app.config.WebConfiguration
		
		*/

}

//@Configuration
   @EnableAutoConfiguration
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
think.
in.spring.boot.app.config.WebConfigurationguration
	
	*/
}
```

所以cglib提升是为了@Configuration修饰后的类准备的，而非@Bean

##### 理解自动配置机制

我们之前定义了WebConfiguration属于编码方式的导入编程，而非自动装配。相反，其它自动装配的Bean肯定由某种机制完成的，这种机制就是自动配置的机制。

这里概括起来，就是spring-boot1.0添加了约定配置，可以自动导入@Configuration类的方式。这些注解需要标注到Configuration'上

* @ConditionalOnClass 当且仅当目标类存在于ClassPath下才可以装配。
* @ConditionOnMissingBean

当@Conditional中可以修饰一个class类，然后可以去查找这个类的所在实现

[@Conditional详解文章](https://blog.csdn.net/xcy1193068639/article/details/81491071)

**创建自动配置类**

在resource下面，新建META-INF/spring.factories

```properties
# 自动装配
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  think.in.spring.boot.app.autoconfigure.WebAutoConfiguration
```

#### 理解Production-Ready特性

在之前，我们可以使用自动装配，并且也可以使用@Conditional注解，都可以让Bean无声无息的被实例化，让spring为我们接管，我们越来越不清楚里面的细节。

所以我们需要一套运维的方案，了解Bean的组装情况，甚至是其他应用相关的信息，同时为了支持以配置方式调整应用行为，如Web服务端口，Spring-boot提供了Production-Ready特性

metrics(指标)/health checks（健康检查）/externalized configuration（外部配置）均属于为生产准备的特性

引出Spring Boot Actuator特性，他是“Production-Ready”的具体化

他的作用

* 使用场景：监视和管理投入生产的应用
* 监管媒介：HTTP或JMX端点(Endpoints)
* 端点类型：审计（Auditing）/健康 (Health )和指标收集（metrics gathering）
* 基本特点：自动运用（automatically applied）

添加依赖

```xml
<!--添加 Actuator依赖，主要用于监控应用
		production-ready概念的体现
		-->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
```

常用的Endpoints

* beans：显示当前Spring 应用上下文的Spring Bean完整列表，包含所有的ApplicationContext层次
* conditions：显示当前应用所有配置类和自动装配类的条件评估结果，(包含匹配和未匹配结果)
* env：暴露Spring ConfigurableEnvironment中的PropertySource属性
* health: 显示应用的健康信息（默认添加）
* info：显示任意的应用信息。（默认添加）

如果我们想要暴露其它endpoint可以加入这样的参数

这里用beans作为例子

`mvn spring-boot:run -Dmanagement.endpoints.web.exposure.include=beans`

接着，我们可以从控制台可以看到默认的/actuator将会被映射，然后我们在url地址输入

`http://localhost:8080/actuator/beans`

就可以得到目前所有的自动装配的Beans列表了

```json
{"contexts":{"application":{"beans":{"org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration":{"aliases":[],"scope":"singleton","type":"org.springframework.boot.autoconfigure.websocket.reactive.WebSocketReactiveAutoConfiguration$$EnhancerBySpringCGLIB$$fdd433b2","resource":null,"dependencies":[]},"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration$ReactiveWebHealthConfiguration":{"aliases":[],"scope":"singleton","type":"org.springframework.boot.actuate.autoconfigure.health.HealthEndpointWebExtensionConfiguration$ReactiveWebHealthConfiguration$$EnhancerBySpringCGLIB$$38c4c3b9","resource":null,"dependencies":["reactiveHealthIndicatorRegistry"]},"endpointCachingOperationInvokerAdvisor":{"aliases":[],"scope":"singleton","type":"org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor","resource":"class path resource [org/springframework/boot/actuate/autoconfigure/endpoint/EndpointAutoConfiguration.class]","dependencies":["environment"]},"org.springframework.boot.actuate.autoconfigure.web.reactive.ReactiveManagementContextAutoConfiguration":{"aliases":....
```

同时，如果你还想暴露更多的endpoint，需要在自动装配的类上加上参数

```java
@ConditionalOnWebApplication//<----这里
@Configuration
@Indexed
@Import(WebConfiguration.class)
public class WebAutoConfiguration {
    /*
    * 由于注解编程简化了配置，但是解析是会有性能消耗的，所以 springframk5.0加入了@Inedexed
    * 它将会为@Component和派生注解添加索引，减少运行时性能消耗。
    * */
}
```

同时，我们将启动的参数再变化一下

`mvn spring-boot:run -Dmanagement.endpoints.web.exposure.include=beans,conditions,env`

改变参数为

`http://localhost:8080/actuator/conditions`

可以得到当前系统的环境参数情况

#####  理解外部化配置

简单来说就是，我们可以通过新的语法来定义自己的配置，springboot中提供三种途径

* Bean 的 @Value
* Spring Environment 读取
* @ConfigurationProperties绑定到结构化对象中

原文中有比较详细的外部配置的加载顺序P130中

```java
@Component
public class MyBean{
	@Value("${name}")
    private String name;
}
```

这个值会从application.properties中读取，但是如果你在`命名行`给他命名了，那么他就是使用在命令行中的参数，因为外部化配置顺序 4是Commad line arguments，而优先于15.Applicationproperties packaged inside your jar(application.properties and YAML variants)

PropertySource就是“外部配置化”API的描述方式。

**总结**

Spring-boot主要有五大特性

* SpringApplication
* 自动装配
* 外部化配置
* Spring Boot Actuator
* 嵌入式web容器

最后引出了微服务概念，spring-boot因为缺少构建微服务的能力，

所以在此基础上研发了Spring-Cloud，可以帮助开发者快速构建通同的分布式系统

核心特性如下

* Distributed/versioned configuration（分布式配置）
* Service registration and dicovery （服务注册和发现）
* Routing （路由）
* Service-to-service calls (服务调用)
* Load balancing （负载均衡）
* Circuit Breakers （熔断机制）
* Distributed messaging （分布式消息）

最后作者向读者推荐了两本关于Spring Cloud的书籍

《Spring Cloud 微服务实战》作者：翟永超

《Spring Cloud 与 Docker 微服务架构实战》作者：周立



#### 走向自动装配

##### 走向注解驱动编程(Annotation-Driven)

* SpringFramework 1.x 不支持注解，只支持xml配置

* SpringFramework 2.x 增加几个注解

  * Bean相关的 @Required
  * 数据相关的@Repository
  * AOP的@Aspect

* SpringFramework 2.5 是比较重大的更新，算是一个分水岭

  * 依赖注入 @Autowired

    * 另外，依赖注入也是可以注入某种类型的集合的

    * ```java
      @Component("nameRepositoryHolder")
      public class NameRepositoryHolder{
          @Autowired
          private Collection<NameRepository> repositories;
      }
      ```

    * 当然，无论你autowired注入单个spring bean还是集合都是可以的，但这只局限于某一个class类，例如上面就是`NameRepository`的集合，如果你还想在细粒度的控制筛选，那么就需要@Qualifier

    * ```java
      @Component("nameRepositoryHolder")
      public class NameRepositoryHolder{
          @Autowired
          @Qualifier("chinesNameRepository")
          private Collection<NameRepository> repositories;
      }
      ```

      

  * 依赖查找 @Qualifier

  * 组件声明 @Component @Service

  * SpringMVC Annotation @Controller @RequestMapping @ModeAttribute 等

  * 支持 JSR-250 的 @Resource注入，包括PostConstruct和PreDestory

    * 前者可替代 <bean init-method="...">或者 Spring InitializingBean接口回调
    * 后者可代替<bean destroy-method="...">或者 DisposableBean

  * 但是 2.x是个尴尬的版本，他提供了很多的核心的anontaion，但是，却还是无法直接注解驱动，我们还是需要xml配置来驱动spring容纳 PathClassXmlApplicationContext,就类似`<context:annotation-config>`和`<context:component-scan>`前者启动注册Annoation处理器，后者负责扫描相对于classpath下面的指定java根包(base package)，寻找spring模式注解标记的类class，将他们注册成spring bean

  * 在1.x版本里，多个spring bean是需要排序的，一般的做法是实现ordered接口，从2.0开始通过在@Component class中标注@Order方式进行替代。

* Spring Framework 3.x 注解黄金时代

  * 从3.X开始，spring开始着重的要替换xml配置。@Configuration就是其中一个

  * ```java
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Component
    public @interface Configuration {
    
    	@AliasFor(annotation = Component.class)
    	String value() default "";
    
    }
    
    ```

  * 不过3.0的spring还是没找找到替换`<context:component-scan>`的注解，而是选择了过渡的方案@ImportResource和@Import

  * @ImportResource允许导入遗留的XML文件，而@Import则允许导入一个或多个SpringBean

  * ```java
    @ImportResource(value = "classpath:/META-INF/spring/others.xml")
    @Configuration
    public class SpringContextConfiguration {
    }
    
    ```

  * 所以3.0引入了新的驱动AnnotationConfigApplicationContext

  * ```java
    @ImportResource(value = "classpath:/META-INF/spring/others.xml")
    @Configuration
    public class SpringContextConfiguration {
    
        @Lazy
        @Primary//当多个想同类型的bean出现的时候，可以定义主要注入的为这个
        @DependsOn("springContextConfiguration")//依赖springContextConfiguration
        @Bean(name = "user") //Bean的名称为user
        public User user(){
            User user = new User();
            user.setName("Pop");
            return user;
        }
    
    }
    ```

  * @Configuration把一个类作为一个IoC容器，它的某个方法头上如果注册了@Bean，就会作为这个Spring容器中的Bean。 
    @Scope注解 作用域 
    @Lazy(true) 表示延迟初始化 
    @Service用于标注业务层组件、 
    @Controller用于标注控制层组件（如struts中的action） 
    @Repository用于标注数据访问组件，即DAO组件。 
    @Component泛指组件，当组件不好归类的时候，我们可以使用这个注解进行标注。 
    @Scope用于指定scope作用域的（用在类上） 
    @PostConstruct用于指定初始化方法（用在方法上） 
    @PreDestory用于指定销毁方法（用在方法上） 
    @Resource 默认按名称装配，当找不到与名称匹配的bean才会按类型装配。 
    @DependsOn：定义Bean初始化及销毁时的顺序 
    @Primary：自动装配时当出现多个Bean候选者时，被注解为@Primary的Bean将作为首选者，否则将抛出异常 
    @Autowired 默认按类型装配，如果我们想使用按名称装配，可以结合@Qualifier注解一起使用 
    @Autowired @Qualifier(“personDaoBean”) 存在多个实例配合使用

  * spring 3.1 开始引入了@ComponentScan去替换`<context:component-scan>`

​    

springframework 3.1 抽象了一套统一配置属性的API，包括配属属性存储接口 Environment以及配置属性源抽象 PropertySource，这个两个核心Api奠定了SpringBoot外部化配置的基础，也是Spring Cloud分布式配置的基石。 

但是需要掌握这种API，学习成本还是略大，因为你需要熟悉spring，并且对bean的生命周期了解。

所以提供了@PropertySource简化实现

* 缓存方面：API提供了 Cache 和缓存管理器 Cache Manager 配套注解 Caching 和 Cacheable 简化了数据缓存的开发
* 异步支持方面，引入了异步操作注解@Async，周期异步执行注解@Scheduled以及异步Web请求处理 DeferredResult
* 校验方面；Spring 引入了校验注解@Validated不但整合了JSR-303,而且还适配了Spring早期的Validator抽象



Spring Framework 4.x 驱动完善的时代

* 条件化注解@Conditional被引入

详细总结请看P153表格

#### Spring 注解编程模型

主要讨论一下几点

* 元注解 (Meta-Annotation)
* Spring 模式注解 （Stereotype Annotations）
* Spring 组合注解 （Composed Annoations）
* Spring 注解属性别名和覆盖（Attribute Alizses and Overrides）

##### 元注解 (Meta-Annotation)

表示能够标记上的注解上的注解，例如java中的@Documented还有@Inherited

spring中的@Component，他可以注解到@Service和@Repository上，所以他可以算是元注解

##### Spring 模式注解 （Stereotype Annotations）

作者解释道可以这样理解Spring 模式注解

`Spring注解即@Component“派生”注解`

 

由于Java语法规范规定，Annotation不允许继承，没有类派生子类的能力，因此Spring framework采用元标注的方式实现注解之间的派生

**自定义@Component“派生”注解**

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface StringRepository {
    //自定义一个我们自己的 Repository

    /**
     * 属性名名称必须与Component value 一致
     * @return
     */
    String value() default "";
}

```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <context:component-scan base-package="think.in.spring.boot.app.repository"/>

</beans>

```

并创建引导类

```java
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

```

成功打印

```c
....
22:11:43.132 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'org.springframework.context.annotation.internalCommonAnnotationProcessor'
22:11:43.140 [main] DEBUG org.springframework.beans.factory.support.DefaultListableBeanFactory - Creating shared instance of singleton bean 'chineseNameRepository'
[张三, 李四, 王五]
```

