#!/bin/bash
images=../app/src/main/assets/images
if [ ! -d $images ] ; then
  mkdir -p $images
fi
if [ ! -d images ] ; then
  ln -s $images images
fi
max=2048
for i in tif_out/*.tif; do
  # resize asset images to measure at most $max pixels in either dimension
  out=$(echo $i | sed -e 's|tif_out/|images/|' -e 's|[LR]||' -e 's|.tif|.png|')
  if [ ! -f "$out" -o "$i" -nt "$out" ] ; then
    geometry=$(identify $i | awk '{print $3}')
    geom_x=$(echo $geometry | cut -d 'x' -f 1)
    geom_y=$(echo $geometry | cut -d 'x' -f 2)
    if [ $geom_x -ge $geom_y -a $geom_x -gt $max ] ; then
        resize=$(printf '%cresize %d%c' - $max x)
    elif [ $geom_y -gt $geom_x -a $geom_y -gt $max ] ; then
        resize="-resize x$max"
    else
        resize=""
    fi
    echo "$i ($geometry) -> $out ($resize)"
    convert $i -negate $resize $out
  fi
done
