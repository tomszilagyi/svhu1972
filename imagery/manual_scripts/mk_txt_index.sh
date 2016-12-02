#!/bin/bash
for i in `seq -w 25 1020` ; do echo -n "$i - "; cat html/$i.html | awk -f getidx.awk ; done > resource_txt/index.txt
