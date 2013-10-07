package gretty


class GrettyPluginExtension {

  int port = 8080
  int servicePort = 9900
  String contextPath
  Map initParameters = [:]
  String realm
  String realmConfigFile
  List overlays = []
  List onStart = []
  List onStop = []

  def initParameter(key, value) {
    initParameters[key] = value
  }

  def overlay(def newValue) {
    if(!(newValue instanceof String))
      throw GradleException("Overlay ${newValue?.toString()} should be a string")
    overlays.add newValue
  }

  def onStart(newValue) {
    onStart.add newValue
  }

  def onStop(newValue) {
    onStop.add newValue
  }
}
