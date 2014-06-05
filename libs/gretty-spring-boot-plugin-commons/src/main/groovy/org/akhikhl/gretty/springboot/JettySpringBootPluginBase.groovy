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

/**
 *
 * @author akhikhl
 */
abstract class JettySpringBootPluginBase extends JettyPluginBase {

  JettySpringBootPluginBase() {
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
    SpringBootResolutionStrategy.apply(project)
  }

  @Override
  protected RunnerFactory getRunnerFactory() {
    new SpringBootRunnerFactory()
  }
  
  @Override
  protected void injectDefaultRepositories(Project project) {
    super.injectDefaultRepositories(project)
    project.repositories {
      maven { url 'http://repo.spring.io/release' }
      maven { url 'http://repo.spring.io/milestone' }
      maven { url 'http://repo.spring.io/snapshot' }      
    }
  }

  @Override
  protected void injectDependencies(Project project) {
    super.injectDependencies(project)
    project.dependencies {
      compile 'org.springframework.boot:spring-boot-starter-web'
      springBoot 'org.springframework.boot:spring-boot-starter-jetty'
    }
  }
}
