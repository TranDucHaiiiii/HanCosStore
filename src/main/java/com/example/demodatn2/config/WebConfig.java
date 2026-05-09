package com.example.demodatn2.config;

import com.example.demodatn2.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

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
        // Map /images/products/** to the actual directory on disk
        Path productDir = Paths.get("src/main/resources/static/images/products/");
        String productPath = productDir.toFile().getAbsolutePath();
        
        if (!productPath.endsWith("/")) {
            productPath += "/";
        }

        registry.addResourceHandler("/images/products/**")
                .addResourceLocations("file:///" + productPath);

        Path returnDir = Paths.get("src/main/resources/static/images/returns/");
        String returnPath = returnDir.toFile().getAbsolutePath();

        if (!returnPath.endsWith("/")) {
            returnPath += "/";
        }

        registry.addResourceHandler("/images/returns/**")
                .addResourceLocations("file:///" + returnPath);
    }


    
}
