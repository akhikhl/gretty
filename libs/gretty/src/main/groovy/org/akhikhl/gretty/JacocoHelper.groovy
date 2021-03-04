package org.akhikhl.gretty

import org.gradle.api.internal.TaskInputsInternal
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.TaskOutputsInternal
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.Internal
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

interface JacocoHelper extends TaskInternal, JavaForkOptions, ExtensionAware {
  @Internal
  JacocoTaskExtension getJacoco()

  @Internal
  @Override
  Map<String, Object> getEnvironment()

  @Internal
  @Override
  String getExecutable()

  @Internal
  @Override
  TaskInputsInternal getInputs()

  @Internal
  @Override
  TaskOutputsInternal getOutputs()

  @Internal
  @Override
  TaskStateInternal getState()

  @Internal
  @Override
  File getWorkingDir()
}