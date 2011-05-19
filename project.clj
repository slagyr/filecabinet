(defproject filecabinet "1.0.4"
  :description "Library to work with files"
  :dependencies [[org.clojure/clojure "1.2.1"]]
  :dev-dependencies [[junit "4.8.2"]
                    [lein-clojars "0.6.0"]]
  :java-source-path "src/"
  :jar-exclusions [#".*Test[\$|\.class]"])
