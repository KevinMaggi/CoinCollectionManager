# Coin Collection Manager

A coin collection manager application developed with advanced programming techniques practices, like TDD, build automation and continuous integration.

This work is part of the exam *Advanced Programming Techniques* (6CFU version) by Prof. *Lorenzo Bettini* in Laurea Magistrale in Ingegneria Informatica at University of Florence.

###### Project report:

[IT] *cooming soon*

###### Project documentation (by [GitHub Pages](https://pages.github.com/)):

[https://kevinmaggi.github.io/CoinCollectionManager/](https://kevinmaggi.github.io/CoinCollectionManager/)

###### Continuous integration (by [GitHub Actions](https://github.com/features/actions)):

[![Build with Maven (Linux)](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-linux.yaml/badge.svg)](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-linux.yaml)<br/>
[![Build with Maven (Windows) [only GUI tests]](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-windows.yaml/badge.svg)](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-windows.yaml)<br/>
[![Build with Maven (MacOS)](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-macos.yaml/badge.svg)](https://github.com/KevinMaggi/CoinCollectionManager/actions/workflows/maven-macos.yaml)

###### Code coverage (by [Coveralls](https://coveralls.io/)):

[![Coverage Status](https://coveralls.io/repos/github/KevinMaggi/CoinCollectionManager/badge.svg?branch=main)](https://coveralls.io/github/KevinMaggi/CoinCollectionManager?branch=main)

###### Code quality (by [SonarCloud](https://www.sonarsource.com/products/sonarcloud/)):

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=coverage)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)<br/>
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)<br/>
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=bugs)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)<br/>
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=KevinMaggi_CoinCollectionManager&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=KevinMaggi_CoinCollectionManager)

## Overview

In this project I developed a basic application in **Java 11** for managing a coin collection, with generic coins that can be located in albums, keeping track also of the location of albums and their fullness. CRUD operations are enabled on both albums and coins, through a GUI (in **Java Swing**). The underlying database is **Postgresql** and the persistence API used is **Hibernate**.

The development (with TDD approach) made use of testing (with **jUnit** 4 and 5 and **AssertJ**), code coverage (with **JaCoCo**) mocking (with **Mockito**), mutation testing (with **Pitest**) and isolated container (with **Docker** and **TestContainer**).

The software have been fully tested in CI on Ubuntu and macOS with Java 11. Against Windows has been tested only the GUI in CI, due to the incompatibility of GitHub Actions Windows environment with Linux containers; however it has been fully tested on local machine with Windows.

## Requirements

To follow the underlying instructions, the following tools are needed:

- Java 11
- Docker
- Docker compose (already included in Docker Desktop)

Maven is not required since all the following instructions make use of a Maven Wrapper: for a perfectly reproducible experience will be downloaded the Maven version used during the development.

## Get the code

### Clone the repo

To clone the repo:

```
git clone https://github.com/KevinMaggi/CoinCollectionManager.git
```

### Eclipse project

If you want to import the project into Eclipse IDE you can do it by importing from Git (you can or not have been already cloned the repo).

If you use Eclipse Smart Imports, remember to not import main folder as project, but only the subfolders, for a better experience.

N.B. Sometimes I experienced some errors and/or warnings just imported in Eclipse, anyway it is just matter of refreshing the Eclipse projects and everything goes for the best.

## Build the project and run tests

After having cloned the repo, you have to move inside the root project directory:

```
cd CoinCollectionManager
```

Before to build the project is necessary to have a Docker postgres:15.1 image locally, otherwise it will be pulled during the first build, causing (probably) a failure due to a timeout.

```
docker pull postgres:15.1
```

After that, in order to build the project and run all tests with Maven (make sure you have Docker running), simply run:

```
./mvnw -f coin-collection-manager-aggregator/pom.xml clean verify
```

Enabling the profile `-Pdocker-build` it will build also the Docker image of the application (more on this later).

If you are on Windows, substitute `./mvnw` with `mvnw.cmd`.<br/>
On Windows, in addition to enabling the option "*Expose daemon on tcp://localhost:2375 without TLS*", I suggest to run the previous command with the parameter `-Ddocker.host=tcp://localhost:2375` in order to make execution more reliable (in the sense that without that, sometimes the build fails because Docker is not accessible).

If you are on Linux or macOS you could have to precede the command with `sudo`, depending on your system/docker configuration, otherwise the build will fail when request for Docker.

