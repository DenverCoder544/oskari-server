package org.oskari.spring.security;

import fi.nls.oskari.control.ActionParameters;
import org.oskari.user.Role;
import org.oskari.user.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.user.MybatisUserService;
import org.oskari.log.AuditLog;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Common helper methods needed for both SAML and DB authentication
 */
public class OskariUserHelper {

    private static Logger log = LogFactory.getLogger(OskariUserHelper.class);
    private MybatisUserService userService = new MybatisUserService();

    /**
     * Common code done for SAML and DB authentication on successful login
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws jakarta.servlet.ServletException
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException,
            jakarta.servlet.ServletException {
        onAuthenticationSuccess(request, response, authentication.getPrincipal().toString());
    }
    /**
     * Common code done for SAML and DB authentication on successful login
     * @param request
     * @param response
     * @param username
     * @throws IOException
     * @throws jakarta.servlet.ServletException
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, String username)
            throws IOException,
            jakarta.servlet.ServletException {
        log.debug("Auth success");
        // setup user object in session for Oskari
        setupSession(request, username);
        log.info("Auth success and session setup complete");
    }

    /**
     * Tries to setup Oskari user information to session based on given username.
     * @param httpRequest
     * @param username
     */
    private void setupSession(final HttpServletRequest httpRequest, final String username)  {
        final User user = getLoggedInUser(httpRequest);
        if(user != null && !user.isGuest()) {
            // user is already logged in
            return;
        }
        log.debug("Getting user from service with principal name:", username);
        try {
            User loadedUser = UserService.getInstance().getUser(username);
            log.debug("Got user from service:", loadedUser);
            if(loadedUser != null) {
                httpRequest.getSession(true).setAttribute(User.class.getName(), loadedUser);

                AuditLog.user(ActionParameters.getClientIp(httpRequest), loadedUser)
                        .withMsg("Login")
                        .updated(AuditLog.ResourceType.USER);

                // update last login
                User userToUpdate = UserService.getInstance().getUser(username);
                userToUpdate.setLastLogin(OffsetDateTime.now());
                userService.updateUser(userToUpdate);
            }
            else {
                log.error("Login user check failed! Got user from principal, but can't find it in Oskari db:", username);
            }
        } catch (Exception e) {
            log.error(e, "Session setup failed");
        }
    }

    /**
     * Returns the user object from session without creating a new session
     * if one is not available. Returns null if user or session does not exist.
     * @param httpRequest
     * @return
     */
    private User getLoggedInUser(final HttpServletRequest httpRequest) {
        final HttpSession session = httpRequest.getSession(false);
        if(session != null) {
            return (User) session.getAttribute(User.class.getName());
        }
        return null;
    }

    /**
     * Wraps Oskari roles as a List of Spring security GrantedAuthorities
     * @param roles
     * @return
     */
    public static List<GrantedAuthority> getRoles(Set<Role> roles) {
        final List<GrantedAuthority> grantedAuths = new ArrayList<>();
        if(roles == null) {
            return grantedAuths;
        }
        for(Role role : roles) {
            grantedAuths.add(new SimpleGrantedAuthority(role.getName()));
        }
        return grantedAuths;
    }
}
