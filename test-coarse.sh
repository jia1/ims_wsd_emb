WS=se4
WS_TASK=${WS}-all-coarse
TAGGED_CORPUS=omsti
STRAT=exp

# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017
EMB_OPT_SRC=w2v-800-win-10-Camille
MODEL_DIR=../models-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}
TEST_FILE=../${WS}-coarse/eng-coarse-all-words.xml
RESULT_FILE=../results/${WS_TASK}-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}.result
INDEX_SENSE=../WordNet-3.1/dict/index.sense
NOHUP_FILE=nohup-test-${WS_TASK}-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${STRAT}.out

nohup time ./testCoarse.bash \
    $MODEL_DIR $TEST_FILE $RESULT_FILE $INDEX_SENSE \
    &> $NOHUP_FILE &

