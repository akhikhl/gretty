package org.akhikhl.gretty

import org.gradle.api.GradleException
import org.gradle.api.Project
/**
 * @author sala
 */
class FarmConfigurerUtil {

  static Project resolveWebAppRefToProject(Project project, webAppRef) {
    def proj
    if(webAppRef instanceof Project)
      proj = webAppRef
    else if(webAppRef instanceof String || webAppRef instanceof GString)
      proj = project.findProject(webAppRef)
    proj
  }

  static File resolveWebAppRefToWarFile(Project project, webAppRef) {
    File warFile = webAppRef instanceof File ? webAppRef : new File(webAppRef.toString())
    if(!warFile.isAbsolute())
      warFile = new File(project.projectDir, warFile.path)
    warFile.absoluteFile
  }

  static Tuple resolveWebAppType(Project project, suppressMavenToProjectResolution, wref) {
    def proj = resolveWebAppRefToProject(project, wref)
    if(proj) {
      return new Tuple(FarmWebappType.PROJECT, proj)
    }

    wref = wref.toString()
    def gav = wref.split(":")
    if(gav.length == 3) {
      if(!suppressMavenToProjectResolution) {
        proj = project.rootProject.allprojects.find { it.group == gav[0] && it.name == gav[1] }
        if(proj)
          return new Tuple(FarmWebappType.DEPENDENCY_TO_PROJECT, proj)
      }
      return new Tuple(FarmWebappType.WAR_DEPENDENCY, wref)
    }

    def warFile = resolveWebAppRefToWarFile(project, wref)
    if(warFile) {
      return new Tuple(FarmWebappType.WAR_FILE, warFile)
    }

    throw new GradleException("Cannot evaluate type: $warFile")
  }
}
