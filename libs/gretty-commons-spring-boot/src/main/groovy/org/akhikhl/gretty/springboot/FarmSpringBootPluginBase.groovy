/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty.springboot

import org.akhikhl.gretty.FarmPluginBase
import org.akhikhl.gretty.LauncherFactory
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
abstract class FarmSpringBootPluginBase extends FarmPluginBase {

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
    if(!project.configurations.findByName('grettyNoSpringBoot'))
      project.configurations {
        grettyNoSpringBoot {
          extendsFrom gretty
          exclude group: 'org.springframework.boot'
        }
      }
    SpringBootResolutionStrategy.apply(project)
  }

  @Override
  protected LauncherFactory getLauncherFactory() {
    new SpringBootLauncherFactory()
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
    project.dependencies {
      compile 'org.springframework.boot:spring-boot-starter-web'
      springBoot 'org.springframework.boot:spring-boot-starter-jetty'
    }
  }
}

