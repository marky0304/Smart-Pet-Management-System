package com.pet.config;

import com.pet.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 转为绝对路径，确保静态资源能正确访问
        String absolutePath = new File(uploadPath).getAbsolutePath();
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/auth/**",           // 认证相关接口（含发送验证码、重置密码）
                    "/login/**",          // 登录相关
                    "/uploads/**",        // 静态资源
                    "/error",             // 错误页面
                    "/favicon.ico",       // 图标
                    "/community/topics/hot",     // 热门话题（允许匿名访问）
                    "/community/posts/hot",      // 热门动态（允许匿名访问）
                    "/product/list",             // 商品列表（允许匿名访问）
                    "/product/{id}",              // 商品详情（允许匿名访问）
                    "/product/hot",              // 热销商品（允许匿名访问）
                    "/product/new",              // 新品推荐（允许匿名访问）
                    "/product/search",             // 商品搜索（允许匿名访问）
                    "/product/searchByCategory", // Agent商品搜索（允许匿名访问）
                    "/product-review/product/**", // 商品评价（允许匿名查看）
                    "/service/list",             // 服务列表（允许匿名访问）
                    "/order/createByAgent",      // Agent下单（允许匿名访问）
                    "/test/**",                  // 测试接口
                    "/health",                   // 系统健康检查
                    "/ws/**"                     // WebSocket
                );
    }
}