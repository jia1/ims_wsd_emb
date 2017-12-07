#!/bin/bash

IMS_DIR=$(pwd)
# LIB_DIR=$IMS_DIR/lib

JAVA_HOME=$HOME/jdk1.8.0_131/bin
# CLASSPATH=$LIB_DIR/*:$IMS_DIR/blstm.jar
CLASSPATH=$IMS_DIR/blstm.jar

export LANG=en_US

# EMB_FILE=$HOME/word2vec/w2v-300-win-10-1b.txt
# EMB_FILE=$HOME/word2vec/w2v-800-win-10-wikiFeb2017.txt

# See scorer.bash for more details on Java command line arguments -Xmx and -cp.

<<SAMPLE_COMMAND
$JAVA_HOME/java -Xmx30G -cp $CLASSPATH \
sg.edu.nus.comp.nlp.ims.classifiers.GravesLSTMCharModellingExample
SAMPLE_COMMAND

$JAVA_HOME/java -Xmx30G -cp $CLASSPATH \
    sg.edu.nus.comp.nlp.ims.classifiers.GravesLSTMCharModellingExample