If you don't want to use the Wrapper (at your own "risk" of build failure), simply substitute all `./mvnw` occurrences with `mvn`.

### Tests

The following Maven profiles are available to enable additional features during tests:

- `-Pjacoco` to perform code coverage analysis (an aggregate report will be available in the `coin-collection-manager-report/target/site/jacoco-aggregate` folder);
- `-Ppitest` to run also mutation testing (an aggregate report will be available in the `coin-collection-manager-aggregator/target/pit-reports` folder).

Instead, if you want to skip all the tests, you can use the parameters `-DskipTests` (to skip unit, integration and end-to-end tests) and `-DskipITs` (to skip only integration and end-to-end ones).

Finally, if you want, for some reason, to run only the UI unit tests, you can do it by using the profile `-PGUItest`.

#### Run tests from Eclipse

For running unit tests from Eclipse you don't need any additional setting, since they are run with TestContainers; instead, for running integration tests and end-to-end tests from Eclipse a running instance (suitably configured) of Postgresql is needed. You can start such an instance in a Docker container with:

```
docker run --rm -p 5432:5432 -e POSTGRES_USER=postgres-user -e POSTGRES_PASSWORD=postgres-password -e POSTGRES_DB=collection postgres:15.1 postgres -c max_connections=300
```

## Run the application

Once built the application you can run it running the jar or using its docker image.

N.B. In case of jar you can also skip the building of the application, downloading the FatJar from the release on GitHub.

### Run through jar

First of all you need to start a Postgresql instance. You can run a docker container with:

```
docker run --name ccm_db -p 5432:5432 -e POSTGRES_USER=<YOUR_USER> -e POSTGRES_PASSWORD=<YOUR_PW> -e POSTGRES_DB=<YOUR_DB> postgres:15.1
```

or start a previously created container with:

```
docker start ccm_db
```

N.B. On Linux and macOS you could have to precede the Docker commands with `sudo`, depending on your system/docker configuration.

Then you can start the application with:

```
java -jar ./coin-collection-manager-app/target/coin-collection-manager-app-1.0.0-jar-with-dependencies.jar --postgres-url=localhost --postgres-port=5432 --postgres-db=<YOUR_DB> --postgres-user=<YOUR_USER> --postgres-password=<YOUR_PW>
```

Obviously you can also use another instance of Postgresql modifying also the `--postgres-url` and `--postgres-port` arguments.

Launching it with `--help` argument you can obtain more information about accepted arguments.

### Run through Docker

If you build the app with the `-Pdocker-build` profile, it will be built a docker image of the application.

You can run the application in one command thanks to Docker compose, that will also start the Postgresql instance.

The command is the following and must be run in the project root folder, but, ***before that, you need to set up the graphical environment***: you can found instructions in the next sections depending on your OS.

```
docker compose up
```

#### Linux

In Linux OSes the only thing to do is to disable the access control to the X server (if you don't have already done) with

```
xhost +
```

and you are ready to `sudo docker compose up`.

#### Windows

In Windows in order to run a GUI app through Docker (that will run a Linux container, since Postgresql doesn't exist for Windows) you have two options: installing an X server (like VcXsrv) or exploiting the new WSLg (for Windows 11 and Windows 10 Build 19041 or later).

##### X server

To use an X server you only need to:
- run it,
- disable the access control and
- set a `DISPLAY` variable pointing to the display (with the IP of your machine, that you can find with `ipconfig`):

```
set DISPLAY=<YOUR_IP>:0.0
```

- run `docker compose up`

###### VcXsrv

A possible X server is VcXsrv, that you can download from [here](https://sourceforge.net/projects/vcxsrv/).

One downloaded and installed, you can run it choosing "*multiple windows*" and "*start no client*" options and checking the "*disable access control*" option.

Then you can set the `DISPLAY` variable and finally start the application with Docker compose (as seen in the previous sections).

##### WSLg

To use [WSLg](https://github.com/microsoft/wslg) the first thing to do is to install the last version of WSL from the [Microsoft Store](https://aka.ms/wslstorepage) or to update it if a previous version is already installed:

```
wsl --update
```

N.B. if Docker was open you may need to restart it after WSL update.

Now you have to set the `DISPLAY` variable:

```
set DISPLAY=:0
```

and you are ready to run the application with Docker compose. In this case you need to override a configuration for the app container, so the command become:

```
docker compose -f docker-compose.yaml -f docker-compose.wslg.yaml up
```