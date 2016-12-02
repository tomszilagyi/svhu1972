#!/bin/bash
drawable=../app/src/main/res/drawable-hdpi
if [ ! -d $drawable ] ; then
  mkdir -p $drawable
  ln -s $drawable drawable
fi
for i in tif_out/*.tif; do
  # resource bitmaps must be named in lowercase, beginning with a letter
  # and measure 2048x2048 max
  out=$(echo $i | sed -e 's|tif_out/|drawable/s|' -e 's|[LR]||' -e 's|.tif|.png|')
  if [ ! -f "$out" -o "$i" -nt "$out" ] ; then
    geometry=$(identify $i | awk '{print $3}')
    geom_x=$(echo $geometry | cut -d 'x' -f 1)
    geom_y=$(echo $geometry | cut -d 'x' -f 2)
    if [ $geom_x -ge $geom_y -a $geom_x -gt "2048" ] ; then
        resize="-resize 2048x"
    elif [ $geom_y -gt $geom_x -a $geom_y -gt "2048" ] ; then
        resize="-resize x2048"
    else
        resize=""
    fi
    echo "$i ($geometry) -> $out ($resize)"
    convert $i -negate $resize $out
  fi
done
