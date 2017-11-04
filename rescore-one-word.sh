WS_TASK=se4-lex

# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017
EMB_OPT_SRC=w2v-100-win-10-1b
STRAT=exp

WORD=$1
POS=v

./scorer.bash ../results/${WS_TASK}-${EMB_OPT_SRC}-${STRAT}-proc/$WORD.$POS.result \
    ../se4/1-lexical-sample/processed/test/$WORD.key

