#!/bin/bash

if [ $# -lt 5 ]; then
    echo "Usage:    $0 MODEL_DIR TEST_FILE LEXELT_FILE STORE_DIR index.sense"
    exit
fi

IMS_DIR=/home/limjiayee/ims_wsd_emb
LIB_DIR=$IMS_DIR/lib

MODEL_DIR=$1
TEST_FILE=$2
LEXELT_FILE=$3
STORE_DIR=$4
SENSE_INDEX=$5

JAVA_HOME=/home/limjiayee/jdk1.8.0_131/bin
CLASSPATH=$LIB_DIR/*:$IMS_DIR/ims_embed.jar

export LANG=en_US

$JAVA_HOME/java -mx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
    -ptm $LIB_DIR/tag.bin.gz \
    -tagdict $LIB_DIR/tagdict.txt \
    -ssm $LIB_DIR/EnglishSD.bin.gz \
    -prop $LIB_DIR/prop.xml \
    -c sg.edu.nus.comp.nlp.ims.corpus.CAllWordsFineTaskCorpus \
    -r sg.edu.nus.comp.nlp.ims.io.CAllWordsResultWriter \
    -is $SENSE_INDEX \
    $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
    -f sg.edu.nus.comp.nlp.ims.feature.CAllWordsFeatureExtractorCombination \
    -e sg.edu.nus.comp.nlp.ims.classifiers.CLibLinearEvaluator \
    -lexelt $LEXELT_FILE \
    -emb /home/limjiayee/embedding/2017_dim400_vectors.txt \
    -ws 10 -str 'EXP'

