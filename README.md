# tortilla - a thin Clojure wrapper for Java classes

> My mom cooked the same food every day - tortillas, beans and meat.
> If it was enchiladas, it was - tortillas, beans and meat.
> If it was burritos, it was still - tortillas, beans and meat.\
> &nbsp; &nbsp; &nbsp; &nbsp; — *Felipe Esparza*

[![Build](https://github.com/emlyn/tortilla/workflows/Build/badge.svg)](https://github.com/emlyn/tortilla/actions?query=workflow%3ABuild)
[![Coverage Status](https://coveralls.io/repos/github/emlyn/tortilla/badge.svg?branch=HEAD)](https://coveralls.io/github/emlyn/tortilla?branch=HEAD)
[![Dependencies Status](https://versions.deps.co/emlyn/tortilla/status.svg)](https://versions.deps.co/emlyn/tortilla)
[![Downloads](https://img.shields.io/clojars/dt/emlyn/tortilla.svg)](https://clojars.org/emlyn/tortilla)

## Warning

This library is very experimental, and the current implementation is not efficient.
In fact, IT IS SLOWER THAN NORMAL UNHINTED INTEROP CODE (see [comment](https://github.com/emlyn/tortilla/issues/20#issuecomment-1646567785)), so it is not recommended for use at the moment.

## Introduction

Interfacing to Java libraries from Clojure can be ugly.
You have to use the special interop syntax,
and either sprinkle type hints all over the place,
or risk having runtime reflection killing performance.

Calling vararg methods is also clumsy, as normal Java interop in Clojure
doesn't have any special handling for them, so you have to manually wrap
the variable arguments in a Java array using e.g. `into-array`.

Java functions also tend to expect different types which need special handling to convert the usual Clojure values into the exoected types.
For example functions that expect an `Integer` or a `Float` need the argument to be wrapped in `(int x)` or `(float y)`, those that expect an array need a Clojure vector or list to be wrapped in `(into-array TYPE my_vec)`.
Additionally, in Java 8 there are Funcional Interfaces (that accept a lambda expression in Java), but from Clojure you need to wrap the Clojure function in `proxy` or `reify` to convert it to the right type.

Tortilla aims to remedy this by using reflection at compile time to
automatically generate reflection-free idiomatic Clojure function wrappers
around Java class methods.
These wrappers then know the parameter types of the various candidate overloads and can select the correct one with simple type checks that are much faster than full runtime reflection.
They provide variable-arity functions to wrap vararg methods so that
they can be called idiomatically from Clojure.
Tortilla also includes support for automatically coercing Clojure types.
So, for example, you can just pass normal Clojure longs and doubles, and tortilla will coerce them to Integer and Float respectively, when necessary.
You can also pass in a Clojure vector when an array is excepcted, or a Clojure function when a Functional Interface is expected (e.g. `java.lang.function.Function`, `java.io.FileFilter`...)

Tortilla is still in the early alpha stages of development
and has not yet been heavily tested,
so it is not reecommended for use in production code.
However, the basic functionality is mostly in place,
so feel free to give it a try, and provide feedback
if you find anything that doeesn't work,
or any functionality that's missing.

## Usage

There are two ways to use Tortilla: either as a macro that you call in your code to
generate the wrapper functions at compile time, or as a command-line interface (CLI)
that you run as a separate step to generate Clojure source files that you then build
with the rest of your code. There are pros and cons to both approaches:

- Macro mode can adapt to different versions of the wrapped Java library automatically,
just by changing the dependency.
- Macro mode doesn't require a separate step to generate any Clojure wrapper source files,
as everything happens during the normal build process.
- CLI mode is easier to debug, as the generated source is available to inspect.
- CLI mode tends to generate better error messages when something goes wrong, since they can refer to lines in the generated code as opposed to just the macro call.
- CLI mode can automatically add a `:refer-clojure :exclude` clause to the `ns` form to avoid compiler warrnings about redefined symbols.
- others...?

### Macro mode

#### Installation

Add Tortilla as a dependency to your project.
The latest version is:

[![Clojars Project](https://clojars.org/emlyn/tortilla/latest-version.svg)](https://clojars.org/emlyn/tortilla)

#### Defining wrappers

You define wrappers for a Java class by calling the `defwrapper` macro, for example:

``` clojure
;; define the wrapper functions:
(defwrapper java.io.File)

;; create a File object
(def f (clojure.java.io/file "project.clj"))

;; call the generated functions on the File object
(get-name f)      ;; => "project.clj"
(is-directory f)  ;; => false
(exists f)        ;; => true
(length f)        ;; => 2996
(last-modified f) ;; => 1602755163000
```

In the above example, Clojure will emit a warning like:

``` text
WARNING: list already refers to: #'clojure.core/list in namespace: user, being replaced by: #'user/list
```

This is because the File class has a member function called `list`, so when the wrapper function is created, this clashes with the `list` function in Clojure core.
There are two ways to work around this:

Either you can exclude the Clojure core functions from being imported by adding a clause to your `ns` form like:

``` clojure
(:refer-clojure :exclude [list])
```

If you use the CLI mode (see below), this can be handled automatically. If you still need to refer to the excluded function, you can still access it using its fully-qualified name (`clojure.core/list`).

Alternatively, you can add a prefix to all generated function names when generating the wrappers:

``` clojure
(defwrapper java.io.File {:prefix "f-"})

(f-get-name f) ;; => "project.clj"
```

Note that it's still possible to get clashes with a prefix, for example if a method is called `cat` and you use a prefix of `lazy-`, the generated function will clash with Clojure's `lazy-cat`.

#### Filtering

#### Coercion

Tortilla can handle the automatic coercion of values to Java types.
By default it will coerce:
- Long values to Integers, so Java functions that expect Integers can be called with `(f 1)` instead of manually coercing like `(f (int 1))`.
- Double values to Floats, so you can use `(f 1.0)` instead of `(f (float 1.0))`.
- Keywords to Enums, so instead of having to include `(:import [package.name EnumClass])` in your ns declaration then use `(f EnumClass/VALUE)`, you can just use `(f :VALUE)`.
- Vectors to Java arrays, so if a function expects an array of Strings, you can just use `(f ["Hello" "world"])` instead of `(f (into-arrray String ["Hello" "world"]))`.
- Functions into Java Functional Interfaces (types that accept lambda expressions in Java 8+), so you can call, for example java.io.File::list, as `(list file #(str/ends-with? %2 ".txt"))` instead of having to do something like:
  ```clojure
  (let [filter (reify java.io.FilenameFilter
                 (accept [_self _dir name]
                   (str/ends-with? name ".txt")))]
    (list file filter))
  ```
  Note that tortilla currently by default only supports a predetermined list of Functional Interfaces (listed in `tortilla.coerce`).
  Support for other types can be added by calling the `tortilla.coerce/coerce-fn-impl` macro with the new Functional Interface.

The default coerce implementation can be extended by extending the `Coercible` protocol in `tortilla.coerce` to new Clojure types and/or defining new methods for the `coerce-long`, `coerce-double`, `coerce-kw`, `coerce-vector` and `coerce-fn` multimethods.

Alternatively, a completely different implementation can be passsed in to `defwrapper` (or the keyword `:none` to disable coercion). The new function should accept two arguments: the clojure value and a `Class` object representing the target type. It should return either a value of the target type if it can coerce successfully, or the original value unaltered.

### CLI Mode

#### Installing

You can download the latest CLI executable from [github](//github.com/emlyn/tortilla/releases),
or build it from source with `lein bin` in a clone of this repository, which will put the executable in the `bin` subdirectory.

#### Running

You can get an overview of the options available with the `--help`/`-h` option:

```text
> ./bin/tortilla -h
Usage: tortilla [options]

Options:
  -c, --class CLASS               Class to generate a wrapper. May be specified multiple times.
  -m, --members                   Print list of class members instead of wrapper code (useful for checking -i/-x).
  -i, --include REGEX             Only wrap members that match REGEX. Match members in format name(arg1.type,arg2.type):return.type
  -x, --exclude REGEX             Exclude members that match REGEX from wrapping.
  -n, --namespace NAMESPACE       Generate ns form at start of output with given name.
      --[no-]refer-clojure        Generate refer-clojure clause excluding any wrapped names.
      --coerce SYMBOL             Use SYMBOL for coercion (or 'none' to disable, empty for default).
  -p, --prefix PREFIX             Prefix generated function names (useful to avoid conflicts with clojure.core names.)
  -o, --out FILE                  Write generated output to FILE.
      --[no-]metadata             Include metadata in output.
      --[no-]instrument           Instrument specs.
      --[no-]unwrap-do            Unwrap 'do' form around defns.
  -w, --width CHARS          100  Limit output width.
  -d, --dep COORD                 Add jars to classpath. May be specified multiple times. COORD may be in leiningen format ('[group/artifact "version"]') or maven format (group:artifact:version). In both cases the group part is optional, and defaults to the artifact ID.
  -v, --version                   Display version information.
  -h, --help                      Display this help.
```

Here is an example of using the CLI to generate a wrapper file for the File class:

``` text
# Generate wrapper file:
> ./bin/tortilla --class java.io.File --namespace java.io.file --out src/java/io/file.clj

# Look at the start of the generated file:
> head -n 10 src/java/io/file.clj
(ns java.io.file
  (:refer-clojure :exclude [list])
  (:require [tortilla.wrap]))

;; ==== java.io.File ====

(clojure.core/defn can-execute
  {:arglists '([java.io.File])}
  (^{:tag java.lang.Boolean}
   [p0_271]
```

## Inspiration

Tortilla is based on a
[post](https://clojureverse.org/t/generating-reflection-free-java-wrappers/4421)
by [Arne Brasseur](https://github.com/plexus)
on [ClojureVerse](https://clojureverse.org/).
Since the code was only available in a
[gist](https://gist.github.com/plexus/645f133fc4c154d1b7497c1b63efdf24),
I took the liberty of cloning it into a full repo and developing it further.

## License

Copyright © 2019 Arne Brasseur and Emlyn Corrin

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
