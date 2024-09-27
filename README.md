# Leia

> "Would it help if I got out and pushed?"
> - Princess Leia to Han after the Millennium Falcon stalls while trying to escape Hoth.

> Nomenclature, the other foundation of botany, should provide the names as soon as the classification is made… If the
> names are unknown knowledge of the things also perishes… For a single genus, a single name.
> - by Carl Linnaeus

Leia is a governance and metadata framework aimed at meeting compliance requirements. It aims at providing

- A versioned schema registry, to register various schemas with all primitive and custom data-types, bound by a
  maker-checker process, with customizable RBAC.
- Ability to dynamically create classifications, PII, TIME_SENSITIVE etc
- A RESTful interface and a console to manage the said schemas, to allow for easier integrations
- A client to help serialize and de-serialize the data, at production and consumption levels respectively, with a fluid
  interface that can work with any infrastructure component.
- Supports multiple types, json, avro and protobuf

## Build Instructions

And

- Clone the source:

      git clone github.com/grookage/leia

- Build

      mvn install

### Maven Dependency

- The bom is available at

```
<dependency>
    <groupId>com.grookage.leia</groupId>
    <artifactId>leia-bom</artifactId>
    <versio>0.0.1-RC3</version>
</dependency>
```

LICENSE
-------

Copyright 2024 Koushik R <rkoushik.14@gmail.com>.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.