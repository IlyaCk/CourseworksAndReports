package com.example.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (client != null) {
                OAuth2AccessToken accessToken = client.getAccessToken();
                Set<String> grantedScopes = accessToken.getScopes();

                Set<String> requiredScopes = Set.of(
                        "openid",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/classroom.courses.readonly",
                        "https://www.googleapis.com/auth/classroom.student-submissions.students.readonly",
                        "https://www.googleapis.com/auth/classroom.profile.emails",
                        "https://www.googleapis.com/auth/classroom.rosters.readonly",
                        "https://www.googleapis.com/auth/drive.readonly"
                );

                if (!grantedScopes.containsAll(requiredScopes)) {
                    response.sendRedirect("/api/auth/logout?login=insufficient_scopes");
                    return;
                }
            }
        }

        response.sendRedirect(frontendUrl+ "?login=success");
    }
}

