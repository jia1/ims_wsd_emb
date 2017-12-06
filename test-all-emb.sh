WS=se${1}
WS_TASK=${WS}-all
TAGGED_CORPUS=omsti
STRAT=exp

# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017
EMB_OPT_SRC=w2v-800-win-10-Camille

# MODEL_DIR=../models-MUN-SC-wn30
MODEL_DIR=../models-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}

# TEST_FILE=../$WS/2-all-words/test/eng-all-words.test.xml
# TEST_FILE=../$WS/2-all-words/test/english-all-words.xml
TEST_FILE=../$WS/2-all-words/fine/test/english-all-words.test.xml

# LEXELT_FILE=../$WS/2-all-words/test/zhong-zhi-files/${WS}.lexelt
LEXELT_FILE=../$WS/2-all-words/fine/test/zhong-zhi-files/${WS}.lexelt

RESULT_FILE=../results/${WS_TASK}-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}.result
INDEX_SENSE=../WordNet-3.1/dict/index.sense
NOHUP_FILE=nohup-test-${WS_TASK}-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}.out

nohup time ./testFine.bash \
    $MODEL_DIR $TEST_FILE $LEXELT_FILE $RESULT_FILE $INDEX_SENSE \
    &> $NOHUP_FILE &

