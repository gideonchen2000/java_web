# cookie&session

## cookie

cookie可以在浏览器中保存一些信息, 并且在下次请求时, 请求头中会携带这些信息.

如何设置一个cookie:

```java
Cookie cookie = new Cookie("test", "yyds");
resp.addCookie(cookie);
```

在HttpServletResponse中添加cookie后, 浏览器的响应头中会包含一个 Set-Cookie 属性. 同时, 在重定向后, 请求头中也会携带此cookie作为一个属性

我们可以通过HttpServletRequest快速获取有哪些cookie信息.

```java
for (Cookie cookie : req.getCookies()) {
    System.out.println(cookie.getName() + ": " + cookie.getValue());
}
```

### cookie中包含哪些信息

- name : cookie的名称, 一旦创建, 不可更改
- value : cookie的值
- maxAge : cookie时效的时间, 单位 秒. 如果为负数, 该cookie为临时cookie, 关闭浏览器即失效, 浏览器也不会以任何形式保存该cookie. 如果为0, 表示删除该cookie. maxAge默认值为 -1 .
- secure : 该cookie是否仅被使用安全协议传输. 安全协议有https, SSL等, 在网络上传输数据之前先将数据加密. 默认为false.
- path : cookie的使用路径. 如果设置为"/sessionWeb/", 则只有contextPath为 "/sessionWeb" 的程序可以访问该Cookie. 如果设置为"/"，则本域名下contextPath都可以访问该Cookie. **注意**最后一个字符必须为 "/" .
- domain : 可以访问该cookie的域名. 如果设置为".google.com", 则所有以"google.com"结尾的域名都可以访问该Cookie. 注意第一个字符必须为 "."
- comment : 说明, 浏览器显示cookie信息的时候显示该说明
- version : cookie使用的版本号

### cookie实现 "记住我"

可以通过使用cookie来实现 "记住我" 功能.

可以将用户名和密码全部保存在cookie中, 如果访问首页时携带了相应的cookie, 就可以直接为用户进行登录. 登陆成功则跳转到首页, 失败则清理浏览器中的cookie

**首先**前端需要有一个勾选框:

```html
<div>
    <label>
        <input type="checkbox" placeholder="记住我" name="remember-me">
        记住我
    </label>
</div>
```

接下来, 在登陆成功时进行判断, 如果勾选了"记住我",那么就将cookie存到本地:

```java
if(map.containsKey("remember-me")) { 
    Cookie cookie_username = new Cookie("username", username);
    cookie_username.setMaxAge(30);
    Cookie cookie_password = new Cookie("password", password);
    cookie_password.setMaxAge(30);
    resp.addCookie(cookie_username);
    resp.addCookie(cookie_password);
}
```

再设置一下doGet请求方法

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Cookie[] cookies = req.getCookies();
    if(cookies != null){
        String username = null;
        String password = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("username")) username = cookie.getValue();
            if(cookie.getName().equals("password")) password = cookie.getValue();
        }
        if(username != null && password != null){
            //登陆校验
            try (SqlSession sqlSession = factory.openSession(true)){
                UserMapper mapper = sqlSession.getMapper(UserMapper.class);
                User user = mapper.getUser(username, password);
                if(user != null){
                    resp.sendRedirect("time");
                    return;   //直接返回
                }else{
                    Cookie cookie_username = new Cookie("username", username);
                    cookie_username.setMaxAge(0);
                    Cookie cookie_password = new Cookie("password", password);
                    cookie_password.setMaxAge(0);
                    resp.addCookie(cookie_username);
                    resp.addCookie(cookie_password);
                }
            }
        }
    }
    req.getRequestDispatcher("/").forward(req, resp);   //正常情况还是转发给默认的Servlet 返回静态页面
}
```

现在, 30s不需要重新登录, 访问登陆页面后会直接跳转到time页面

另外, 现在的页面, 无论是否登录, 所有人只要知道域名就可以直接访问, 要实现只有登陆成功才能访问, 就需要用到session了.

## session

由于http是无连接的, 那么如何能够辨别当前的请求是哪个用户发起的呢?

session就是用来解决这种问题的, 每个用户的会话都会有一个自己的session对象, 来自同一个浏览器的所有请求, 就属于同一个会话.

session实际上也是基于cookie实现的, 前面了解到 服务端可以将cookie保存到浏览器, 当浏览器下次访问服务端时就会附带这些cookie信息.

session也利用了这一点, 他会给浏览器一个名为`JSESSIONID`的cookie, 值是一个随机数, 而此cookie就对应了属于哪一个会话.

只要浏览器携带此cookie访问服务器, 服务器就会通过cookie的值进行辨别, 得到对应的session对象, 这样就可以知道是哪个浏览器在访问服务器

现在, 用户登陆成功后, 将用户对象添加到session中, 只要是此用户发起的请求, 都可以从HttpSession中读取到 存储在会话中的数据.

```java
HttpSession session = req.getSession();
session.setAttribute("user", user);
```

同时, 如果用户没有登陆就去访问页面, 那么将发送一个重定向请求, 告诉用户, 需要先进行登陆才可以访问.

```java
HttpSession session = req.getSession();
User user = (User) session.getAttribute("user");
if(user == null) {
    resp.sendRedirect("login");
    return;
}
```

session也是有过期时间的, 默认是30分钟, 可以在配置文件中修改.

```xml
<session-config>
    <session-timeout>1</session-timeout>
</session-config>
```

代码中可以使用`session.invalidate();`来让session立即失效
