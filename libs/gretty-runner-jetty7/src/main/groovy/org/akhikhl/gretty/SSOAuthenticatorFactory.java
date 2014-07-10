/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty;

import javax.servlet.ServletContext;
import org.eclipse.jetty.security.Authenticator.AuthConfiguration;
import org.eclipse.jetty.security.DefaultAuthenticatorFactory;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.security.Constraint;

/**
 *
 * @author akhikhl
 */
public class SSOAuthenticatorFactory extends DefaultAuthenticatorFactory {

  @Override
  public org.eclipse.jetty.security.Authenticator getAuthenticator(Server server, ServletContext ctx, AuthConfiguration configuration, IdentityService identityService, LoginService loginService) {
    String auth = configuration.getAuthMethod();
    if (auth==null || Constraint.__BASIC_AUTH.equalsIgnoreCase(auth))
      return new SSOBasicAuthenticator();
    if (Constraint.__DIGEST_AUTH.equalsIgnoreCase(auth))
      return new SSODigestAuthenticator();
    if ( Constraint.__SPNEGO_AUTH.equalsIgnoreCase(auth) )
      return new SSOSpnegoAuthenticator();
    if ( Constraint.__NEGOTIATE_AUTH.equalsIgnoreCase(auth) ) // see Bug #377076
      return new SSOSpnegoAuthenticator(Constraint.__NEGOTIATE_AUTH);
    if (Constraint.__CERT_AUTH.equalsIgnoreCase(auth)||Constraint.__CERT_AUTH2.equalsIgnoreCase(auth))
      return new SSOClientCertAuthenticator();
    return super.getAuthenticator(server, ctx, configuration, identityService, loginService);
  }
}
