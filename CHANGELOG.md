# Changelog

All notable changes to this project will be documented in this file.

## [0.0.1-RC2]
- Added tests to the core functionality to schemaValidator, refresher, client, core and models
- Fixed the validation bug in SchemaValidationUtils. field.getType().isInstanceOf is a miss, Class.isAssignable is the correct way to check for class assignments.
- Some minor code formatting and linting fixes

## [0.0.1-RC1]

- A versioned schema registry, to register various schemas with all primitive and custom data-types, bound by a
  maker-checker process, with customizable RBAC.
- Ability to dynamically create classifications, PII, TIME_SENSITIVE etc
- A RESTful interface and a console to manage the said schemas, to allow for easier integrations
- A client to help serialize and de-serialize the data, at production and consumption levels respectively, with a fluid
  interface that can work with any infrastructure component.
- Supports json to begin with. (Avro and Protobuf shall be accommodated later on)