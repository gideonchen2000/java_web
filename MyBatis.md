# MyBatis

**MyBatis官方文档**<https://mybatis.org/mybatis-3/zh/index.html>

## 初次使用Mybatis

我们需要导入Mybatis的依赖, Jar包需要在github上下载  
编写Mybatis的配置文件 在项目根目录下新建名为mybatis-config.xml的文件  
配置文件完成后，我们需要在Java程序启动时，让Mybatis对配置文件进行读取并得到一个SqlSessionFactory对象：  

```java
public static void main(String[] args) throws FileNotFoundException {
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(new FileInputStream("mybatis-config.xml"));
    try (SqlSession sqlSession = sqlSessionFactory.openSession(true)){
            //暂时还没有业务
    }
}
```

- 读取实体类  
读取实体类肯定需要一个映射规则, 比如类中的哪个字段对应数据库中的哪个字段, 在查询语句返回结果后, Mybatis就会自动将对应的结果填入到对象的对应字段上. 新建名为TestMapper.xml的文件作为映射器  

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="TestMapper">
    <select id="selectStudent" resultType="com.test.entity.Student">
        select * from student
    </select>
</mapper>
```

配置文件中也要添加这个映射器  

```xml
<mappers>
    <mapper url="file:mappers/TestMapper.xml"/>
    <!--    这里用的是url，也可以使用其他类型，我们会在后面讲解    -->
</mappers>
```

可以通过namespace来绑定到一个接口上, 利用接口的特性, 可以直接指明方法的行为, 而实际实现则是由Mybatis来完成  

## 增删改查

只需要编写一个对应的映射器既可以了：  

```xml
<mapper namespace="com.test.mapper.TestMapper">
    <select id="studentList" resultType="Student">
        select * from student
    </select>
</mapper>
```

这些属性也可以被映射到一个Map上  
也可以自定义resultMap来设定映射规则  

```xml
<resultMap id="Test" type="Student">
    <result column="sid" property="sid"/>
    <result column="sex" property="name"/>
    <result column="name" property="sex"/>
</resultMap>
```

- 当一个类存在多个构造方法时会报错 需要用constructor标签来指定使用哪个构造方法

```xml
<resultMap id="test" type="Student">
    <constructor>
        <arg column="sid" javaType="Integer"/>
        <arg column="name" javaType="String"/>
    </constructor>
</resultMap>
```

值得注意的是, 指定构造方法后, 若此字段被填入了构造方法作为参数, 将不会通过反射给字段单独赋值, 而构造方法中没有传入的字段, 依然会被反射赋值

如果数据库中存在一个带下划线的字段, 我们可以通过设置让其映射为以驼峰命名的字段, 比如my_test映射为myTest  

```xml
<settings>
    <setting name="mapUnderscoreToCamelCase" value="true"/>
</settings>
```

默认为不开启, 也就是默认需要名称保持一致

条件查询, 那么肯定需要我们传入查询条件, 比如现在我们想通过sid字段来通过学号查找信息:  

```java
Student getStudentBySid(int sid);
```

```xml
<select id="getStudentBySid" parameterType="int" resultType="Student">
    select * from student where sid = #{sid}
