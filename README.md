# slackfun

A library to post fun quotes to Slack and waste time

## Usage

You will need to have Clojure and lein installed. Download the sourcecode, and in the project directory:

```lein repl```

Then in the Clojure repl, set your namespace:

```clojure
(use 'slackfun.core)
```

You are now ready to send stupid and useless quotes:

```clojure
(hello "kpa") ; Say hello to user kpa
(trout "djc") ; Slap user djc with some kind of fish
(chuck "djc") ; Reveal facts about Chuck Norris to user djc
(dune "kw") ; Send a Dune quote to user kw
(bofh "kpa") ; Diagnose whatever problem user kpa is currently having and possibly propose a solution
```

## License

Copyright © 2015 Kenneth Ayers. The code is released under the Eclipse license.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
