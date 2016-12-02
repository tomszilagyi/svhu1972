#!/bin/bash

mkdir -p resource_txt
for H in html/*.html; do
    T=$(echo $H | sed -e 's|html|txt|g' -e 's|txt/|resource_txt/txt|')
    awk -f mk_txt.awk $H | html2text -utf8 > $T
done
