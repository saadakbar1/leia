# Leia [![Build](https://github.com/grookage/leia/actions/workflows/build.yml/badge.svg)](https://github.com/grookage/leia/actions/workflows/build.yml)

> Do raindrops follow a certain hierarchy when they fall?
> - by Anthony T. Hincks

Leia is a KV config store template, for making versioned config store handlings simple. 
- Supports versioning of a configType
- Manages rollout of a version, in percentages
- Provides type-safe generic interface for integration with support for custom serializers and deserializers
- Has default implementations for an In-Memory Store, Aerospike and MySQL, with sharding enabled for the latter. 
- Provides a leia-client to offer a simple interface to work with the building blocks
- Provides an RBAC implementation, with plugging in a custom one if needed, and audits the changes to the said configs, via post-processors
- Makes plugging in custom storage layer simple
- Additionally provides a leia-server (atop dropwizard)

## Why?

Versioned config management, with rollouts is a typical problem across organizations, and there is a lot of boilerplate code to achieve this. Leia provides a easy spin-up of the same; can even double up as a cataloguing engine, should it have to. 

## Build Instructions

- Clone the source:

      git clone github.com/grookage/leia

- Build

      mvn install

### Maven Dependency

```
<dependency>
    <groupId>com.grookage.apps</groupId>
    <artifactId>leia</artifactId>
    <versio>0.0.1-RC1</version>
</dependency>
```