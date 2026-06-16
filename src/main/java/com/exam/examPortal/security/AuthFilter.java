package com.exam.examPortal.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
public class AuthFilter implements Filter {
    @Autowired
    private JwtUtils jwtUtils;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String token = getAccessTokenFromCookie(req);

        if (token != null && jwtUtils.validateToken(token)) {
            // 1. Get claims
            Claims claims = jwtUtils.getClaimsFromToken(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class); // Extract the role

            // 2. Convert role to Spring Security Authority
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(role));

            // 3. Set authentication WITH authorities
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(email, null, authorities)
            );
            chain.doFilter(req, res);
        } else {
            String uri = req.getRequestURI();
            if (uri.contains("/user/login") || uri.contains("/admin/login") || uri.contains("/user/register")) {
                chain.doFilter(req, res);
            } else {
                res.sendRedirect("/user/login?error=SessionExpired");
            }
        }
    }
    private String getAccessTokenFromCookie(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}