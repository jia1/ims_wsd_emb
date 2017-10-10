cp ../results/se2-lex-w2v-800-exp/* ../results/se2-lex-w2v-800-exp-proc
python proc.py ../results/se2-lex-w2v-800-exp-proc
python comb.py ../results/se2-lex-w2v-800-exp-proc ../se2/1-lexical-sample/processed/test
./scorer.bash fin.result fin.key 2>&1 | tee score.out
cat score.out
