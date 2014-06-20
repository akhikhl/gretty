/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.StandardJarScanner;

/**
 * The code was copied from:
 * https://github.com/spring-projects/spring-boot/blob/master/spring-boot/src/main/java/org/springframework/boot/context/embedded/tomcat/CustomSkipPatternJarScanner.java
 * Changes compared to original version: got rid of springframework-specific classes.
 *
 * @author akhikhl
 */
public class SkipPatternJarScanner extends StandardJarScanner {

	private final JarScanner jarScanner;

	private final SkipPattern pattern;

	SkipPatternJarScanner(JarScanner jarScanner, String pattern) {
		assert jarScanner != null : "JarScanner must not be null";
		this.jarScanner = jarScanner;
		this.pattern = (pattern == null ? new SkipPattern() : new SkipPattern(pattern));
	}

	@Override
	public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
    jarsToSkip = jarsToSkip == null ? new HashSet<String>() : new HashSet<String>(jarsToSkip);
    jarsToSkip.addAll(this.pattern.asSet());
    jarsToSkip = Collections.unmodifiableSet(jarsToSkip);
		this.jarScanner.scan(context, classloader, callback, jarsToSkip);
	}

	/**
	 * Apply this decorator the specified context.
	 * @param context the context to apply to
	 * @param pattern the jar skip pattern or {@code null} for defaults
	 */
	public static void apply(StandardContext context, String pattern) {
		SkipPatternJarScanner scanner = new SkipPatternJarScanner(
				context.getJarScanner(), pattern);
		context.setJarScanner(scanner);
	}

	private static final class SkipPattern {

		private final Set<String> patterns = new LinkedHashSet<String>();

		protected SkipPattern() {
			add("ant-*.jar");
			add("aspectj*.jar");
			add("commons-beanutils*.jar");
			add("commons-codec*.jar");
			add("commons-collections*.jar");
			add("commons-dbcp*.jar");
			add("commons-digester*.jar");
			add("commons-fileupload*.jar");
			add("commons-httpclient*.jar");
			add("commons-io*.jar");
			add("commons-lang*.jar");
			add("commons-logging*.jar");
			add("commons-math*.jar");
			add("commons-pool*.jar");
			add("geronimo-spec-jaxrpc*.jar");
			add("h2*.jar");
			add("hamcrest*.jar");
			add("hibernate*.jar");
			add("jmx*.jar");
			add("jmx-tools-*.jar");
			add("jta*.jar");
			add("junit-*.jar");
			add("httpclient*.jar");
			add("log4j-*.jar");
			add("mail*.jar");
			add("org.hamcrest*.jar");
			add("slf4j*.jar");
			add("tomcat-embed-core-*.jar");
			add("tomcat-embed-logging-*.jar");
			add("tomcat-jdbc-*.jar");
			add("tomcat-juli-*.jar");
			add("tools.jar");
			add("wsdl4j*.jar");
			add("xercesImpl-*.jar");
			add("xmlParserAPIs-*.jar");
			add("xml-apis-*.jar");
      // gretty-specific
      add("gretty-runner*.jar");
      add("javax.servlet*.jar");
      add("sysout-over-slf4j*.jar");
      add("jul-to-slf4j*.jar");
      add("jcl-over-slf4j*.jar");
      add("logback*.jar");
      add("springloaded*.jar");
      add("tomcat-embed*.jar");
      add("groovy-all*.jar");
      add("ecj-*.jar");
		}

		public SkipPattern(String patterns) {
      this();
			StringTokenizer tokenizer = new StringTokenizer(patterns, ",");
			while (tokenizer.hasMoreElements()) {
				add(tokenizer.nextToken());
			}
		}

		protected void add(String patterns) {
			assert patterns != null : "Patterns must not be null";
			if (patterns.length() > 0 && !patterns.trim().startsWith(",")) {
				this.patterns.add(",");
			}
			this.patterns.add(patterns);
		}

		public Set<String> asSet() {
			return this.patterns;
		}
	}
}
