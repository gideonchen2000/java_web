# Filter & Listener

## Filter

session可以很好地控制用户的登陆验证, 只有授权的用户, 才可以访问一些页面, 但是我们需要一个一个去进行配置, 还是太复杂, 过滤器可以一次性地过滤掉没有登录验证的用户

来自浏览器的所有访问请求都会首先经过过滤器, 只有过滤器允许通过的请求, 才可以顺利地到达对应的Servlet, 不允许通过的请求, 可以自由地进行控制 是否进行重定向或是请求转发. 过滤器可以添加很多个.

添加一个过滤器只需要实现Filter接口, 并添加@WebFilter注解即可:

```java
@WebFilter("/*")   //路径的匹配规则和Servlet一致, 这里表示匹配所有请求
public class TestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        System.out.println(request.getRequestURL());
        // filterChain.doFilter(servletRequest, servletResponse);
    }
}
```

成功添加了一个过滤器, 并添加一条打印语句

结果发现, 发起的所有请求, 一律需要经过此过滤器, 并且所有的请求都没有任何响应的内容.

那么为了让请求可以顺利到达对应的Servlet, 只需要在最后添加一句`filterChain.doFilter(servletRequest, servletResponse);`

这个代码的意思是, 将此请求继续传递给下一个过滤器, 当没有下一个过滤器时, 就会到达对应的Servlet进行处理

和Servlet一样, Filter也有对应的HttpFilter专用类, 我们可以直接使用HttpFilter来编写

现在, 给应用程序添加一个过滤器: 在用户未登录情况下, 只允许静态资源和登陆页面请求通过, 登录之后都可以.

```java
@WebFilter("/*")
public class MainFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String url = req.getRequestURL().toString();
        //判断是否为静态资源
        if(!url.endsWith(".js") && !url.endsWith(".css") && !url.endsWith(".png")){
            HttpSession session = req.getSession();
            User user = (User)session.getAttribute("user");
            //判断是否未登陆
            if(user == null && !url.endsWith("login")){
                res.sendRedirect("login");
                return;
            }
        }
        //交给过滤链处理
        chain.doFilter(req, res);
    }
}
```

## Listener

监听器简单了解即可.

如果想在程序加载时, 或是Session创建时, 亦或者时在Request对象创建的时候进行一些操作, 这个情况下可以使用监听器来实现

eg: 监听Session的创建, 并输出一句话

```java
@WebListener
public class TestListener implements HttpSessionListener {
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("有一个Session被创建了");
    }
}
```
