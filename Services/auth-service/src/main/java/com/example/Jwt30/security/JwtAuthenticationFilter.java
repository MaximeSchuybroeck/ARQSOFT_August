package com.example.Jwt30.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1.get token
        String requestHeader=request.getHeader("Authorization");
        logger.info("Header :{}", requestHeader);
        //bearer
        String userName=null;
        String token=null;

        if (requestHeader!=null && requestHeader.startsWith("Bearer")){
            token =  requestHeader.substring(7);
            try {
                userName = this.jwtHelper.extractUsername(token);
            }catch (IllegalArgumentException e){
                System.out.println("Unable to get jwt Token");
            }catch (ExpiredJwtException e){
                System.out.println("jwt token has expired");
            }catch (MalformedJwtException e){
                System.out.println("Invaild Jwt token");
            }
        }else {
            System.out.println("Token does not start with Bearer");
        }
        //Get Token Validate
        if (userName != null && SecurityContextHolder.getContext().getAuthentication()==null)  {
            UserDetails userDetails= this.userDetailsService.loadUserByUsername(userName);
            if (this.jwtHelper.validateToken(token,userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            }else {
                System.out.println("Invaild Jwt token");
            }

        }else{
            System.out.println("UserName is NUll");
        }
        filterChain.doFilter(request,response);
    }
}
