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
import org.gradle.api.GradleException
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class FarmExtension extends FarmConfig {

  final Project project

  String integrationTestTask = 'integrationTest'

  protected final List includes_ = []

  private final List afterEvaluate_ = []

  FarmExtension(Map options = [:], Project project) {
    super(options)
    this.project = project
    if(options.containsKey('integrationTestTask'))
      this.integrationTestTask = options.integrationTestTask
    def includes = options.includes_ ?: options.includes
    if(includes)
      this.includes_.addAll(includes as Collection)
    if(options.containsKey('afterEvaluate'))
      afterEvaluate_.add(options.afterEvaluate as Closure)
  }

  void afterEvaluate(Closure closure) {
    afterEvaluate_.add(closure)
  }
  
  private static void collectWebAppRefs(Map result, List includeStack, FarmExtension ext) {
    if(includeStack.contains(ext))
      throw new GradleException("Cyclic farm inclusion: ${ includeStack.collect { it.project.path + '/' + it.farmName } + [ext.project.path + '/' + ext.farmName] }")
    result << ext.webAppRefs_
    if(ext.includes_) {
      includeStack.push(ext)
      for(Map include in ext.includes_) {
        def otherFarms = ext.project.project(include.project).extensions.findByName('farms')
        if(otherFarms) {
          def otherFarm = otherFarms.farmsMap[include.farmName]
          if(otherFarm == null)
            throw new GradleException("Farm ${ext.project.path + '/' + ext.farmName} includes non-existing farm ${include.project + '/' + include.farmName}")
          collectWebAppRefs(result, includeStack, otherFarm)
        }
      }
      includeStack.pop()
    }
  }

  List getAfterEvaluate() {
    afterEvaluate_.asImmutable()
  }
  
  String getFarmName() {
    def thisFarm = this
    project.farms.farmsMap.find { key, value -> value == thisFarm }?.key
  }

  List getIncludes() {
    includes_.asImmutable()
  }

  @Override
  Map getWebAppRefs() {
    Map result = [:]
    collectWebAppRefs(result, [], this)
    result
  }

  void include(def project, def farmName = '') {
    includes_.add([ project: project, farmName: farmName ])
  }

  @Override
  void webapp(Map options = [:], w) {
    if(w instanceof Project)
      w = w.path
    webAppRefs_[w] = options
  }
}
