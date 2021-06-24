# slackfun

A library to post not so fun quotes to Slack and waste time

## Usage

This library relies on your Slack Token to do authentication. To get your slack token go here https://api.slack.com/web and scroll to the bottom. Put it in a file ~/.slack/token (just a single line with the token).

You will need to have Clojure and lein installed. Download the sourcecode, and in the project directory:

```lein repl```

You are now ready to send stupid and useless quotes:

```clojure
(hello "kpa") ; Say hello to user kpa
(slap "djc") ; Slap user djc with some kind of fish
(chuck "djc") ; Reveal facts about Chuck Norris to user djc
(dune "kw") ; Send a Dune quote to user kw
(bofh "kpa") ; Diagnose whatever problem user kpa is currently having and possibly propose a solution
(access-book "neil") ; Express gratitude to user neil with a useful book
(quest "mw") ; Bestow a noble quest upon user mw
(bruce "kw") ; Send a Bruce Schneier fact to user kw
(lron "kpa") ; Send an ... interesting ... quote about L Ron Hubbard to kpa
(agree "kpa") ; Agree with something insightful that kpa said
(disagree "jk") ; Disagree with something ridiculous that jk said
```

## Editing Funnies

Look at the files in `resources` -- some are simple text files with one line per item, others are JSON formatted.

## Adding New Funnies

If you are adding something straightforward, this is quite easy.

To add a new funny based on an input with a text file, with one line per entry, add this to `slackfun/funny.clj`:

```clojure
(def ^:pandora my-funny "Docstring to be displayed by (funny-list)"
  (create-funny-txt
    "my-funny.txt" ;; Contained in /resources
    "%s does something funny to %s: %s")) ;; First %s becomes "my" name; second is "your" name; third is the random quote
```

Note that `^:pandora` is optional. If you include this, then the function `(pandora "you")` can choose your new funny as one of the random options; if you think your funny is appropriate to be selected by pandora then include the flag, otherwise leave it out.

To add a new funny based on a JSON input file, where the contents are a JSON array of quotes:

```clojure
(def ^:pandora my-funny "Docstring to be displayed by (funny-list)"
  (create-funny-json
    "my-funny.json" ;; Contained in /resources
    "%s does something funny to %s: %s")) ;; First %s becomes "my" name; second is "your" name; third is the random quote
```

If you want to add something that doesn't fit this template you will need a custom implementation.

## License

Copyright © 2015 Kenneth Ayers. The code is released under the Eclipse license.

Distributed under the Eclipse Public License either version 1.0.
