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
  
  protected static final Class PropertiesFileManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.PropertiesFileManagedDependencies', true, JettySpringBootPluginBase.classLoader)
  protected static final Class VersionManagedDependencies = Class.forName('org.springframework.boot.dependency.tools.VersionManagedDependencies', true, JettySpringBootPluginBase.classLoader)

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
      resolveDependencyVersions(project)
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
