# filterWebapp

Example programs showing how to implement webapp filtering with gretty.

The main ideas behind webapp filtering are the following:

1. Webapp filtering gives possibility to filter files in "src/main/webapp" 
in a similar way to processResources filtering.

2. Webapp filtering happens "naturally", each time the program is run
or each time a WAR-file (or a product) is built.

3. Webapp filtering is configured via property "gretty.webappCopy":

```groovy
gretty {
  webappCopy = { CopySpec copySpec ->
     // ...
  }
}
```

webappCopy is a closure, accepting org.gradle.api.file.CopySpec as a parameter.

See details in "build.gradle" and "src/main/webapp/WEB-INF/web.xml".

## How to run

```bash
cd examples/filterWebapp
gradle appRun
```

## How to test

```bash
cd examples/filterWebapp
gradle integrationTest
```

## How to build a product


```bash
cd examples/filterWebapp
gradle buildProduct
```

