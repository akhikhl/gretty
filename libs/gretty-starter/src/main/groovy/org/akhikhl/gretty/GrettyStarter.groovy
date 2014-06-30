/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
class GrettyStarter {

  static void main(String[] args) {
    
    def launcherConfig = new LauncherConfig() {

      boolean getDebug() {
        
      }

      boolean getInteractive() {
        
      }

      boolean getManagedClassReload() {
        
      }

      ServerConfig getServerConfig() {
        
      }

      String getStopTaskName() {
        
      }

      WebAppClassPathResolver getWebAppClassPathResolver() {
        
      }

      Iterable<WebAppConfig> getWebAppConfigs() {
        
      }
    }
    
    new StarterLauncher(launcherConfig).launch()
  }
}

