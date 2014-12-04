package org.akhikhl.gretty

import groovy.servlet.ServletCategory
import groovy.util.Expando
import groovyx.net.http.URIBuilder
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import java.util.regex.Pattern
import javax.management.ObjectName
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class RedirectFilter implements Filter {

  private static enum FilterAction {
    CHAIN,
    REDIRECT,
    FORWARD
  }

  private static URI buildURI(destination, URI defaultURI) {
    if(destination instanceof URI)
      return destination
    if(destination instanceof URIBuilder)
      return destination.toURI()
    URI uri = new URI(destination.toString())
    URIBuilder builder = new URIBuilder(defaultURI.toString())
    if(uri.scheme)
      builder.setScheme(uri.scheme)
    if(uri.userInfo)
      builder.setUserInfo(uri.userInfo)
    if(uri.host)
      builder.setHost(uri.host)
    if(uri.port > 0)
      builder.setPort(uri.port)
    String path = uri.path
    if(path == null)
      path = defaultURI.path
    else if(!path.startsWith('/')) {
      if(!defaultURI.path.endsWith('/'))
        path = '/' + path
      path = defaultURI.path + path
    }
    builder.setPath(path)
    if(uri.query)
      builder.setQuery(uri.query)
    if(uri.fragment)
      builder.setFragment(uri.fragment)
    builder.toURI()
  }

  protected static final Logger log = LoggerFactory.getLogger(RedirectFilter)

  protected Integer httpPort
  protected Integer httpsPort
  protected File webappDir
  protected long configFileLastModified
  protected Object filtersLock = new Object()
  protected List filters = []

  RedirectFilter() {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    loadFilters()
    Map result = [ action: FilterAction.CHAIN ]
    def filterContext = new Expando()
    filterContext.httpPort = httpPort
    filterContext.httpsPort = httpsPort
    filterContext.log = log
    filterContext.request = req
    filterContext.response = resp
    def uriBuilder = new URIBuilder(req.getRequestURL().toString())
    uriBuilder.setRawQuery(req.getQueryString())
    filterContext.requestURI = uriBuilder.toURI()
    filterContext.contextPath = req.getContextPath()
    filterContext.webappDir = webappDir
    filterContext.redirect = { destination ->
      result.action = FilterAction.REDIRECT
      result.uri = buildURI(destination, filterContext.requestURI)
      log.trace 'rule redirects to: {}', result.uri
    }
    filterContext.forward = { destination ->
      result.action = FilterAction.FORWARD
      result.uri = buildURI(destination, filterContext.requestURI)
      log.trace 'rule forwards to: {}', result.uri
    }
    List filters = []
    synchronized (filtersLock) {
      for(def f in this.filters) {
        Closure closure = f.closure.rehydrate(filterContext, f.owner, f.thisObject)
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        filters.add([options: f.options, closure: closure])
      }
    }
    use(ServletCategory) {
      while(true) {
        filterContext.authority = filterContext.requestURI.authority
        filterContext.fragment = filterContext.requestURI.fragment
        filterContext.host = filterContext.requestURI.host
        filterContext.path = filterContext.requestURI.path
        filterContext.port = filterContext.requestURI.port
        filterContext.query = filterContext.requestURI.query
        filterContext.scheme = filterContext.requestURI.scheme
        filterContext.userInfo = filterContext.requestURI.userInfo
        filterContext.relPath = filterContext.path - filterContext.contextPath
        if(!matchFilter(result, filters, filterContext, req, resp))
          break
        if(result.action == FilterAction.CHAIN || result.action == FilterAction.REDIRECT)
          break
        filterContext.requestURI = result.uri
      }
    }
    log.trace 'doFilter result={}', result
    switch(result.action) {
      case FilterAction.CHAIN:
        chain.doFilter(req, resp)
        break
      case FilterAction.REDIRECT:
        resp.sendRedirect(result.uri.toString())
        break
      case FilterAction.FORWARD:
        String forwardPath = result.uri.schemeSpecificPart - '//' - result.uri.authority
        req.getRequestDispatcher(forwardPath).forward(req, resp)
        break
    }
  }

  private boolean matchFilter(Map result, List filters, filterContext, ServletRequest req, ServletResponse resp) {
    filters.find { filter ->
      def matches = [:]
      for(def option in filter.options) {
        boolean matchResult = false
        if (option.value instanceof String || option.value instanceof GString) {
          if (filterContext[option.key] == option.value)
            matchResult = true
        } else if (option.value instanceof Pattern) {
          def m = filterContext[option.key] =~ option.value
          if (m) {
            matches[option.key] = m[0].collect()
            matchResult = true
          }
        }
        if(!matchResult)
          return false
      }
      filter.closure.call(matches)
      return result.action != FilterAction.CHAIN // interrupt find, when not chaining
    }
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    if(config.servletContext.hasProperty('contextHandler')) {
      // jetty-specific
      def server = config.servletContext.contextHandler.server
      server.connectors.each { conn ->
        if(server.version.startsWith('7.') || server.version.startsWith('8.')) {
          if(conn.getClass().getName() == 'org.eclipse.jetty.server.bio.SocketConnector')
            httpPort = conn.port instanceof Integer ? conn.port : Integer.parseInt(conn.port.toString(), 8080)
          else if(conn.getClass().getName() == 'org.eclipse.jetty.server.ssl.SslSocketConnector')
            httpsPort = conn.port instanceof Integer ? conn.port : Integer.parseInt(conn.port.toString(), 8443)
        } else {
          if(conn.protocols.find { it.startsWith 'http/' } && !conn.protocols.find { it.startsWith 'ssl-http/' })
            httpPort = conn.port instanceof Integer ? conn.port : Integer.parseInt(conn.port.toString(), 8080)
          else if(conn.protocols.find { it.startsWith 'http/' } && conn.protocols.find { it.startsWith 'ssl-http/' })
            httpsPort = conn.port instanceof Integer ? conn.port : Integer.parseInt(conn.port.toString(), 8443)
        }
      }
    }
    else {
      // tomcat-specific
      def mbeans = java.lang.management.ManagementFactory.getPlatformMBeanServer()
      for(def connName in mbeans.queryNames(new ObjectName('Tomcat:type=Connector,*'), null)) {
        def conn = new GroovyMBean(mbeans, connName)
        if(conn.scheme == 'http')
          httpPort = conn.port
        else if(conn.scheme == 'https')
          httpsPort = conn.port
      }
    }
    log.debug 'found httpPort={}', httpPort
    log.debug 'found httpsPort={}', httpsPort
    webappDir = new File(config.getServletContext().getRealPath(''))
    loadFilters()
  }

  protected loadFilters() {
    List newFilters = []
    File configFile = new File(webappDir, 'WEB-INF/filter.groovy')
    if(configFile.exists()) {
      if(configFileLastModified == configFile.lastModified())
        return
      configFileLastModified = configFile.lastModified()
      Binding binding = new Binding()
      binding.filter = { Map options, Closure closure ->
        newFilters.add([options: options, closure: closure])
      }
      def importCustomizer = new ImportCustomizer()
      importCustomizer.addImport 'URIBuilder', 'groovyx.net.http.URIBuilder'
      def configuration = new CompilerConfiguration()
      configuration.addCompilationCustomizers(importCustomizer)
      def shell = new GroovyShell(this.getClass().getClassLoader(), binding, configuration)
      def script = shell.parse(configFile)
      script.run()
    }
    synchronized (filtersLock) {
      filters = newFilters
    }
  }
}
