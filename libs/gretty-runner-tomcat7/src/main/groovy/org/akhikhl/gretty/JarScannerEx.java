/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;

/**
 * This is modified version of class
 * org.apache.tomcat.util.scan.StandardJarScanner. Modification compared to
 * original version includes:
 * <ul>
 * <li>skipPatterns parameter, allowing to specify jar skip patterns.</li>
 * <li>virtualWebInfLibs parameter, allowing to specify which jars in
 * WEB-INF/lib are "virtual" and should be skipped.</li>
 * </ul>
 *
 * @author akhikhl
 */
public class JarScannerEx implements JarScanner {

  private static final Log log = LogFactory.getLog(JarScannerEx.class);

  private static final Set<String> defaultJarsToSkip = new HashSet<String>();

  protected final JarSkipPatterns skipPatterns;
  protected final Set virtualWebInfLibs;

  JarScannerEx(JarSkipPatterns skipPatterns, Set virtualWebInfLibs) {
    this.skipPatterns = skipPatterns;
    this.virtualWebInfLibs = virtualWebInfLibs;
  }

  /**
   * The string resources for this package.
   */
  private static final StringManager sm
          = StringManager.getManager(Constants.Package);

  static {
    String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
    if (jarList != null) {
      StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
      while (tokenizer.hasMoreElements()) {
        String token = tokenizer.nextToken().trim();
        if (token.length() > 0) {
          defaultJarsToSkip.add(token);
        }
      }
    }
  }

  /**
   * Controls the classpath scanning extension.
   */
  private boolean scanClassPath = true;

  public boolean isScanClassPath() {
    return scanClassPath;
  }

  public void setScanClassPath(boolean scanClassPath) {
    this.scanClassPath = scanClassPath;
  }

  /**
   * Controls the testing all files to see of they are JAR files extension.
   */
  private boolean scanAllFiles = false;

  public boolean isScanAllFiles() {
    return scanAllFiles;
  }

  public void setScanAllFiles(boolean scanAllFiles) {
    this.scanAllFiles = scanAllFiles;
  }

  /**
   * Controls the testing all directories to see of they are exploded JAR files
   * extension.
   */
  private boolean scanAllDirectories = false;

  public boolean isScanAllDirectories() {
    return scanAllDirectories;
  }

  public void setScanAllDirectories(boolean scanAllDirectories) {
    this.scanAllDirectories = scanAllDirectories;
  }

  /**
   * Controls the testing of the bootstrap classpath which consists of the
   * runtime classes provided by the JVM and any installed system extensions.
   */
  private boolean scanBootstrapClassPath = false;

  public boolean isScanBootstrapClassPath() {
    return scanBootstrapClassPath;
  }

  public void setScanBootstrapClassPath(boolean scanBootstrapClassPath) {
    this.scanBootstrapClassPath = scanBootstrapClassPath;
  }