</select>
```

插入 更新和删除操作, 与查询操作差不多, 不过需要使用对应的标签:

```java
int addStudent(Student student);
```

```xml
<insert id="addStudent" parameterType="Student">
    insert into student(name, sex) values(#{name}, #{sex})
</insert>
```

## 复杂查询

一个老师可以教授多个学生, 如何一次性将老师的学生全部映射给此老师的对象? 比如:

```java
@Data
public class Teacher {
    int tid;
    String name;
    List<Student> studentList;
}
```

这种一对多的查询, 就需要进行复杂查询了

现在需要resultMap来自定映射规则

```xml
<select id="getTeacherByTid" resultMap="asTeacher">
        select *, teacher.name as tname from student inner join teach on student.sid = teach.sid
                              inner join teacher on teach.tid = teacher.tid where teach.tid = #{tid}
</select>

<resultMap id="asTeacher" type="Teacher">
    <id column="tid" property="tid"/>
    <result column="tname" property="name"/>
    <collection property="studentList" ofType="Student">
        <id property="sid" column="sid"/>
        <result column="name" property="name"/>
        <result column="sex" property="sex"/>
    </collection>
</resultMap>
```

多对一如何查询? 每个学生都有一个班主任 同时修改一些teacher类

查询到每个Student对象时候都带上班主任, 也可以使用resultMap来自定实现

```java
@Data
@Accessors(chain = true)
public class Student {
    private int sid;
    private String name;
    private String sex;
    private Teacher teacher;
}
```

通过使用association进行关联, 形成多对一的关系, 实际上和一对多同理

```xml
<resultMap id="test2" type="Student">
    <id column="sid" property="sid"/>
    <result column="name" property="name"/>
    <result column="sex" property="sex"/>
    <association property="teacher" javaType="Teacher">
        <id column="tid" property="tid"/>
        <result column="tname" property="name"/>
    </association>
</resultMap>
<select id="selectStudent" resultMap="test2">
    select *, teacher.name as tname from student left join teach on student.sid = teach.sid
                                                 left join teacher on teach.tid = teacher.tid
</select>
```

## 事务操作

可以在获取SqlSession关闭自动提交来开启事务模式

```java
try (SqlSession sqlSession = MybatisUtil.getSession(false)){
    TestMapper testMapper = sqlSession.getMapper(TestMapper.class);

    testMapper.addStudent(new Student().setSex("男").setName("小王"));

    testMapper.selectStudent().forEach(System.out::println);
    sqlSession.rollback();
    sqlSession.commit();
}
```

rollback后将不会提交 如果注释掉这行代码则可以提交

## 动态sql

MyBatis的强大特性之一便是它的动态 SQL

利用动态sql可以避免 拼接sql语句的麻烦事

MyBatis提供了很多元素

- if
- choose(when, otherwise)
- trim(where, set)
- foreach

### if

动态sql通常要做的事情事有条件地包含where子句的一部分, 比如:

```xml
<select id="findActiveBlogWithTitleLike"
     resultType="Blog">
  SELECT * FROM BLOG 
  WHERE state = ‘ACTIVE’ 
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
```

这条语句提供了一个可选的文本查找类型的功能. 如果没有传入"title", 那么所有处于"ACTIVE"状态的BLOG都会返回; 反之若传入了"title", 那么就会把模糊查找"title"内容的BLOG结果返回

### choose、when、otherwise

有时候, 不想使用所有的条件，而只是想从多个条件中选择一个使用. 针对这种情况,MyBatis 提供了 choose 元素, 它有点像 Java 中的 switch 语句.

还是上面的例子, 但是策略变为：传入了 “title” 就按 “title” 查找, 传入了 “author” 就按 “author” 查找. 若两者都没有传入, 就返回标记为 featured 的 BLOG

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = ‘ACTIVE’
  <choose>
    <when test="title != null">
      AND title like #{title}
    </when>
    <when test="author != null and author.name != null">
      AND author_name like #{author.name}
    </when>
    <otherwise>
      AND featured = 1
    </otherwise>
  </choose>
</select>
```

## 缓存

MyBatis 内置了一个强大的事务性查询缓存机制, 它可以非常方便地配置和定制

默认情况下, 只启用了本地的会话缓存, 它仅仅对一个会话中的数据进行缓存

要启用全局的二级缓存, 需要在 SQL 映射文件中添加一行：

```xml
<cache/>
```

这个简单语句的效果如下:

- 映射语句文件中的所有 select 语句的结果将会被缓存
- 映射语句文件中的所有 insert  update 和 delete 语句会刷新缓存
- 缓存会使用最近最少使用算法(LRU, Least Recently Used)算法来清除不需要的缓存
- 缓存不会定时进行刷新(也就是说，没有刷新间隔)
- 缓存会保存列表或对象(无论查询方法返回哪种)的 1024 个引用
- 缓存会被视为读/写缓存, 这意味着获取到的对象并不是共享的, 可以安全地被调用者修改, 而不干扰其他调用者或线程所做的潜在修改

这些属性可以通过 cache 元素的属性来修改. 比如：

```xml
<cache
  eviction="FIFO"
  flushInterval="60000"
  size="512"
  readOnly="true"/>
```

可用的清除策略有：

- LRU – 最近最少使用：移除最长时间不被使用的对象
- FIFO – 先进先出：按对象进入缓存的顺序来移除它们
- SOFT – 软引用：基于垃圾回收器状态和软引用规则移除对象
- WEAK – 弱引用：更积极地基于垃圾收集器状态和弱引用规则移除对象

**提示** 二级缓存是事务性的.这意味着, 当 SqlSession 完成并提交时, 或是完成并回滚, 但没有执行 flushCache=true 的 insert/delete/update 语句时, 缓存会获得更新

配置sql语句时候, 属性 useCache="false" 可以用来关闭二级缓存, flushCache="true" 是没执行一次这条sql语句强制清空缓存. 可以理解成为true的话关闭一级缓存.

## 使用注解

### 使用注解来实现Insert, 每个操作都有一个对应的注解

```java
@Insert("insert into student(name, sex) values(#{name}, #{sex})")
int addStudent(Student student);
```

同时修改配置文件中的映射器注册:

```xml
<mappers>
    <mapper class="com.test.mapper.MyMapper"/>
    <!--  也可以直接注册整个包下的 <package name="com.test.mapper"/>  -->
</mappers>
```

通过直接指定Class, 来让Mybatis知道这里有一个通过注解实现的映射器

### 自定义映射规则

直接通过@Results注解, 就可以直接进行配置了, 此注解的value是一个@Result注解数组, 每个@Result注解都相当于一个单独的字段配置, 和之前xml中很相似.

```java
@Results({
        @Result(id = true, column = "sid", property = "sid"),
        @Result(column = "sex", property = "name"),
        @Result(column = "name", property = "sex")
})
@Select("select * from student")
List<Student> getAllStudent();
```

### 完成复杂查询

还是一个老师教多个学生:

```java
@Results({
        @Result(id = true, column = "tid", property = "tid"),
        @Result(column = "name", property = "name"),
        @Result(column = "tid", property = "studentList", many =
            @Many(select = "getStudentByTid")
        )
})
@Select("select * from teacher where tid = #{tid}")
Teacher getTeacherBySid(int tid);

@Select("select * from student inner join teach on student.sid = teach.sid where tid = #{tid}")
List<Student> getStudentByTid(int tid);
```

这里多了一个子查询, 作用是单独查询符合条件的学生, 并将其结果作为@Result注解的一个many结果, 代表子查询的所有结果都放入此集合中(也就是之前的collection)

同时, @Result也提供了@One子注解来实现一对多的关系表示, 类似于之前的assocation标签：

```java
@Results({
        @Result(id = true, column = "sid", property = "sid"),
        @Result(column = "sex", property = "name"),
        @Result(column = "name", property = "sex"),
        @Result(column = "sid", property = "teacher", one =
            @One(select = "getTeacherBySid")
        )
})
@Select("select * from student")
List<Student> getAllStudent();
```

### 使用注解编写sql, 但映射规则由xml实现

这里提供了@ResultMap注解, 直接指定ID即可

```java
@ResultMap("test")
@Select("select * from student")
List<Student> getAllStudent();
```

### 多个构造方法情况

如果出现之前的两个构造方法的情况, 且没有任何一个构造方法匹配的话, 如何处理

提供了@ConstructorArgs注解来指定构造方法

```java
@ConstructorArgs({
        @Arg(column = "sid", javaType = int.class),
        @Arg(column = "name", javaType = String.class)
})
@Select("select * from student where sid = #{sid} and sex = #{sex}")
Student getStudentBySidAndSex(@Param("sid") int sid, @Param("sex") String sex);
```

得到的结果和使用constructor标签效果一致

### 多个参数报错情况

当参数列表中出现两个以上参数时, 会报错

```java
@Select("select * from student where sid = #{sid} and sex = #{sex}")
Student getStudentBySidAndSex(int sid, String sex);

### Error querying database.  Cause: org.apache.ibatis.binding.BindingException: Parameter 'sid' not found. Available parameters are [arg1, arg0, param1, param2]
```

原因是Mybatis不明确到底哪个参数是什么, 可以添加@Param来指定参数名称：

```java
@Select("select * from student where sid = #{sid} and sex = #{sex}")
Student getStudentBySidAndSex(@Param("sid") int sid, @Param("sex") String sex);
```

在参数 一个是基本类型, 一个是对象类型时又出现问题了

```java
@Insert("insert into student(sid, name, sex) values(#{sid}, #{name}, #{sex})")
int addStudent(@Param("sid") int sid, @Param("student")  Student student);


### SQL: insert into student(sid, name, sex) values(?, ?, ?)
### Cause: org.apache.ibatis.binding.BindingException: Parameter 'name' not found. Available parameters are [student, param1, sid, param2]
```

原因是MyBatis不能明确这些属性是从哪里来的

这里使用 {参数名.属性名} 让MyBatis知道要用的是哪个属性

```java
@Insert("insert into student(sid, name, sex) values(#{sid}, #{student.name}, #{student.sex})")
int addStudent(@Param("sid") int sid, @Param("student")  Student student);
```

### 控制缓存机制

使用@CacheNamespace直接定义在接口上即可, 然后可以通过使用@Options来控制单个操作的缓存启用

```java
@CacheNamespace(readWrite = false)
public interface MyMapper {

    @Select("select * from student")
    @Options(useCache = false)
    List<Student> getAllStudent();
}
```
