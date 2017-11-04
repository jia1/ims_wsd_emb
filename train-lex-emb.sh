WS=se2
WS_TASK=${WS}-lex
TRAIN_DIR=../$WS/1-lexical-sample/processed/train

# EMB_OPT_SRC=w2v-100-win-10-1b
EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017

MODEL_DIR=../models-${WS_TASK}-${EMB_OPT_SRC}-${STRAT}
NOHUP_FILE=nohup-train-${WS_TASK}-${EMB_OPT_SRC}-${STRAT}.out

nohup time ./train_one.bash $TRAIN_DIR $TRAIN_DIR $MODEL_DIR \
    &> $NOHUP_FILE &

