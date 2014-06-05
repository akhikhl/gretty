/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.JettyPluginBase
import org.akhikhl.gretty.RunnerFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolveDetails

/**
 *
 * @author akhikhl
 */
abstract class JettySpringBootPluginBase extends JettyPluginBase {
  
  protected static Class ManagedDependencies
  protected static Class PropertiesFileManagedDependencies
  protected static Class VersionManagedDependencies
  
  JettySpringBootPluginBase() {
    if(ManagedDependencies == null)
      try {
        ManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.ManagedDependencies', true, JettySpringBootPluginBase.classLoader)
      } catch(ClassNotFoundException e) {
        // ignore, we are using older version of spring-boot
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

  @Override
  protected void createConfigurations(Project project) {
    super.createConfigurations(project)
    if(!project.configurations.findByName('springBoot'))
      project.configurations {
        springBoot {
          extendsFrom runtime
          exclude module: 'spring-boot-starter-tomcat'
          exclude group: 'org.eclipse.jetty'
        }
      }
    if(!project.configurations.findByName('versionManagement')) {
      project.configurations.create('versionManagement')
      if(PropertiesFileManagedDependencies == null)
        resolveDependencyVersions(project)
      else
        resolveDependencyVersionsNew(project)
    }
  }	
  
  @Override
  protected RunnerFactory getRunnerFactory() {
    new SpringBootRunnerFactory()
  }

  @Override
  protected void injectDependencies(Project project) {
    super.injectDependencies(project)
    project.dependencies {
      springBoot 'org.springframework.boot:spring-boot-starter-jetty'
    }
  }
  
  private void resolveDependencyVersions(Project project) {
    def managedDependencies
    project.configurations.all { config ->
      if(config.name == 'versionManagement')
        return
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
  
  private void resolveDependencyVersionsNew(Project project) {
    def managedDependencies
    project.configurations.all { config ->
      if(config.name == 'versionManagement')
        return
      config.resolutionStrategy.eachDependency { DependencyResolveDetails resolveDetails ->
        if(!resolveDetails.target.version) {
          if(managedDependencies == null) {
            managedDependencies = project.configurations.versionManagement.files.collect { file ->
              assert file.name.toLowerCase().endsWith('.properties')
              PropertiesFileManagedDependencies.newInstance(new FileInputStream(file))
            }
            managedDependencies = VersionManagedDependencies.newInstance(managedDependencies)
          }
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
