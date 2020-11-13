package org.akhikhl.gretty

import org.eclipse.jetty.server.session.Session
import org.eclipse.jetty.server.session.SessionHandler

class SingleSignOnSessionHandler extends SessionHandler {

    @Override
    Session getSession(String id) {
        Session session = getLocalSession(id)
        if (session == null) {
            for (SessionHandler handler : getSessionIdManager().getSessionHandlers()) {

                if (handler == this || !(handler instanceof SingleSignOnSessionHandler)) {
                    continue
                }

                session = ((SingleSignOnSessionHandler) handler).getLocalSession(id)
                if (session != null) {
                    break
                }
            }
        }

        return session
    }

    private Session getLocalSession(String id) {
        return super.getSession(id)
    }
}
