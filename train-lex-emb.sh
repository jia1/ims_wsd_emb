WS=se3
WS_TASK=${WS}-lex
STRAT=exp

EMB_OPT_SRC=w2v-100-win-10-1b
# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017

TRAIN_DIR=../$WS/1-lexical-sample/processed/train
MODEL_DIR=../models-${WS_TASK}-${EMB_OPT_SRC}-${STRAT}
NOHUP_FILE=nohup-train-${WS_TASK}-${EMB_OPT_SRC}-${STRAT}.out

mkdir -p $MODEL_DIR

nohup time ./train_one.bash $TRAIN_DIR $TRAIN_DIR $MODEL_DIR \
    &> $NOHUP_FILE &

