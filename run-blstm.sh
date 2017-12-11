#!/bin/bash

<<BLSTM
IMS_DIR=$(pwd)
LIB_DIR=$IMS_DIR/lib

JAVA_HOME=$HOME/jdk1.8.0_131/bin
CLASSPATH=$LIB_DIR/*:$IMS_DIR/blstm.jar

export LANG=en_US

# EMB_FILE=$HOME/word2vec/w2v-300-win-10-1b.txt
# EMB_FILE=$HOME/word2vec/w2v-800-win-10-wikiFeb2017.txt

<<SAMPLE_COMMAND
$JAVA_HOME/java -Xmx30G -cp $CLASSPATH \
sg.edu.nus.comp.nlp.ims.classifiers.GravesLSTMCharModellingExample
SAMPLE_COMMAND

$JAVA_HOME/java -Xmx30G -cp $CLASSPATH \
sg.edu.nus.comp.nlp.ims.classifiers.GravesLSTMCharModellingExample
BLSTM

IMS_DIR=$(pwd)
LIB_DIR=$IMS_DIR/lib

MODEL_DIR=../models-se2-lex-w2v-100-win-05-1b-exp
TEST_FILE=../se2/1-lexical-sample/processed/test
STORE_DIR=../results/blstm-results

JAVA_HOME=$HOME/jdk1.8.0_131/bin
# CLASSPATH=$LIB_DIR/*:$IMS_DIR/ims_embed.jar
CLASSPATH=$LIB_DIR/*:$IMS_DIR/blstm.jar

EMB_FILE=$HOME/word2vec/w2v-100-win-05-1b.txt

if [ $# -ge 5 ]; then
    nohup time \
    $JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -is $5 -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb $EMB_FILE \
        -ws 10 -str 'EXP' \
        -type 'directory' \
    > nohup-blstm.out 2>&1 &
else
    nohup time \
    $JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb $EMB_FILE \
        -ws 10 -str 'EXP' \
        -type 'directory' \
    > nohup-blstm.out 2>&1 &
fi

