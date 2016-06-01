/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
/**
 *
 * @author akhikhl
 */
class JacocoHelper extends DummyTask implements JavaForkOptions, ExtensionAware {

  private final String taskName
  private final ExtensionContainer extensions
  private final File projectDir

  @Delegate
  private final JavaForkOptions javaForkOptions

  JacocoHelper(Task task) {
    this.taskName = task.name
    this.extensions = task.extensions
    this.projectDir = task.project.projectDir
    javaForkOptions = [:] as JavaForkOptions
    task.project.jacoco.applyTo(this)
  }

  // needed by JacocoPluginExtension.applyTo, dummy
  Task doFirst(Closure closure) {
  }

  // needed by JacocoPluginExtension.applyTo
  ExtensionContainer getExtensions() {
    extensions
  }

  // needed by JacocoPluginExtension.applyTo
  JacocoTaskExtension getJacoco() {
    extensions.jacoco
  }

  // needed by JacocoPluginExtension.applyTo
  String getName() {
    taskName
  }

  File getWorkingDir() {
    projectDir
  }
}
