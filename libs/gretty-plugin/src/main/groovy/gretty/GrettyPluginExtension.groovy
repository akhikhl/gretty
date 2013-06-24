package gretty


class GrettyPluginExtension {

  int port = 8080
  int servicePort = 9900
  String contextPath
  def initParameters = [:]
  String realm
  String realmConfigFile
  def overlays = []
  def onStart = []
  def onStop = []

  def initParameter(key, value) {
    initParameters[key] = value
  }

  def overlay(newValue) {
    overlays.add newValue
  }

  def onStart(newValue) {
    onStart.add newValue
  }

  def onStop(newValue) {
    onStop.add newValue
  }
}
