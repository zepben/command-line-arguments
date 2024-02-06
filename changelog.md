# Command Line Arguments changelog

## [2.1.0] - UNRELEASED

### Breaking Changes

* Updated the super pom with potentially breaking transitive dependencies.

### New Features

* None.

### Enhancements

* None.

### Fixes

* None.

### Notes

* None.

## [2.0.0] - 2023-03-16

### Breaking Changes

* Updated to a Kotlin code base which includes the following changes:
    * Converted accessor functions to Kotlin properties. Accessing these via Kotlin requires removing the `()`, whilst in Java you need to change them
      to `get*()`
    * Removal of the `ensureOptionInitialised` helper. Accessing options without parsing will now throw `UninitializedPropertyAccessException` errors instead.
    * Removed the `ParseException` annotations from functions. The function still throw these exceptions, but Kotlin does not require them.
    * Returning `null` instead of `Optional` from argument extractors. If you wish to continue using `Optional` from a Java codebase, you can just wrap the
      result.

### New Features

* None.

### Enhancements

* None.

### Fixes

* Updated Apache commons-cli to v1.5.0 to resolve issue with parsing some arguments with optional/multiple arguments consuming following arguments.

### Notes

* The changes due to the Kotlin code base will not be visible to the user, they just change the way you need to implement and interface with the `CmdArgsBase`
  class.

## [1.1.0]

Initial open source release of command-line-arguments.
