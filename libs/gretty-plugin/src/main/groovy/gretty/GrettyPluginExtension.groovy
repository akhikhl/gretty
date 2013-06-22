package gretty

class GrettyPluginExtension {

  int port = 8080
  int stopPort = 9999
  def onStart = []
  def onStop = []
  String realm
  String realmConfigFile

  def onStart(newValue) {
    onStart.add newValue;
  }

  def onStop(newValue) {
    onStop.add newValue;
  }
}
