/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.DigestAuthenticator;
import static org.eclipse.jetty.security.authentication.FormAuthenticator.__J_POST;
import static org.eclipse.jetty.security.authentication.FormAuthenticator.__J_URI;
import org.eclipse.jetty.security.authentication.SessionAuthentication;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author akhikhl
 */
class SSODigestAuthenticator extends DigestAuthenticator {

    private static final Logger LOG = LoggerFactory.getLogger(SSOBasicAuthenticator.class);

    // "login" is copied without changes from FormAuthenticator
    @Override
    public UserIdentity login(String username, Object password, ServletRequest request)
    {

        UserIdentity user = super.login(username,password,request);
        if (user!=null)
        {
            HttpSession session = ((HttpServletRequest)request).getSession(true);
            Authentication cached=new SessionAuthentication(getAuthMethod(),user,password);
            session.setAttribute(SessionAuthentication.__J_AUTHENTICATED, cached);
        }
        return user;
    }

    @Override
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        HttpServletRequest request = (HttpServletRequest)req;

        if (!mandatory)
            return new DeferredAuthentication(this);

        // ++ copied from FormAuthenticator

        HttpSession session = request.getSession(true);

        // Look for cached authentication
        Authentication authentication = (Authentication) session.getAttribute(SessionAuthentication.__J_AUTHENTICATED);
        if (authentication != null)
        {
            // Has authentication been revoked?
            if (authentication instanceof Authentication.User &&
                _loginService!=null &&
                !_loginService.validate(((Authentication.User)authentication).getUserIdentity()))
            {

                session.removeAttribute(SessionAuthentication.__J_AUTHENTICATED);
            }
            else
            {
                String j_uri=(String)session.getAttribute(__J_URI);
                if (j_uri!=null)
                {
                    MultiMap<String> j_post = (MultiMap<String>)session.getAttribute(__J_POST);
                    if (j_post!=null)
                    {
                        StringBuffer buf = request.getRequestURL();
                        if (request.getQueryString() != null)
                            buf.append("?").append(request.getQueryString());

                        if (j_uri.equals(buf.toString()))
                        {
                            // This is a retry of an original POST request
                            // so restore method and parameters

                            session.removeAttribute(__J_POST);
                            Request base_request = (req instanceof Request)?(Request)req:AbstractHttpConnection.getCurrentConnection().getRequest();
                            base_request.setMethod(HttpMethods.POST);
                            base_request.setParameters(j_post);
                        }
                    }
                    else
                        session.removeAttribute(__J_URI);

                }
                return authentication;
            }
        }
        // -- copied from FormAuthenticator

        return super.validateRequest(req, res, mandatory);
    }
}
