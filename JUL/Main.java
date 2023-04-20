import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) throws SecurityException, IOException {
      // 首先获取日志打印器
      Logger logger = Logger.getLogger(Main.class.getName());
      
      //打印父日志打印器
      System.out.println(logger.getParent().getClass());

      //修改日志级别
      logger.setLevel(Level.ALL);
      //不使用父日志处理器
      logger.setUseParentHandlers(false);
      
      
      //使用自定义日志处理器
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.ALL);
      logger.addHandler(handler);

      //添加输出到本地文件
      FileHandler fileHandler = new FileHandler("JUL/test.log");
      fileHandler.setLevel(Level.WARNING);
      
      //使用自定义日志处理器(文本日志使用控制台格式)
      SimpleFormatter formatter = new SimpleFormatter();
      fileHandler.setFormatter(formatter);
      logger.addHandler(fileHandler);

      //自定义过滤规则
      logger.setFilter(record -> !record.getMessage().contains("普通"));
      
      // 调用info来输出一个普通的信息，直接填写字符串即可
      logger.info("我是普通的日志");

      logger.log(Level.SEVERE, "严重的错误", new IOException("我就是错误"));
      logger.log(Level.WARNING, "警告的内容");
      logger.log(Level.INFO, "普通的信息");
      logger.log(Level.CONFIG, "级别低于普通信息");
    }
}
