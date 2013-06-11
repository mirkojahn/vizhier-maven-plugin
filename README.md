# Visualize Maven Hierarchy Plug-in

The `vizhier-maven-plugin` is an attempt to simplify the understandabilty of a complex Maven project by rendering a dependency graph of its components.

## Features

Currently… not much! This project is driven by my annoyance of having to manually create documentation for stuff that is in constant change anyway and should be rather simple to accomplish with bit of tool support. So here we are with a few hours of hacking - nothing pretty, but it does what I wanted.

- create a DOT diagram file describing the project modules
- include various maven hierarchy types (parent pom, module, regular dependencies\* and imports\* ) for the dependency graph display

\*) not (yet) supported

## General Usage

Go to the root of your multi module project and call:
`mvn net.mjahn.tools:vizhier-maven-plugin:viz -U`

This command will create a *.dot file in the target folder relative to the folder you executed this command. Open the file with a program capable of reading DOT files (like Graphviz for instance).


>Because the plugin is currently still a SNAPSHOT, you will have to checkout this project and `mvn clean install` first to have the plugin locally available.

## Next Steps

The current status of the plugin doesn't support any kind of configuration, although it is sort of prepared to do so. One enhancement would therefore be to include such configuration points for things like:
- dependency filter (artifactId/groupId based?!)
- dependency type filter (what kind of dependencies to include)
- custom output formatter (to support more than just DOT)
- label naming strategies?
- ...
