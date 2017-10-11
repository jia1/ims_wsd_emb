cp ../results/se4-lex-w2v-800-exp/* ../results/se4-lex-w2v-800-exp-proc
python proc-se4.py ../results/se4-lex-w2v-800-exp-proc
python comb.py ../results/se4-lex-w2v-800-exp-proc ../se4/1-lexical-sample/processed/test
./scorer.bash fin.result fin.key 2>&1 | tee score-se4-lex.out
cat score-se4-lex.out
