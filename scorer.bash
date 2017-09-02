#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Usage:    $0 test.result test.key"
    exit
fi

IMS_DIR=/home/limjiayee/ims_wsd_emb
LIB_DIR=$IMS_DIR/lib

TEST_FILE=$1
MODEL_DIR=$2
STORE_DIR=$3

JAVA_HOME=/home/limjiayee/jdk1.8.0_131/bin
CLASSPATH=$LIB_DIR/*:$IMS_DIR/ims_embed.jar

export LANG=en_US

# -cp -classpath
# Specifies a list of directories, JAR files, and ZIP archives to search for class files.
# Separate class path entries with semicolons (;).
# Specifying -classpath or -cp overrides any setting of the CLASSPATH environment variable.
# If -classpath and -cp are not used and CLASSPATH is not set, then the user class path consists of the current directory (.).

# -Xmxn
# Specifies the maximum size, in bytes, of the memory allocation pool.
# This value must a multiple of 1024 greater than 2 MB.
# Append the letter k or K to indicate kilobytes, or m or M to indicate megabytes.
# The default value is chosen at runtime based on system configuration.

# $@ or $*
# http://wiki.bash-hackers.org/scripting/posparams

$JAVA_HOME/java -mx2048m -cp $CLASSPATH sg.edu.nus.comp.nlp.ims.util.CScorer $@

<<SAMPLE_OUTPUT
score for [example.result] using key [examples/test.key]:
precision: 0.409 (54.00 correct of 132.00 attempted)
recall: 0.409 (54.00 correct of 132.00 in total)
attempted: 1.000 (132.00 attempted of 132.00 in total)
SAMPLE_OUTPUT

