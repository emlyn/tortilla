# tortilla - a thin Clojure wrapper for Java classes

> My mom cooked the same food every day - tortillas, beans and meat.
> If it was enchiladas, it was - tortillas, beans and meat.
> If it was burritos, it was still - tortillas, beans and meat.\
> &nbsp; &nbsp; &nbsp; &nbsp; — *Felipe Esparza*

[![Build](https://github.com/emlyn/tortilla/workflows/Build/badge.svg)](https://github.com/emlyn/tortilla/actions?query=workflow%3ABuild)
[![Coverage Status](https://coveralls.io/repos/github/emlyn/tortilla/badge.svg?branch=HEAD)](https://coveralls.io/github/emlyn/tortilla?branch=HEAD)
[![Dependencies Status](https://versions.deps.co/emlyn/tortilla/status.svg)](https://versions.deps.co/emlyn/tortilla)
[![Downloads](https://img.shields.io/clojars/dt/emlyn/tortilla.svg)](https://clojars.org/emlyn/tortilla)

## Introduction

Interfacing to Java libraries from Clojure can be ugly.
You have to use the special interop syntax,
and either sprinkle type hints all over the place,
or risk having runtime reflection killing performance.

Tortilla aims to remedy this by using reflection at compile time to
automatically generate reflection-free idiomatic Clojure function wrappers
around Java class methods.
These wrappers then know the parameter types of the various candidate overloads
and can select the correct one with simple type checks that are much faster
than full runtime reflection.

Tortilla is still in the early alpha stages of development
and has not yet been heavily tested,
so it is not reecommended for use in production code.
However, the basic functionality is mostly in place,
so feel free to give it a try, and provide feedback
if you find anything that doeesn't work,
or any functionality that's missing.

## Usage

There are two ways to use Tortilla: either as a macro that you call in your code to
generqte the wrapper functions at compile time, or as a command-line interface (CLI)
that you run before to generate Clojure source files that you then build with the
rest of your code. There are pros and cons to both approaches.

### Installation

#### Macro

Add Tortilla as a dependency to your project.
The latest version is:

[![Clojars Project](https://clojars.org/emlyn/tortilla/latest-version.svg)](https://clojars.org/emlyn/tortilla)

#### CLI

You can download the latest CLI executable from [github](//github.com/emlyn/tortilla/releases),
or build it from source with `lein bin` in a clone of this repository.

#### Pros & Cons

- Macro mode can adapt to different versions of the wrapped Java library automatically,
just by changing the dependency.
- Macro mode doesn't require a separate step to generate any Clojure wrapper source files,
as everything happens during the normal build process.
- CLI mode is easier to debug, as the generated source is available to inspect.
- CLI mode generates better error messages, since they can refer to lines in the generated code.
- others...?

### Filtering

### Coercion

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
