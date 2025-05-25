package com.example.javabackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtTokenValidator tokenValidator;
    private final ObjectMapper objectMapper;
    private final CognitoUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenValidator tokenValidator,
            ObjectMapper objectMapper,
            CognitoUserDetailsService userDetailsService) {
        this.tokenValidator = tokenValidator;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        logger.debug("Received request to: {} with Authorization header: {}", request.getRequestURI(), authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                logger.debug("Validating token for request: {}", request.getRequestURI());
                String username = tokenValidator.getUsernameFromToken(token);
                List<String> groups = tokenValidator.getGroupsFromToken(token);
                
                logger.debug("Token validated successfully for user: {} with groups: {}", username, groups);
                var userDetails = userDetailsService.createUserDetails(username, groups, token);
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authentication set in SecurityContext");
            } catch (ParseException e) {
                logger.error("Token parsing failed: {}", e.getMessage());
                handleError(response, "Invalid token: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                logger.error("Token validation failed: {}", e.getMessage(), e);
                handleError(response, "Token validation failed: " + e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            logger.warn("No valid Authorization header found for request: {}", request.getRequestURI());
            handleError(response, "No valid Authorization header found", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        
        objectMapper.writeValue(response.getWriter(), error);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean shouldNotFilter = path.startsWith("/api/public/") || path.equals("/error");
        logger.debug("Checking if should filter path: {} - result: {}", path, shouldNotFilter);
        return shouldNotFilter;
    }
} 