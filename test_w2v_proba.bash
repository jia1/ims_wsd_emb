#!/bin/bash

if [ $# -lt 3 ]; then
    echo "$0 MODEL_DIR TEST_FILE STORE_DIR"
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

if [ $# -ge 5 ]; then
    $JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -is $5 -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb /home/limjiayee/word2vec/w2v-200-1b-win-10.txt \
        -ws 10 -str 'EXP' \
        -type 'directory'
else
    $JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $LIB_DIR/tag.bin.gz \
        -tagdict $LIB_DIR/tagdict.txt \
        -ssm $LIB_DIR/EnglishSD.bin.gz \
        -prop $LIB_DIR/prop.xml \
        -r sg.edu.nus.comp.nlp.ims.io.CFullResultWriter \
        $TEST_FILE $MODEL_DIR $MODEL_DIR $STORE_DIR \
        -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
        -emb /home/limjiayee/word2vec/w2v-200-1b-win-10.txt \
        -ws 10 -str 'EXP' \
        -type 'directory'
fi

# -emb /home/limjiayee/embedding/2017_dim800_vectors.txt \
# -emb /home/limjiayee/word2vec/w2v-800-wikiFeb2017.txt
