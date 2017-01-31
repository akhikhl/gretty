package org.akhikhl.gretty

import org.gradle.api.Action
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.TaskInputsInternal
import org.gradle.api.internal.TaskInternal
import org.gradle.api.internal.TaskOutputsInternal
import org.gradle.api.internal.tasks.ContextAwareTaskAction
import org.gradle.api.internal.tasks.TaskExecuter
import org.gradle.api.internal.tasks.TaskStateInternal
import org.gradle.api.internal.tasks.execution.TaskValidator
import org.gradle.api.logging.Logger
import org.gradle.api.logging.LoggingManager
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import org.gradle.internal.Factory
import org.gradle.logging.StandardOutputCapture

/**
 * Dummy implementation of task plugin
 *
 * @author sala
 */
class DummyTask implements TaskInternal {

  @Override
  List<ContextAwareTaskAction> getTaskActions() {
    return null
  }

  @Override
  Set<ClassLoader> getActionClassLoaders() {
    return null
  }

  @Override
  Spec<? super TaskInternal> getOnlyIf() {
    return null
  }

  @Override
  void execute() {

  }

  @Override
  StandardOutputCapture getStandardOutputCapture() {
    return null
  }

  @Override
  TaskExecuter getExecuter() {
    return null
  }

  @Override
  void setExecuter(TaskExecuter taskExecuter) {

  }

  @Override
  TaskInputsInternal getInputs() {
    return null
  }

  @Override
  TaskOutputsInternal getOutputs() {
    return null
  }

  @Override
  File getTemporaryDir() {
    return null
  }

  @Override
  Task mustRunAfter(Object... objects) {
    return null
  }

  @Override
  void setMustRunAfter(Iterable<?> iterable) {

  }

  @Override
  TaskDependency getMustRunAfter() {
    return null
  }

  @Override
  Task finalizedBy(Object... objects) {
    return null
  }

  @Override
  void setFinalizedBy(Iterable<?> iterable) {

  }

  @Override
  TaskDependency getFinalizedBy() {
    return null
  }

  @Override
  TaskDependency shouldRunAfter(Object... objects) {
    return null
  }

  @Override
  void setShouldRunAfter(Iterable<?> iterable) {

  }

  @Override
  TaskDependency getShouldRunAfter() {
    return null
  }

  @Override
  List<TaskValidator> getValidators() {
    return null
  }

  @Override
  void addValidator(TaskValidator taskValidator) {

  }

  @Override
  String getName() {
    return null
  }

  @Override
  Project getProject() {
    return null
  }

  @Override
  List<Action<? super Task>> getActions() {
    return null
  }

  @Override
  void setActions(List<Action<? super Task>> list) {

  }

  @Override
  TaskDependency getTaskDependencies() {
    return null
  }

  @Override
  Set<Object> getDependsOn() {
    return null
  }

  @Override
  void setDependsOn(Iterable<?> iterable) {

  }

  @Override
  Task dependsOn(Object... objects) {
    return null
  }

  @Override
  void onlyIf(Closure closure) {

  }

  @Override
  void onlyIf(Spec<? super Task> spec) {

  }

  @Override
  void setOnlyIf(Closure closure) {

  }

  @Override
  void setOnlyIf(Spec<? super Task> spec) {

  }

  @Override
  TaskStateInternal getState() {
    return null
  }

  @Override
  void setDidWork(boolean b) {

  }

  @Override
  boolean getDidWork() {
    return false
  }

  @Override
  String getPath() {
    return null
  }

  @Override
  Task doFirst(Action<? super Task> action) {
    return null
  }

  @Override
  Task doFirst(Closure closure) {
    return null
  }

  @Override
  Task doLast(Action<? super Task> action) {
    return null
  }

  @Override
  Task doLast(Closure closure) {
    return null
  }

  @Override
  Task leftShift(Closure closure) {
    return null
  }

  @Override
  Task deleteAllActions() {
    return null
  }

  @Override
  boolean getEnabled() {
    return false
  }

  @Override
  void setEnabled(boolean b) {

  }

  @Override
  Task configure(Closure closure) {
    return null
  }

  @Override
  AntBuilder getAnt() {
    return null
  }

  @Override
  Logger getLogger() {
    return null
  }

  @Override
  LoggingManager getLogging() {
    return null
  }

  @Override
  Object property(String s) throws MissingPropertyException {
    return null
  }

  @Override
  boolean hasProperty(String s) {
    return false
  }

  @Override
  Convention getConvention() {
    return null
  }

  @Override
  String getDescription() {
    return null
  }

  @Override
  void setDescription(String s) {

  }

  @Override
  String getGroup() {
    return null
  }

  @Override
  void setGroup(String s) {

  }

  @Override
  boolean dependsOnTaskDidWork() {
    return false
  }

  @Override
  boolean getImpliesSubProjects() {
    return false
  }

  @Override
  void setImpliesSubProjects(boolean b) {

  }

  @Override
  Factory<File> getTemporaryDirFactory() {
    return null
  }

  @Override
  void prependParallelSafeAction(Action<? super Task> action) {

  }

  @Override
  void appendParallelSafeAction(Action<? super Task> action) {

  }

  @Override
  boolean isHasCustomActions() {
    return false
  }

  @Override
  int compareTo(Task o) {
    return 0
  }

  @Override
  ExtensionContainer getExtensions() {
    return null
  }

  @Override
  org.gradle.util.Path getIdentityPath() {
    null
  }
}
