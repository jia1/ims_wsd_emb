import os
import sys
import shutil

args = sys.argv

if len(args) != 3:
    exit('Usage: python comb.py result_dir key_dir')

result_dir, key_dir = args[1:]

result_dir = os.path.abspath(result_dir)
key_dir = os.path.abspath(key_dir)

print(result_dir, key_dir)

fin_result_fn, fin_key_fn = 'fin.result', 'fin.key'

def comb(src_dir, src_ext, dst_fn):
    with open(dst_fn, 'wb') as f:
        for fn in os.listdir(src_dir):
            if fn.endswith(src_ext) and fn != dst_fn:
                with open(os.path.join(src_dir, fn), 'rb') as g:
                    shutil.copyfileobj(g, f)

comb(result_dir, '.result', fin_result_fn)
comb(key_dir, '.key', fin_key_fn)

