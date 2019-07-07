package enable.webautoconfig.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Pop
 * @date 2019/7/7 23:01
 */
@Controller
public class HelloWorldController {

    @RequestMapping
    @ResponseBody
    public String helloWorld(){
        return "hello world";
    }

}
