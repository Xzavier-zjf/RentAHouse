
package com.rental.user.config;

import com.rental.user.interceptor.AdminAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAccessInterceptor adminAccessInterceptor;

    public WebMvcConfig(AdminAccessInterceptor interceptor) {
        this.adminAccessInterceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAccessInterceptor).addPathPatterns("/api/admin/**");
    }
}
