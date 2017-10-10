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
            split_line = line.split(' ')
            if len(split_line) > 1:
                split_line[0] = split_line[1].split('.')[0]
            processed_lines.append(' '.join(split_line))
    with open(result_file, 'w') as g:
        g.writelines(processed_lines)

if os.path.isdir(result_file):
    files = [f for f in os.listdir(result_file) if os.path.isfile(f)]
    for f in files:
        process_one(f)
else:
    process_one(result_file)
