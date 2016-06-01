package org.akhikhl.gretty

import org.gradle.api.Action
import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.logging.LoggingManager
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskInputs
import org.gradle.api.tasks.TaskOutputs
import org.gradle.api.tasks.TaskState

/**
 * Dummy implementation of task plugin
 *
 * @author sala
 */
class DummyTask implements Task {
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
  TaskState getState() {
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
  TaskInputs getInputs() {
    return null
  }

  @Override
  TaskOutputs getOutputs() {
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
  int compareTo(Task o) {
    return 0
  }

  @Override
  ExtensionContainer getExtensions() {
    return null
  }
}
