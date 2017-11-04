WS=se3
WS_TASK=${WS}-lex

# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017
EMB_OPT_SRC=w2v-100-win-10-1b

STRAT=exp

RESULT_DIR=../results/${WS_TASK}-${EMB_OPT_SRC}-${STRAT}
PROC_RESULT_DIR=${RESULT_DIR}-proc

KEY_DIR=../$WS/1-lexical-sample/processed/test

mkdir -p $PROC_RESULT_DIR

cp $RESULT_DIR/* $PROC_RESULT_DIR
python proc-${WS}.py $PROC_RESULT_DIR
python comb.py $PROC_RESULT_DIR $KEY_DIR

./scorer.bash fin.result fin.key \
    > score-${WS}-${EMB_OPT_SRC}.out
cat score-${WS}-${EMB_OPT_SRC}.out

