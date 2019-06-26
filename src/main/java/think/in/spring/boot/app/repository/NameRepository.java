package think.in.spring.boot.app.repository;

import think.in.spring.boot.app.annotation.StringRepository;

import java.util.Arrays;
import java.util.List;

/**
 * @author Pop
 * @date 2019/6/26 21:55
 */
@StringRepository("chineseNameRepository")
public class NameRepository {
    public List<String> findAll(){
        return Arrays.asList("张三","李四","王五");
    }
}
