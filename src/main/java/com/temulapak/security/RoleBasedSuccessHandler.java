package com.temulapak.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Setelah login, arahkan ke halaman yang semula dituju (jika sesuai peran),
 * atau ke dashboard sesuai peran.
 */
@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Role role = ((AppUserDetails) authentication.getPrincipal()).getRole();

        SavedRequest saved = requestCache.getRequest(request, response);
        if (saved != null) {
            requestCache.removeRequest(request, response);
            String url = saved.getRedirectUrl();
            try {
                String path = URI.create(url).getPath();
                String ctx = request.getContextPath();
                if (path != null && path.startsWith(ctx + role.getPrefix() + "/")) {
                    response.sendRedirect(url);
                    return;
                }
            } catch (IllegalArgumentException ignored) {
                // gunakan dashboard bila URL tersimpan tidak valid
            }
        }
        response.sendRedirect(request.getContextPath() + role.getHome());
    }
}
