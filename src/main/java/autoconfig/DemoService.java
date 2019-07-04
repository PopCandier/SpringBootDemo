package autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * @author Pop
 * @date 2019/7/4 18:56
 */
//@ConditionalOnClass(name = "cacheService")
public class DemoService {
    public void test(){
        System.out.println("DemoService !");
    }
}
