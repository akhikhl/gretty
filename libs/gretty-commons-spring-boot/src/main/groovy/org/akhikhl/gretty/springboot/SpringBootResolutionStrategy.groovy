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
    if(project.configurations.findByName('versionManagement')) // already applied?
      return
    project.configurations.create('versionManagement')
    resolveClasses()
    resolveDependencyVersions(project)
  }

  private static resolveClasses() {
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
  
  private static getManagedDependencies(Project project) {
    if(VersionManagedDependencies == null) 
      ManagedDependencies.get()
    else {
      VersionManagedDependencies.newInstance(project.configurations.versionManagement.files.collect { file ->
        assert file.name.toLowerCase().endsWith('.properties')
        // FileInputStream is closed by PropertiesFileManagedDependencies constructor
        PropertiesFileManagedDependencies.newInstance(new FileInputStream(file))
      })
    }
  }
  
  static String getSpringBootVersion(Project project) {
    getSpringBootVersionFromManagedDependencies(getManagedDependencies(project))
  }
  
  private static String getSpringBootVersionFromManagedDependencies(managedDependencies) {
    if(managedDependencies.metaClass.methods.find { it.name == 'getSpringBootVersion' })
      // 1.1.x
      managedDependencies.getSpringBootVersion()
    else
      // 1.0.x
      managedDependencies.getVersion()
  }

  private static void resolveDependencyVersions(Project project) {
    def managedDependencies
    project.configurations.all { config ->
      config.resolutionStrategy.eachDependency { DependencyResolveDetails resolveDetails ->
        if(!resolveDetails.target.version) {
          if(managedDependencies == null)
            managedDependencies = getManagedDependencies(project)
          if (resolveDetails.target.group == 'org.springframework.boot')
            resolveDetails.useVersion(getSpringBootVersionFromManagedDependencies(managedDependencies))
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
