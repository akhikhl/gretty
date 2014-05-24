/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.process.JavaForkOptions

/**
 *
 * @author akhikhl
 */
class JacocoJavaForkOptionsWrapper implements JavaForkOptions, ExtensionAware {

  @Delegate
  private final JavaForkOptions delegate
  private final dummyContainer

  JacocoJavaForkOptionsWrapper(Project project, JavaForkOptions delegate) {
    this.delegate = delegate
    dummyContainer = project.container(Runner)
  }

  void doFirst(Closure closure) {
    closure()
  }

  ExtensionContainer getExtensions() {
    dummyContainer.extensions
  }

  def getJacoco() {
    dummyContainer.extensions.jacoco
  }

  String getName() {
    'JavaExecSpecWrapper'
  }
}
