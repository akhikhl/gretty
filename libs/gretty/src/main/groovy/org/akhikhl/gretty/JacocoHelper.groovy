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
import org.gradle.api.internal.TaskInputsInternal
import org.gradle.api.internal.TaskOutputsInternal
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
/**
 *
 * @author akhikhl
 */
class JacocoHelper extends DummyTask implements JavaForkOptions, ExtensionAware {

  private final Task task

  @Delegate
  private final JavaForkOptions javaForkOptions

  JacocoHelper(Task task) {
    this.task = task
    javaForkOptions = [:] as JavaForkOptions
    task.project.jacoco.applyTo(this)
  }

  // needed by JacocoPluginExtension.applyTo, dummy
  Task doFirst(Closure closure) {
  }

  // needed by JacocoPluginExtension.applyTo
  ExtensionContainer getExtensions() {
    task.getExtensions()
  }

  @Override
  TaskInputsInternal getInputs() {
    task.getInputs()
  }

  // needed by JacocoPluginExtension.applyTo
  JacocoTaskExtension getJacoco() {
    task.extensions.jacoco
  }

  // needed by JacocoPluginExtension.applyTo
  String getName() {
    task.getName()
  }

  @Override
  TaskOutputsInternal getOutputs() {
    task.getOutputs()
  }

  File getWorkingDir() {
    task.project.projectDir
  }
}
