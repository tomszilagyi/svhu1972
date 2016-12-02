#!/bin/bash

echo Downloading HTML files with OCR output...
mkdir html; cd html
for i in `seq -w 25 1020` ; do wget -nd -nH -P. -l 0 http://runeberg.org/svhu1972/$i.html ; done
cd ..
