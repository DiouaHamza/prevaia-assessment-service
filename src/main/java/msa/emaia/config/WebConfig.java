package msa.emaia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${api.base-path:/v1}")
    private String apiBasePath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Serve all requests to index.html except those starting with /v1
        registry.addViewController("/{path:[A-Za-z0-9\\-\\/]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/suppliers/{path:[A-Za-z0-9\\-\\/]+}")
                .setViewName("forward:/index.html");
    }

    /*@Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}/**")
                .setViewName("forward:/index.html");
    }*/


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");

    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setPatternParser(new PathPatternParser());
        configurer.addPathPrefix(apiBasePath, c -> c.isAnnotationPresent(RestController.class));
    }
}
