import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.firefox.FirefoxOptions

FirefoxOptions options = new FirefoxOptions()
options.setProfile(new FirefoxProfile(new File(System.getProperty('firefox.profile.path'))))
options.setHeadless(true)
driver = {
  // we pass profile with trusted localhost ssl certificate
  new FirefoxDriver(options)
}
