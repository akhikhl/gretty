package org.akhikhl.gretty

import org.gradle.api.internal.TaskInternal
import org.gradle.api.plugins.ExtensionAware
import org.gradle.process.JavaForkOptions
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

interface JacocoHelper extends TaskInternal, JavaForkOptions, ExtensionAware {

  JacocoTaskExtension getJacoco()
}