package com.example.demodatn2.config;

import com.example.demodatn2.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/css/**", "/js/**", "/images/**", "/api/danh-muc/**", "/api/chatbot", "/login", "/register", "/logout", "/", "/index", "/403", "/products/**", "/cart/**", "/order/checkout", "/order/success", "/payment/**", "/chinh-sach-doi-tra", "/api/sepay/webhook", "/api/order/status/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/products/**")
            .addResourceLocations("classpath:/static/images/products/");

        registry.addResourceHandler("/images/returns/**")
            .addResourceLocations("classpath:/static/images/returns/");

        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/");

        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");

        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");
    }


    
}
