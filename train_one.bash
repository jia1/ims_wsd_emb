#!/bin/bash

if [ $# -lt 3 ]; then
    echo "Usage:    $0 train.xml train.key OUTPUT_DIR s2 c2"
    echo ""
    echo "s2:       Cut-off for surrounding words   (default 0)"
    echo "c2:       Cut-off for collocation         (default 0)"
    exit
fi

s2=0
if [ $# -gt 3 ]; then
    s2=$4
fi

c2=0
if [ $# -gt 4 ]; then
    c2=$5
fi

# IMS_DIR=/home/limjiayee/ims_wsd_emb
IMS_DIR=$(pwd)
LIB_DIR=$IMS_DIR/lib

JAVA_HOME=$HOME/jdk1.8.0_131/bin
CLASSPATH=$LIB_DIR/*:$IMS_DIR/ims_embed.jar

export LANG=en_US

# EMB_FILE=$HOME/embedding/context2vec.ukwac.words.targets
EMB_FILE=$HOME/word2vec/w2v-300-win-10-1b.txt
# EMB_FILE=$HOME/word2vec/w2v-800-win-10-wikiFeb2017.txt

# See scorer.bash for more details on Java command line arguments -Xmx and -cp.

<<SAMPLE_COMMAND
$JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTrainModel
-prop $LIB_DIR/prop.xml
-ptm $LIB_DIR/tag.bin.gz
-tagdict $LIB_DIR/tagdict.txt
-ssm $LIB_DIR/EnglishSD.bin.gz
$1 $2 $3
-f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination
-s2 $s2 -c2 $c2
-emb $EMB_FILE
-ws 10 -str 'EXP'
SAMPLE_COMMAND

$JAVA_HOME/java -Xmx30G -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTrainModel \
    -prop $LIB_DIR/prop.xml \
    -ptm $LIB_DIR/tag.bin.gz \
    -tagdict $LIB_DIR/tagdict.txt \
    -ssm $LIB_DIR/EnglishSD.bin.gz \
    $1 $2 $3 \
    -f sg.edu.nus.comp.nlp.ims.feature.CFeatureExtractorCombination \
    -s2 $s2 -c2 $c2 \
    -emb $EMB_FILE \
    -ws 10 -str 'EXP' \
    -type 'directory' \

