Gretty is regularly published to maven central, so normally you don't need to use this repository.

If you need to use older gretty versions, you can configure gradle to use this repository:

```groovy
repositories {
    maven {
        url 'https://raw2.github.com/akhikhl/gretty/master/maven_repo'
    }
}
```
