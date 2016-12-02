#!/bin/bash

# post processing: remove the top 6 lines of text for all
# non-chapter-opening pages (first pages of a new letter must be excluded)

for p in `seq 25 1020`; do
    # chapter opening page?
    if [ $p -eq 25 ];   then continue; fi  # A
    if [ $p -eq 70 ];   then continue; fi  # B
    if [ $p -eq 128 ];  then continue; fi  # C
    if [ $p -eq 131 ];  then continue; fi  # D
    if [ $p -eq 154 ];  then continue; fi  # E
    if [ $p -eq 170 ];  then continue; fi  # F
    if [ $p -eq 251 ];  then continue; fi  # G
    if [ $p -eq 287 ];  then continue; fi  # H
    if [ $p -eq 332 ];  then continue; fi  # I
    if [ $p -eq 350 ];  then continue; fi  # J
    if [ $p -eq 357 ];  then continue; fi  # K
    if [ $p -eq 412 ];  then continue; fi  # L
    if [ $p -eq 447 ];  then continue; fi  # M
    if [ $p -eq 489 ];  then continue; fi  # N
    if [ $p -eq 505 ];  then continue; fi  # O
    if [ $p -eq 529 ];  then continue; fi  # P
    if [ $p -eq 562 ];  then continue; fi  # R
    if [ $p -eq 601 ];  then continue; fi  # S
    if [ $p -eq 795 ];  then continue; fi  # T
    if [ $p -eq 865 ];  then continue; fi  # U
    if [ $p -eq 910 ];  then continue; fi  # V, W
    if [ $p -eq 971 ];  then continue; fi  # X
    if [ $p -eq 972 ];  then continue; fi  # Y
    if [ $p -eq 977 ];  then continue; fi  # Z
    if [ $p -eq 978 ];  then continue; fi  # Å
    if [ $p -eq 991 ];  then continue; fi  # Ä
    if [ $p -eq 1002 ]; then continue; fi  # Ö

    # do it
    f=$(printf "resource_txt/txt%04d.txt" $p)
    n=$(cat $f | wc -l)
    echo -n "$p: $f ($n): "
    hd_empty=$(head -6 $f | grep -E "^$" | wc -l)
    if [ $hd_empty -eq 3 ] ; then
        tail -$(($n-6)) $f > $f.tmp
        mv $f.tmp $f
        echo "OK"
    else
        echo "SKIPPED"
    fi
done

## Manually remove the top letter from chapter opening pages!
## (page numbers listed above)
