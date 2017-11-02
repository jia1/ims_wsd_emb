# nohup time ./train_one.bash ../omsti-data/XXXX ../omsti-data/XXXX ../models-omsti-emb &> nohup-train-omsti-XXXX.out &
# nohup time ./train_one.bash ../omsti-data/YYYY ../omsti-data/YYYY ../models-omsti-emb-1b-100-win-05 &> nohup-train-omsti-1b-100-win-05-YYYY.out &
# nohup time ./train_one.bash ../omsti-data/ZZZZ ../omsti-data/ZZZZ ../models-omsti-emb-1b-200-win-10 &> nohup-train-omsti-1b-200-win-10-ZZZZ.out &
nohup time ./train_one.bash ../omsti-data/adv ../omsti-data/adv ../models-omsti-emb-wikiFeb2017 &> nohup-train-omsti-800-wikiFeb2017-adv.out &
