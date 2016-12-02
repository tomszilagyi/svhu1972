#!/bin/bash

# post processing: every 16th page and two pages after
# (open for manual editing)

for p in `seq 25 16 1020`; do
    f=$(printf "resource_txt/txt%04d.txt" $p)
    nano $f
    # Remove stuff such as:
    # (... one per file, center-ish or bottom ...)
    # 1 Svéd—magyar szótár
    # 2 Svéd—mágyar szótár
done

for p in `seq 27 16 1020`; do
    f=$(printf "resource_txt/txt%04d.txt" $p)
    nano $f
    # Remove stuff such as:
    # (... one per file, center-ish or bottom ...)
    # 1*
    # 2*
done
