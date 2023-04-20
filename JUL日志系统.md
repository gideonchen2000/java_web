# JUL日志系统

使用日志框架来规范化日志输出, 为了在debug时候打印出某些信息, 而在实际运行时不打印日志

JDK为我们提供了一个自带的日志框架, 位于java.util.logging包下, 我们可以使用此框架来实现日志的规范化打印

```java
public class Main {
    public static void main(String[] args) {
          // 首先获取日志打印器
        Logger logger = Logger.getLogger(Main.class.getName());
          // 调用info来输出一个普通的信息，直接填写字符串即可
        logger.info("我是普通的日志");
    }
}

// 结果
// 4月 20, 2023 11:15:11 下午 Main main
// 信息: 我是普通的日志
```

## JUL介绍

日志分为7个级别, 详细信息我们可以在Level类中查看：

- SEVERE (最高值) --- 一般用于代表严重错误
- WARNING - 一般用于表示某些警告，但是不足以判断为错误
- INFO (默认级别) --- 常规消息
- CONFIG
- FINE
- FINER
- FINEST (最低值)

可以通过log方法来设定该条日志的输出级别：

```java
    logger.log(Level.SEVERE, "严重的错误", new IOException("我就是错误"));
    logger.log(Level.WARNING, "警告的内容");
    logger.log(Level.INFO, "普通的信息");
    logger.log(Level.CONFIG, "级别低于普通信息");

// 结果

// 4月 20, 2023 11:26:22 下午 Main main
// 信息: 我是普通的日志
// 4月 20, 2023 11:26:22 下午 Main main
// 严重: 严重的错误
// java.io.IOException: 我就是错误
//         at Main.main(Main.java:15)

// 4月 20, 2023 11:26:22 下午 Main main
// 警告: 警告的内容
// 4月 20, 2023 11:26:22 下午 Main main
// 信息: 普通的信息
```

可以发现 级别低于默认级别的日志信息, 无法输出到控制台, 当然也可以通过设置来修改日志打印级别:

```java
    //修改日志级别
    logger.setLevel(Level.CONFIG);
    //不使用父日志处理器
    logger.setUseParentHandlers(false);
    //使用自定义日志处理器
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.CONFIG);
    logger.addHandler(handler);
```

每个Logger都有一个 父日志打印器, 我们可以通过getParent()来获取:

```java
System.out.println(logger.getParent().getClass());

//结果
//java.util.logging.LogManager$RootLogger
```

得到一个java.util.logging.LogManager$RootLogger类, 它默认使用的是ConsoleHandler(也就是将信息输出到控制台), 且日志级别为INFO, 由于每一个日志打印器都会直接使用父类的处理器, 所以之前需要关闭父类的然后使用我们自己的处理器

同时 日志信息不光可以输出到控制台, 还可以使用文件处理器来处理日志信息:

```java
//添加输出到本地文件
FileHandler fileHandler = new FileHandler("test.log");
fileHandler.setLevel(Level.WARNING);
logger.addHandler(fileHandler);
```

可以自定义打印格式, 控制台默认使用的是SimpleFormatter, 而文件处理器则默认使用XMLFormatter

```java
    //使用自定义日志处理器(文本日志使用控制台格式)
    SimpleFormatter formatter = new SimpleFormatter();
    fileHandler.setFormatter(formatter);
```

日志可以以设置过滤器, 当不希望某些日志被输出时, 可以配置过滤规则

```java
    //自定义过滤规则
    logger.setFilter(record -> !record.getMessage().contains("普通"));
```
