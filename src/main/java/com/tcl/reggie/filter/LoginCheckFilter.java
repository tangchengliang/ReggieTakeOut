package com.tcl.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.tcl.reggie.common.BaseContext;
import com.tcl.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器，匹配**通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        /**
         * 1、获取本次请求的URI
         * 2、判断本次请求是否需要处理
         * 3、如果不需要处理，则直接放行
         * 4、判断登录状态，如果已登录，则直接放行
         * 5、如果未登录则返回未登录结果
         */
        // 1、获取本次请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截请求：{}", requestURI);
        // 定义不拦截的路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", //  移动端发送短信
                "/user/login",    // 移动端登录
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        // 2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        // 3、如果不需要处理，则直接放行
        if(check){
            log.info("不需要处理：{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 4-1、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("employee")!=null){
            log.info("已经登录：{}", requestURI);

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        // 4-2、判断移动端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user")!=null){
            log.info("已经登录：{}", requestURI);

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        // 5、如果未登录则返回未登录结果
        // 配合index页面，使用输出流的方式向客户端页面响应数据
        log.info("用户未登录：{}", requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
