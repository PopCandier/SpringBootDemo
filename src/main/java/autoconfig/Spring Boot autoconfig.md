#### Spring Boot的另一种理解

服务于spring 框架的框架



##### 约定由于配置的体现

* maven 的目录结构
* spring-boot-starter-web。内置tomcat、resource、template、static
* 默认的application.properties



#### SpringBoot里面没有新技术

* AutoConfiguration 自动装配
* Starter
* Actuator
* SpringBoot CLI



AutoConfigurationImportSelector与AutoConfigurationPackages.Registrar

这两个类位于@EnableAutoConfiguration中

在spring中，可不可以根据上下文来激活不同的bean

动态注入

ImportSelector
ImportBeanDefinitionRegistrar

首先实现ImportSelector



SPI 扩展点

也就是我们之前的META-INF/spring.factories 下面的内容

写在这里的内容将会被自动装配，为了导入的时候将会内容，key就是注解的路径名

* 一样的目录结构
* 一样的文件名
* 一样的key

ConditionalOnBean

条件注解，首先，他的用法和上面一样

META-INF/spring-autoconfigure-metadata.properties

可以在spring-boot,autoconfiguration包下找到。

```properties
com.pop.Person.ConditionalOnClass = com.pop.Teacher
# 表示当 com.pop.Teacher 存在的时候，Person对象将会被被spring容器接管
```



我们模仿@SpringBootApplication中的@EnableAutoConfigurtaion

中发现一个并不是@Configuartion修饰的类，但是还是被装配的了

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)//<---这里
public @interface EnableAutoConfiguration {

	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	/**
	 * Exclude specific auto-configuration classes such that they will never be applied.
	 * @return the classes to exclude
	 */
	Class<?>[] exclude() default {};

	/**
	 * Exclude specific auto-configuration class names such that they will never be
	 * applied.
	 * @return the class names to exclude
	 * @since 1.3.0
	 */
	String[] excludeName() default {};

}
```

这个方法本质上实现了 ImportSelector接口，他可以对类进行判断注入，我们也可以使用这种方法

定义我们自己的annotation

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({CacheImportSelector.class,LoggerDefinitionRegistrar.class})//首先，这里不是configuartion，而是importSelector
public @interface EnableDefineService {
    Class<?>[] exclude() default {};//定义来说，如果填写了，表示不希望注入的bean的class
}

```

从importSelecotr说起

```java
public class CacheImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        //可以获得源信息，也就是配置的信息
        Map<String,Object> attribute=
                importingClassMetadata.getAnnotationAttributes(EnableDefineService.class.getName());
		
        return new String[]{CacheService.class.getName()};//固定,这里面的内容将会被spring托管
    }
}

```

返回的String[]这个数组里面写的类名将会被spring托管，也就是说，你可以根据自定义的元数据(meta data)来**判断**应该返回给spring托管的bean类名集合

第二种，ImportBeanDefinitionRegistrar

```java
/**
 * @author Pop
 * @date 2019/7/4 16:53
 */
public class LoggerDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Class beanClass = LoggerService.class;
        RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
        String beanName = StringUtils.uncapitalize(beanClass.getSimpleName());//首字母小写
        registry.registerBeanDefinition(beanName,beanDefinition);
    }
    //另外一种注入bean的方法
}
```

用法一致，没什么好讲的。被注册的bean，将会被spring托管。

此外，

```java
public class AutoConfigurationImportSelector
		implements DeferredImportSelector, BeanClassLoaderAware, ResourceLoaderAware,
		BeanFactoryAware, EnvironmentAware, Ordered {

	//....

	@Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
        //这个方法将会解析 META-INF/spring-autoconfigure-metadata.properties
		AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
				.loadMetadata(this.beanClassLoader);
        //  进入这个方法
		AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(
				autoConfigurationMetadata, annotationMetadata);
		return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
	}
	//....
```

所以

```java
protected AutoConfigurationEntry getAutoConfigurationEntry(
			AutoConfigurationMetadata autoConfigurationMetadata,
			AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
    		//这个方法将会解析 META-INF/spring.factories
		List<String> configurations = getCandidateConfigurations(annotationMetadata,
				attributes);
    // 去除  META-INF/spring.factories 重复定义的，因为这个文件可能会有很多
		configurations = removeDuplicates(configurations);
    // 去除 不希望加入的bean，这个可以在注解上配置
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		checkExcludedClasses(configurations, exclusions);
    // 移除掉他们
		configurations.removeAll(exclusions);
    //与 META-INF/spring-autoconfigure-metadata.properties 比对过滤条件
		configurations = filter(configurations, autoConfigurationMetadata);
    //开始过滤
		fireAutoConfigurationImportEvents(configurations, exclusions);
    //返回结果
		return new AutoConfigurationEntry(configurations, exclusions);
	}
```

这是属于SPI，也就是所谓的扩展点。一定要符合规范。

```pro
# 自动装配 spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  think.in.spring.boot.app.autoconfigure.WebAutoConfiguration,\
  autoconfig.DemoService
```

```properties
#spring-autoconfigure-metadata.properties
#autoconfig.DemoService.ConditionalOnClass=
autoconfig.DemoService.ConditionalOnClass=autoconfig.CacheService,autoconfig.TestService
# 首先我需要说明的是@SpringBootApplication 中的 EnableAutoConfiguration注解可以有自动装配的功能
# spring.factories   spring-autoconfigure-metadata.properties 是一种标准
# 前者由于 只要写了key，将会被装配
# 后者将会过滤，条件不符合将不会被装备，如果你没有写条件，像是上面这样，将会不会被装配

```

当然key也不能乱写

```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  think.in.spring.boot.app.autoconfigure.WebAutoConfiguration,

org.springframework.boot.test.autoconfigure.core.AutoConfigureCache=\
   autoconfig.DemoService
```

会报错

```c
java.lang.IllegalStateException: Unable to read meta-data for class 
	at org.springframework.boot.autoconfigure.AutoConfigurationSorter$AutoConfigurationClass.getAnnotationMetadata(AutoConfigurationSorter.java:245) ~[spring-boot-autoconfigure-2.1.5.RELEASE.jar:2.1.5.RELEASE]
	at org.springframework.boot.autoconfigure.AutoConfigurationSorter$AutoConfigurationClass.getOrder(AutoConfigurationSorter.java:214) ~[spring-boot-autoconfigure-2.1.5.RELEASE.jar:2.1.5.RELEASE]
	at org.springframework.boot.autoconfigure.AutoConfigurationSorter$AutoConfigurationClass.access$000(AutoConfigurationSorter.java:155) ~[spring-boot-autoconfigure-2.1.5.RELEASE.jar:2.1.5.RELEASE]
	at org.springframework.boot.autoconfigure.AutoConfigurationSorter.lambda$getInPriorityOrder$0(AutoConfigurationSorter.java:63) ~[spring-boot-autoconfigure-2.1.5.RELEASE.jar:2.1.5.RELEASE]
	at java.util.TimSort.countRunAndMakeAscending(TimSort.java:355) ~[na:1.8.0_202]
	at java.util.TimSort.sort(TimSort.java:234) ~[na:1.8.0_202]

```

这个估计要符合什么规范吧

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ImportAutoConfiguration
public @interface AutoConfigureCache {
    @PropertyMapping("spring.cache.type")
    CacheType cacheProvider() default CacheType.NONE;
}
```

