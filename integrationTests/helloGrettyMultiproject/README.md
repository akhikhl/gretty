# helloGrettyMultiproject

Multiproject gretty example.

## How to run

```bash
cd examples/helloGrettyMultiproject
gradle appRun
```

## How to test

```bash
cd examples/helloGrettyMultiproject
gradle integrationTest
```

Unfortunately there's no automated test for the reload mechanism, because the `AppBeforeIntegrationTestTask` task
delibrately disables scanning on integration tests.  
The existing automated test verifies only that gretty is usable for multiproject builds.

Verifying the hot-reload behavior has to be done manually:
1. start the server with `gradle appRun` in one terminal
2. use another terminal to `touch subproject/src/main/java/org/akhikhl/examples/gretty/helloGrettyMultiproject/subproject/SomeClass.java`
3. verify in the first terminal that gretty recompiled the project and reloaded the app
4. verify the page is still usable in a browser

## How to build a product


```bash
cd examples/helloGrettyMultiproject
gradle buildProduct
```

