package quizmaster.quiz.auth.Seguranca.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import quizmaster.quiz.auth.Servico.usuDetImpSer;

public class authFilterToken extends OncePerRequestFilter {

    @Autowired
    private utilJwt utilJwt;

    @Autowired
    private usuDetImpSer usuDetImpSer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getToken(request);

            if (jwt != null && utilJwt.validarToken(jwt)) {

                String username = utilJwt.getUsernameToken(jwt);
                UserDetails UserDetails = usuDetImpSer.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(UserDetails, null,
                        UserDetails.getAuthorities());

            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }


        } catch (Exception e) {

                    System.out.println("Erro a processar o token");
        } finally {

        }
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {

        String headerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(headerToken) && headerToken.startsWith("Bearer")) {

            return headerToken.replace("Bearer ", "");
        }
        return null;
    }

}

