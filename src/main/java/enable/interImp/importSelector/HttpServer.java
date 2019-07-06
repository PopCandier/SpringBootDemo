package enable.interImp.importSelector;


import org.springframework.stereotype.Component;

/**
 * @author Pop
 * @date 2019/7/6 16:01
 */
@Component
public class HttpServer implements Server {
    @Override
    public void start() {
        System.out.println("Http 服务器启动中");
    }

    @Override
    public void stop() {
        System.out.println("Http 服务器关闭中");
    }
}
