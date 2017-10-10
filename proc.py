import os
import sys

args = sys.argv

if len(args) != 2:
    exit('Usage: python proc.py result_file/directory')

result_file = os.path.abspath(args[1])

def process_one(result_file):
    processed_lines = []
    with open(result_file, 'r') as f:
        for line in f:
            split_line = line.strip().split(' ')
            if len(split_line) >= 3:
                word_id = split_line[1].split('.')[0].split('%')[0]
                best_sense, best_prob = '', 0
                for i in range(2, len(split_line)-1, 2):
                    curr_sense, curr_prob = split_line[i], split_line[i+1]
                    if curr_prob > best_prob:
                        best_sense, best_prob = curr_sense, curr_prob
                if split_line[1].split('.')[0] == word_id:
                    processed_lines.append('{0} {1} {2}\n'.format(word_id, split_line[1], best_sense))
                else:
                    processed_lines.append('{0} {1} {2}\n'.format(word_id, split_line[0], best_sense))
    with open(result_file, 'w') as g:
        g.writelines(processed_lines)

if os.path.isdir(result_file):
    for f in os.listdir(result_file):
        process_one(os.path.join(result_file, f))
else:
    process_one(result_file)