  /**
   * Scan the provided ServletContext and classloader for JAR files. Each JAR
   * file found will be passed to the callback handler to be processed.
   *
   * @param context The ServletContext - used to locate and access WEB-INF/lib
   * @param classloader The classloader - used to access JARs not in WEB-INF/lib
   * @param callback The handler to process any JARs found
   * @param jarsToSkip List of JARs to ignore. If this list is null, a default
   * list will be read from the system property defined by
   * {@link Constants#SKIP_JARS_PROPERTY}
   */
  @Override
  public void scan(ServletContext context, ClassLoader classloader,
          JarScannerCallback callback, Set<String> jarsToSkip) {

    if (log.isTraceEnabled()) {
      log.trace(sm.getString("jarScan.webinflibStart"));
    }

    jarsToSkip = jarsToSkip == null ? new HashSet() : new HashSet(jarsToSkip);
    jarsToSkip.addAll(skipPatterns.asSet());
    jarsToSkip = Collections.unmodifiableSet(jarsToSkip);

    final Set<String> ignoredJars;
    if (jarsToSkip == null) {
      ignoredJars = defaultJarsToSkip;
    } else {
      ignoredJars = jarsToSkip;
    }

    // Scan WEB-INF/lib
    Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
    if (dirList != null) {
      Iterator<String> it = dirList.iterator();
      while (it.hasNext()) {
        String path = it.next();
        if (path.endsWith(Constants.JAR_EXT)
                && !virtualWebInfLibs.contains(path)
                && !Matcher.matchName(ignoredJars,
                        path.substring(path.lastIndexOf("/") + 1))) {
          // Need to scan this JAR
          if (log.isDebugEnabled()) {
            log.debug(sm.getString("jarScan.webinflibJarScan", path));
          }
          URL url = null;
          try {
            // File URLs are always faster to work with so use them
            // if available.
            String realPath = context.getRealPath(path);
            if (realPath == null) {
              url = context.getResource(path);
            } else {
              url = (new File(realPath)).toURI().toURL();
            }
            process(callback, url);
          } catch (IOException e) {
            log.warn(sm.getString("jarScan.webinflibFail", url), e);
          }
        } else {
          if (log.isTraceEnabled()) {
            log.trace(sm.getString("jarScan.webinflibJarNoScan", path));
          }
        }
      }
    }

    // Scan the classpath
    if (scanClassPath && classloader != null) {
      if (log.isTraceEnabled()) {
        log.trace(sm.getString("jarScan.classloaderStart"));
      }

      ClassLoader loader = classloader;

      ClassLoader stopLoader = null;
      if (!scanBootstrapClassPath) {
        // Stop when we reach the bootstrap class loader
        stopLoader = ClassLoader.getSystemClassLoader().getParent();
      }

      Set<URL> processedUrls = new HashSet<URL>();

      while (loader != null && loader != stopLoader) {
        if (loader instanceof URLClassLoader) {
          URL[] urls = ((URLClassLoader) loader).getURLs();
          for (int i = 0; i < urls.length; i++) {
            URL url = urls[i];
            if (processedUrls.contains(url)) {
              continue;
            }
            processedUrls.add(url);
            // Extract the jarName if there is one to be found
            String jarName = getJarName(url);

            // Skip JARs known not to be interesting and JARs
            // in WEB-INF/lib we have already scanned
            if (jarName != null
                    && !(Matcher.matchName(ignoredJars, jarName)
                    || url.toString().contains(Constants.WEB_INF_LIB + jarName))) {
              if (log.isDebugEnabled()) {
                log.debug(sm.getString("jarScan.classloaderJarScan", url));
              }
              try {
                process(callback, url);
              } catch (IOException ioe) {
                log.warn(sm.getString(
                        "jarScan.classloaderFail", url), ioe);
              }
            } else {
              if (log.isTraceEnabled()) {
                log.trace(sm.getString("jarScan.classloaderJarNoScan", url));
              }
            }
          }
        }
        loader = loader.getParent();
      }

    }
  }

  /*
   * Scan a URL for JARs with the optional extensions to look at all files
   * and all directories.
   */
  private void process(JarScannerCallback callback, URL url)
          throws IOException {

    if (log.isTraceEnabled()) {
      log.trace(sm.getString("jarScan.jarUrlStart", url));
    }

    URLConnection conn = url.openConnection();
    if (conn instanceof JarURLConnection) {
      callback.scan((JarURLConnection) conn);
    } else {
      String urlStr = url.toString();
      if (urlStr.startsWith("file:") || urlStr.startsWith("jndi:")
              || urlStr.startsWith("http:") || urlStr.startsWith("https:")) {
        if (urlStr.endsWith(Constants.JAR_EXT)) {
          URL jarURL = new URL("jar:" + urlStr + "!/");
          callback.scan((JarURLConnection) jarURL.openConnection());
        } else {
          File f;
          try {
            f = new File(url.toURI());
            if (f.isFile() && scanAllFiles) {
              // Treat this file as a JAR
              URL jarURL = new URL("jar:" + urlStr + "!/");
              callback.scan((JarURLConnection) jarURL.openConnection());
            } else if (f.isDirectory() && scanAllDirectories) {
              File metainf = new File(f, "META-INF");
              if (metainf.isDirectory()) {
                callback.scan(f);
              }
            }
          } catch (URISyntaxException e) {
            // Wrap the exception and re-throw
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
          }
        }
      }
    }

  }

  /*
   * Extract the JAR name, if present, from a URL
   */
  private String getJarName(URL url) {

    String name = null;

    String path = url.getPath();
    int end = path.indexOf(Constants.JAR_EXT);
    if (end != -1) {
      int start = path.lastIndexOf("/", end);
      name = path.substring(start + 1, end + 4);
    } else if (isScanAllDirectories()) {
      int start = path.lastIndexOf("/");
      name = path.substring(start + 1);
    }

    return name;
  }

}
