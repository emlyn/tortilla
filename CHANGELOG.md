# Change Log

## [Unreleased]

- Reduced reflection warnings by tagging argslists forms with the return type
- Automatic coercion of arguments

## [0.1.3] - 2020-10-16

- CLI now automatically generates :refer-clojure :exclude form to avoid warnings from generated code.
- The options map can be excluded from defwrapper if no options need to be set.

## [0.1.2] - 2020-06-25

- Basically 0.1.1, but with the build fixed so that the deployment worked.

## [0.1.1] - not released due to broken build

- CLI option to write generated code to file.
- Make tortilla CLI available as a standalone binary.
- Sort members and arglists for more reproducible output.
- A number of other small tweaks and fixes.

## [0.1.0] - 2020-05-06

- Initial release (too many changes to easily list).
- Should be close to usable, but still not thoroughly tested.

## [Original] - 2019-06-19

- Original code from [@plexus](//github.com/plexus) on [Clojureverse](https://clojureverse.org/t/generating-reflection-free-java-wrappers/4421).

[Unreleased]: //github.com/emlyn/tortilla/compare/0.1.3...HEAD
[0.1.3]: //github.com/emlyn/tortilla/compare/0.1.2...0.1.3
[0.1.2]: //github.com/emlyn/tortilla/compare/0.1.1...0.1.2
[0.1.1]: //github.com/emlyn/tortilla/compare/0.1.0...0.1.1
[0.1.0]:      //github.com/emlyn/tortilla/compare/original...0.1.0
[Original]:   //github.com/emlyn/tortilla/commit/original
