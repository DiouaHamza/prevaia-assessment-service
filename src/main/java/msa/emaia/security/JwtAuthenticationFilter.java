package msa.emaia.security;

import com.preveaia.commons.jwt.JwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import msa.emaia.client.iam.IamFeignClient;
import msa.emaia.client.iam.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${emaia.security.jwt.secret-key}")
    private String jwtSecret;

    @Autowired
    private IamFeignClient iamFeignClient;

    private JwtValidator jwtValidator;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        if (jwtValidator == null) {
            jwtValidator = new JwtValidator(jwtSecret);
        }

        final String authHeader = request.getHeader("Authorization");
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            final String userEmail = jwtValidator.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {
                // Feign call to load user details
                UserDto userDetails = iamFeignClient.getUserByEmail(userEmail);

                if (userDetails == null) {
                    log.warn("[JwtAuthenticationFilter] iam-service returned no user for email {} ({} {})", userEmail, request.getMethod(), request.getRequestURI());
                } else if (jwtValidator.isTokenValid(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("[JwtAuthenticationFilter] token failed validation for {} ({} {})", userEmail, request.getMethod(), request.getRequestURI());
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("[JwtAuthenticationFilter] request rejected for {} {}: {}", request.getMethod(), request.getRequestURI(), e.toString());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
