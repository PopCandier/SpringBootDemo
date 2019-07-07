package enable.interImp.importBeanReg;

/**
 * @author Pop
 * @date 2019/7/6 15:59
 */
public interface Server {
    /**
     * 假设当前应用支持两种服务类型，Http，Ftp，通过 @EnableServer 设置服务器类型type
     * 提供响应服务
     */
    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务器
     */
    void stop();

    enum Type{
        HTTP,//http服务器
        FTP//FTP
    }
}
