# VisOpt - Compiler and Server

This is a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) project.

## Run in Development Mode

Use the IntelliJ run configuration *ServerKt* or run:

```shell
spring_profiles_active=development ./gradlew run
```

Listens on port [4552](http://localhost:4552/).

## Test

```shell
./gradlew check
```

This repository contains only public test cases, as most of the test cases were not allowed to be published. Private test case locations can be passed via the environment variable `TEST_CASES_PATH`.

## Build for Production

```shell
./gradlew installDist
```

## Deploy

```shell
mkdir -p /opt/visopt/compiler
rsync -aiv --delete build/install/compiler/ /opt/visopt/compiler
```

## Run in Production Mode

```shell
spring_profiles_active=production /opt/visopt/compiler/bin/compiler
```

Listens on port [34552](http://localhost:34552/).
