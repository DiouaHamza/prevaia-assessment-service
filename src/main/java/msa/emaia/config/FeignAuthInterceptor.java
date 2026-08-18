package msa.emaia.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Forwards the caller's Authorization header onto outgoing Feign calls (e.g.
 * IamFeignClient, CatalogFeignClient, Partner Feign clients), so downstream
 * services that require auth on their /v1/api/** routes don't reject
 * inter-service calls made on behalf of an already-authenticated request.
 */
@Configuration
public class FeignAuthInterceptor {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
            }
        };
    }
}
