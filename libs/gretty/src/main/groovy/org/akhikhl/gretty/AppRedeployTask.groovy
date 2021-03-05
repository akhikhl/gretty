package org.akhikhl.gretty

import org.gradle.api.tasks.Internal

/**
 * @author sala
 */
class AppRedeployTask extends AppServiceTask {
  @Internal
  List webapps = []

  def webapp(String webapp) {
    webapps.add(webapp)
  }

  @Override
  String getCommand() {
    return "redeploy ${webapps.join(' ')}"
  }
}
