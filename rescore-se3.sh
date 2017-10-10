cp ../results/se3-lex-w2v-800-exp/* ../results/se3-lex-w2v-800-exp-proc
python proc-se3.py ../results/se3-lex-w2v-800-exp-proc
python comb.py ../results/se3-lex-w2v-800-exp-proc ../se3/1-lexical-sample/processed/test
./scorer.bash fin.result fin.key 2>&1 | tee score-se3-lex.out
cat score-se3-lex.out
