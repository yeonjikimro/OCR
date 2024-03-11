package com.example.demo.config;

import java.util.Arrays;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean filterBean() {
    
      FilterRegistrationBean registrationBean = new FilterRegistrationBean();
      registrationBean.setOrder(Integer.MIN_VALUE); //필터 여러개 적용 시 순번
//        registrationBean.addUrlPatterns("/*"); //전체 URL 포함
//        registrationBean.addUrlPatterns("/test/*"); //특정 URL 포함
//        registrationBean.setUrlPatterns(Arrays.asList(INCLUDE_PATHS)); //여러 특정 URL 포함
      registrationBean.setUrlPatterns(Arrays.asList("/autoAudit/*")); 
      // registrationBean.setEnabled(false);

      return registrationBean;
  }
}
