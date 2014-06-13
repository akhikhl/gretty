/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

/**
 *
 * @author akhikhl
 */
class JacocoHelper implements JavaForkOptions, ExtensionAware {

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
  void doFirst(Closure closure) {
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
