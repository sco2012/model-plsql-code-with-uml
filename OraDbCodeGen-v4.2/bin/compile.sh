#!/bin/bash

## openapi.jar comes from within the VP app directory
CLASSPATH=./lib/log4j-1.2.17.jar:./lib/openapi.jar; export CLASSPATH

## Previous versions of JDK tried

## Needs JDK exactly version 11.0.16.1+1
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home
export JAVA_HOME

CODE_HOME=".."

NAME=OraDbCodeGen
VER=v4.2
OUT=out
TMP="."
SRCFILES="$TMP/source.lst"
ID="$NAME-$VER"
ZIP=$ID.zip
PLUGINSRCXML=plugin.xml
PLUGINSRCXMLLOC=conf/$PLUGINSRCXML
LOG4JLOC=conf/log4j.properties
COMPILELOG="$TMP/compile.log"


cd $CODE_HOME
find . -name "*.java" -type f > $SRCFILES


rm -fr $OUT


mkdir -p $OUT/$NAME/classes


# javac -Xlint:unchecked -Xlint:deprecation -d $OUT/$NAME/classes @$SRCFILES
javac -Xlint:unchecked -Xlint:deprecation -d $OUT/$NAME/classes @$SRCFILES 2>$COMPILELOG


sed  "s/{PlsqlPackageGen}/$ID/g" $PLUGINSRCXMLLOC > $OUT/$NAME/$PLUGINSRCXML
mkdir $OUT/$NAME/lib
cp $LOG4JLOC $OUT/$NAME/lib
cp -r ./lib/log*.jar $OUT/$NAME/lib/.


rm -f $SRCFILES


cd $OUT
rm -f $ZIP
zip -q -r $ZIP .
cd ..

# java -version



err=$(cat $COMPILELOG |grep "error")

if [$err -eq ""] 
then
  exit 0
else
  echo $err
  exit 1  
fi




