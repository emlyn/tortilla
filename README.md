# tortilla - a thin Clojure wrapper for Java classes

> My mom cooked the same food every day - tortillas, beans and meat.
> If it was enchiladas, it was - tortillas, beans and meat.
> If it was burritos, it was still - tortillas, beans and meat.\
> &nbsp; &nbsp; &nbsp; &nbsp; — *Felipe Esparza*

[![Build](https://github.com/emlyn/tortilla/workflows/Build/badge.svg)](https://github.com/emlyn/tortilla/actions?query=workflow%3ABuild)
[![Codecov](https://img.shields.io/codecov/c/github/emlyn/tortilla.svg)](https://codecov.io/gh/emlyn/tortilla)
[![Dependencies Status](https://versions.deps.co/emlyn/tortilla/status.svg)](https://versions.deps.co/emlyn/tortilla)

## Usage

Tortilla uses reflection at compile time to automatically generate reflection-free
idiomatic Clojure function wrappers around Java class methods.

It is based on a [post](https://clojureverse.org/t/generating-reflection-free-java-wrappers/4421)
by [Arne Brasseur](https://github.com/plexus) on [ClojureVerse](https://clojureverse.org/).
Since the code was only available in a [gist](https://gist.github.com/plexus/645f133fc4c154d1b7497c1b63efdf24),
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
