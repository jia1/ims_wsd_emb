#!/bin/bash

if [ $# -lt 3 ]; then
    echo "Usage:    $0 MODEL_DIR TEST_FILE STORE_DIR index.sense(option)"
    exit
fi

IMS_DIR=/home/limjiayee/ims_wsd_emb
LIB_DIR=$IMS_DIR/lib

MODEL_DIR=$1
TEST_FILE=$2
STORE_DIR=$3

JAVA_HOME=/home/limjiayee/jdk1.8.0_131/bin
CLASSPATH=$LIB_DIR/*:$IMS_DIR/ims_embed.jar

export LANG=en_US

if [ $# -ge 4 ]; then
    $JAVA_HOME/java -Xmx30G -Xms30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -is $4 \
        -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb /home/limjiayee/embedding/context2vec.ukwac.words.targets \
        -ws 10 -str 'FRA'
else
    $JAVA_HOME/java -Xmx30G -Xms30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb /home/limjiayee/embedding/context2vec.ukwac.words.targets \
        -ws 10 -str 'FRA'
fi

