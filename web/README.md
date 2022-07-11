# VisOpt - Web Client

This project was bootstrapped with [Create React App](https://github.com/facebook/create-react-app).

## Set up

```shell
cd ../compiler
./gradlew compileKotlinJs
cd ../web
npm install
```

## Run in Development Mode

```shell
npm run start
```

Listens on port [3000](http://localhost:3000/).

## Test

```shell
CI=true npm run test
```

This repository contains only public test cases, as most of the test cases were not allowed to be published. Private test case locations can be passed via the environment variable `TEST_CASES_PATH`.

## Build for Production

```shell
npm run build
```

## Deploy

```shell
mkdir -p /opt/visopt/web
rsync -aiv --delete build/ /opt/visopt/web
```

## Run in Production Mode

Files in `/opt/visopt/web` are served by VisOpt production server.
