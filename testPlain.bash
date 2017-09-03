#!/bin/bash
if [ $# -lt 4 ]; then
    echo "$0 modelDir testFile savePath index.sense split(0/1) tokenized(0/1) pos(0/1) lemmatize(0/1) delimiter(\"/\")"
    exit
fi

bdir=/home/limjiayee/ims_wsd_emb
ldir=$bdir/lib

JAVA_HOME=/home/limjiayee/jdk1.8.0_131/bin
CLASSPATH=$ldir/*:$bdir/ims_embed.jar

modelDir=$1
testFile=$2
savePath=$3
senseIndex=$4

split=0
token=0
pos=0
lemma=0
delimiter="/"

if [ $# -ge 5 ]; then
    split=$5
fi
if [ $# -ge 6 ]; then
    token=$6
fi
if [ $# -ge 7 ]; then
    pos=$7
fi
if [ $# -ge 8 ]; then
    lemma=$8
fi
if [ $# -ge 9 ]; then
    delimiter=$9
fi

export LANG=en_US

if [ $# -ge 4 ]; then
    java -mx2500m -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $ldir/tag.bin.gz \
        -tagdict $ldir/tagdict.txt \
        -ssm $ldir/EnglishSD.bin.gz \
        -prop $ldir/prop.xml \
        -f sg.edu.nus.comp.nlp.ims.feature.CAllWordsFeatureExtractorCombination \
        -c sg.edu.nus.comp.nlp.ims.corpus.CAllWordsPlainCorpus \
        -r sg.edu.nus.comp.nlp.ims.io.CPlainCorpusResultWriter \
        -is $senseIndex \
        $testFile $modelDir $modelDir $savePath \
        -delimiter "$delimiter" -split $split -token $token -pos $pos -lemma $lemma
else
    java -mx2500m -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.implement.CTester \
        -ptm $ldir/tag.bin.gz \
        -tagdict $ldir/tagdict.txt \
        -ssm $ldir/EnglishSD.bin.gz \
        -prop $ldir/prop.xml \
        -f sg.edu.nus.comp.nlp.ims.feature.CAllWordsFeatureExtractorCombination \
        -c sg.edu.nus.comp.nlp.ims.corpus.CAllWordsPlainCorpus \
        -r sg.edu.nus.comp.nlp.ims.io.CPlainCorpusResultWriter \
        $testFile $modelDir $modelDir $savePath \
        -delimiter "$delimiter" -split $split -token $token -pos $pos -lemma $lemma
fi

