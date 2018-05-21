/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.gaffer.ConfigurationDelegate
import ch.qos.logback.classic.gaffer.GafferConfigurator
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.util.ContextUtil
import ch.qos.logback.core.util.OptionHelper
import org.codehaus.groovy.control.CompilerConfiguration

/**
 * Extends logback GafferConfigurator with run method accepting Binding
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class GafferConfiguratorEx extends GafferConfigurator {

  GafferConfiguratorEx(LoggerContext context) {
    super(context)
  }

  void run(Binding binding, String dslText) {

    binding.setProperty("hostname", ContextUtil.localHostName);

    def configuration = new CompilerConfiguration()
    configuration.addCompilationCustomizers(importCustomizer())

    String debugAttrib = System.getProperty(DEBUG_SYSTEM_PROPERTY_KEY);
    if (OptionHelper.isEmpty(debugAttrib) || debugAttrib.equalsIgnoreCase("false")
            || debugAttrib.equalsIgnoreCase("null")) {
      // For now, Groovy/Gaffer configuration DSL does not support "debug" attribute. But in order to keep
      // the conditional logic identical to that in XML/Joran, we have this empty block.
    } else {
      OnConsoleStatusListener.addNewInstanceToContext(context);
    }

    // caller data should take into account groovy frames
    new ContextUtil(context).addGroovyPackages(context.getFrameworkPackages());

    Script dslScript = new GroovyShell(binding, configuration).parse(dslText)

    dslScript.metaClass.mixin(ConfigurationDelegate)
    dslScript.setContext(context)
    dslScript.metaClass.getDeclaredOrigin = { dslScript }

    dslScript.run()
  }
}

