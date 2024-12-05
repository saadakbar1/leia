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
- Supports event multiplexing, multiplexing one event into different events

## Maven Dependency

- The bom is available at

```
<dependency>
    <groupId>com.grookage.leia</groupId>
    <artifactId>leia-bom</artifactId>
    <version>latest</version>
</dependency>
```

## Build Instructions

- Clone the source:

      git clone github.com/grookage/leia

- Build

      mvn install

## Getting Started

### Using the schema registry

#### Build your own dropwizard schema server by using the `LeiaElastic` bundle.

```
      new LeiaElasticBundle<TestConfiguration, SchemaUpdater>() {

             @Override
             protected Supplier<SchemaUpdaterResolver<SchemaUpdater>> userResolver(TestConfiguration configuration) {
                 return () -> new DefaultResolver();
             }

             @Override
             protected CacheConfig getCacheConfig(TestConfiguration configuration) {
                 return null;
             }

             @Override
             protected Supplier<VersionIDGenerator> getVersionIDGenerator() {
                 return () -> new DefaultVersionGenerator();
             }

             @Override
             protected ElasticConfig getElasticConfig(TestConfiguration configuration) {
                 return null;
             }
         }

```

- **SchemaUpdater** is an RBAC governing class, please extend this SchemaUpdater to implement your own UserProfile
- **CacheConfig** - If the schema will be always resolved from the dataStore (Elasticsearch) or from the in-memory cache
  with a refreshInterval to refresh the data
- **VersionIdGenerator** - Your own version id generator, to generate a unique versionId for every document
- **ElasticConfig** - Elasticsearch configuration to bring up the schema server

#### Ingesting required schemas, backed by a maker-checker process

A sample schema looks like the following

```
{
  "namespace": "testNamespace",
  "schemaName": "testSchema",
  "schemaType": "JSON",
  "attributes": [
    {
      "type": "ARRAY",
      "name": "testAttribute",
      "optional": true,
      "qualifiers": [
        {
          "type": "PII"
        }
      ]
    },
    {
      "type": "ENUM",
      "name": "testAttribute",
      "optional": true,
      "values": [
        "TEST_ENUM"
      ],
      "qualifiers": [
        {
          "type": "PII"
        }
      ]
    }
  ],
  "transformationTargets": [
    {
      "schemaKey": {
        "namespace": "testNamespace",
        "schemaName": "testSchema",
        "version": "v"
      },
      "transformers": [
        {
          "attributeName": "name",
          "transformationPath": "$.userName"
        }
      ]
    }
  ]
}
```

- **AttributeInfo** : There are various type of attributes you can define, please refer to the `SchemaAttribute` class.
- **TransformationTargets** - Helps in event multiplexing, in the above example, when provided with the
  namespace, `testNamespace` and schemaName, `testSchema` with version `V1234`, during message production
  the `LeiaMessageProduceClient`, will multiplex the testSchema to both the versions, the transformationTargets ought to
  be jsonPathRules.

Please refer to the `SchemaBuilder` class which can be used to generate schema against a class.

#### Using the LeiaClientBundle

On the client side, please use the `LeiaClientBundle`

```
    new LeiaClientBundle<AppConfiguration>() {
            @Override
            protected NamespaceDataSource getNamespaceDataSource(AppConfiguration configuration) {
                return new NamespaceDataSource(List.of("schemas"));
            }
            
            @Override
            protected LeiaHttpConfiguration getHttpConfiguration(AppConfiguration configuration) {
                return null;
            }

            @Override
            protected Set<String> getPackageRoots(AppConfiguration configuration) {
                return Set.of("com.example.schemas");
            }
    }
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