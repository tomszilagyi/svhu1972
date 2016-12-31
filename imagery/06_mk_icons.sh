#!/bin/bash

opts="-transparent white -define png:compression-level=9 -define png:exclude-chunk=all"
res="../app/src/main/res"

function conv {
    mkdir -p $res/$1
    convert cover/cover.jpg -resize $2x$2 -gravity center -extent $2x$2 $opts $res/$1/ic_launcher.png
}

conv mipmap-xxxhdpi 192
conv mipmap-xxhdpi 144
conv mipmap-xhdpi 96
conv mipmap-hdpi 72
conv mipmap-mdpi 48
