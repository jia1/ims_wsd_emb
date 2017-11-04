WS=se3
WS_TASK=${WS}-lex
STRAT=exp

EMB_OPT_SRC=w2v-100-win-10-1b
# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017

MODEL_DIR=../models-${WS_TASK}-${EMB_OPT_SRC}
TEST_DIR=../se2/1-lexical-sample/processed/test
RESULT_DIR=../results/${WS_TASK}-${EMB_OPT_SRC}-${STRAT}
NOHUP_FILE=nohup-test-${WS_TASK}-${EMB_OPT_SRC}-${STRAT}.out

mkdir -p $RESULT_DIR

nohup time ./test_w2v_proba.bash \
    $MODEL_DIR $TEST_DIR $RESULT_DIR \
    &> $NOHUP_FILE &

