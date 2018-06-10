package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.GradleException
import org.gradle.api.Project
/**
 * @author sala
 */
@CompileStatic(TypeCheckingMode.SKIP)
class FarmConfigurerUtil {

  static Project resolveProjectRefToProject(Project project, projectRef) {
    def proj
    if(projectRef instanceof Project)
      proj = projectRef
    else if(projectRef instanceof String || projectRef instanceof GString)
      proj = project.findProject(projectRef)
    proj
  }

  static File resolveWebAppRefToWarFile(Project project, webAppRef) {
    File warFile = webAppRef instanceof File ? webAppRef : new File(webAppRef.toString())
    if(!warFile.isAbsolute())
      warFile = new File(project.projectDir, warFile.path)
    warFile.absoluteFile
  }

  static Tuple resolveWebAppType(Project project, suppressMavenToProjectResolution, wref) {
    def proj = resolveProjectRefToProject(project, wref)
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
