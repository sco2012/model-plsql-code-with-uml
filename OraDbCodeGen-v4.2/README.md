# OraDbConGen extension for visual-paradigm

This repo holds the Java code to build a visual-paradigm plugin, which allows for UML class diagrams to be used to generate Oracle database PL/SQL packages.  The plugin has been known to work with all visual-paradigm versions between 2020 and 2026 (version 18.1).

To install the plugin, it must first be compiled from the source.  

## Java requirement
Needs JDK exactly version 11.0.16.1+1

## Compiling

There are many ways to compile Java codes.  This approach involves a Bash script.  From a terminal window, change to the `./bin` directory

```
./compile.sh
```
The script assumes Bash running on MacOS, with visual-paradigm installed in the default location.  For any variation of these conditions, modify the script.


## Deploying

This method involves copying the compiled extension to the appropriate plugins directory. Visual-paradigm provides other deployment methods from the UI

From a terminal window, change to the `./bin` directory

```
./deploy.sh
```
 

As before, the script assumes Bash running on MacOS.  Modify if needed.