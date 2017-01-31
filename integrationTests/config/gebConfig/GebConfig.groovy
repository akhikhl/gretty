import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxProfile

driver = {
  // we pass profile with trusted localhost ssl certificate
  new FirefoxDriver(new FirefoxProfile(new File(System.getProperty('firefox.profile.path'))))
}
