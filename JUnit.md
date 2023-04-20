# JUnit

## 尝试JUnit

这里使用JUnit4 并不是最新版本, 主要用于了解

```java
public class TestMain {
    @Test
    public void method(){
        System.out.println("我是测试用例1");
    }

    @Test
    public void method2(){
        System.out.println("我是测试用例2");
    }
}
```

只需要在方法前写上@Test注解, 就可以执行此测试案例

测试方法:

1. 必须public
2. 不能静态方法
3. 返回void
4. 必须没有任何参数

## 断言工具

Assert.assertEquals方法

```java
public class TestMain {
    @Test
    public void method(){
        System.out.println("我是测试案例！");
        Assert.assertEquals(1, 2);    //参数1是期盼值, 参数2是实际测试结果值
    }
}
```

如果想测试数据库中取数据是否为我们预期的数据:

```java
@Test
public void method(){
    try (SqlSession sqlSession = MybatisUtil.getSession(true)){
        TestMapper mapper = sqlSession.getMapper(TestMapper.class);
        Student student = mapper.getStudentBySidAndSex(1, "男");

        Assert.assertEquals(new Student().setName("小明").setSex("男").setSid(1), student);
    }
}
```

如果想在进行所有的测试前都执行一些前置操作的话, 可以使用@Before注解

```java
private SqlSessionFactory sqlSessionFactory;
    @Before
    public void before(){
        System.out.println("测试前置正在初始化...");
        try {
            sqlSessionFactory = new SqlSessionFactoryBuilder()
                    .build(new FileInputStream("mybatis-config.xml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("测试初始化完成，正在开始测试案例...");
    }
```

同时也可以使用@After注解添加结束操作

JUnit5对@Before和@After做了调整优化
