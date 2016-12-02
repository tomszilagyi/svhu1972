#!/bin/bash
mkdir -p tif; cd tif
for i in `seq -w 25 1020`; do
    if [ ! -f $i.tif ] ; then
        wget -w 0.5 -O $i.tif http://runeberg.org/img/svhu1972/$i.1.tif
    fi
done
cd ..; mkdir -p tif_out
md5sum -c tif.MD5SUM | grep -v OK
