/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails

/**
 *
 * @author akhikhl
 */
class SpringBootResolutionStrategy {

  protected static Class ManagedDependencies
  protected static Class PropertiesFileManagedDependencies
  protected static Class VersionManagedDependencies

  static void apply(Project project) {
    init()
    if(project.configurations.findByName('versionManagement')) // already applied?
      return
    project.configurations.create('versionManagement')
    if(PropertiesFileManagedDependencies == null)
      resolveDependencyVersions_1_0_x(project)
    else
      resolveDependencyVersions_1_1_x(project)
  }

  private static init() {
    if(ManagedDependencies == null)
      try {
        ManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.ManagedDependencies', true, JettySpringBootPluginBase.classLoader)
      } catch(ClassNotFoundException e) {
        throw new GradleException('There is no spring-boot-dependency-tools on the classpath of gretty plugin.')
      }
    if(PropertiesFileManagedDependencies == null)
      try {
        PropertiesFileManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.PropertiesFileManagedDependencies', true, JettySpringBootPluginBase.classLoader)
      } catch(ClassNotFoundException e) {
        // ignore, we are using older version of spring-boot
      }
    if(VersionManagedDependencies == null)
      try {
        VersionManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.VersionManagedDependencies', true, JettySpringBootPluginBase.classLoader)
      } catch(ClassNotFoundException e) {
        // ignore, we are using older version of spring-boot
      }
  }

  private static void resolveDependencyVersions_1_0_x(Project project) {
    def managedDependencies
    project.configurations.all { config ->
      config.resolutionStrategy.eachDependency { DependencyResolveDetails resolveDetails ->
        if(!resolveDetails.target.version) {
          if(managedDependencies == null)
            managedDependencies = ManagedDependencies.get()
          if (resolveDetails.target.group == 'org.springframework.boot')
            resolveDetails.useVersion(managedDependencies.version)
          else {
            def dependency = managedDependencies.find(target.group, target.name)
            if (dependency)
              resolveDetails.useVersion(dependency.version)
          }
        }
      }
    }
  }

  private static void resolveDependencyVersions_1_1_x(Project project) {
    def managedDependencies
    project.configurations.all { config ->
      if(config.name == 'versionManagement')
        return
      config.resolutionStrategy.eachDependency { DependencyResolveDetails resolveDetails ->
        if(!resolveDetails.target.version) {
          if(managedDependencies == null)
            managedDependencies = VersionManagedDependencies.newInstance(project.configurations.versionManagement.files.collect { file ->
              assert file.name.toLowerCase().endsWith('.properties')
              PropertiesFileManagedDependencies.newInstance(new FileInputStream(file))
            })
          if (resolveDetails.target.group == 'org.springframework.boot')
            resolveDetails.useVersion(managedDependencies.springBootVersion)
          else {
            def dependency = managedDependencies.find(target.group, target.name)
            if (dependency)
              resolveDetails.useVersion(dependency.version)
          }
        }
      }
    }
  }
}
