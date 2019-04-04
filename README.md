 clj -Sdeps '{:deps {cider/cider-nrepl {:mvn/version "0.18.0-SNAPSHOT"} }}' -e '(require (quote cider-nrepl.main)) (cider-nrepl.main/init ["cider.nrepl/cider-middleware"])'

use the command above can let cider connect to the repl for plain clj
