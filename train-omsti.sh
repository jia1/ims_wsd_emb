POS=adv

TAGGED_CORPUS=omsti
DATA_DIR=../${TAGGED_CORPUS}-data

# EMB_OPT_SRC=w2v-800-win-10-wikiFeb2017
EMB_OPT_SRC=w2v-100-win-10-1b

MODEL_DIR=../models-${TAGGED_CORPUS}-${EMB_OPT_SRC}

nohup time ./train_one.bash $DATA_DIR/$POS $DATA_DIR/$POS \
    $MODEL_DIR &> nohup-train-${TAGGED_CORPUS}-${EMB_OPT_SRC}-${POS}.out &

