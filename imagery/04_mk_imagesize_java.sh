#!/bin/bash

out=../app/src/main/java/io/github/tomszilagyi/svhu1972/ImageSize.java

cat <<EOF > $out
/* GENERATED FILE -- see imagery/04_mk_imagesize_java.sh */

package io.github.tomszilagyi.svhu1972;

public class ImageSize {
    public static final int[][] size =
EOF

cnt=0
echo -n "Reading image sizes"
for i in drawable/*.png; do
  file=$(basename $i)
  geometry=$(identify $i | awk '{print $3}')
  geom_x=$(echo $geometry | cut -d 'x' -f 1)
  geom_y=$(echo $geometry | cut -d 'x' -f 2)

  if [ $cnt -eq 0 ] ; then
      echo -n "        { " >> $out
  else
      echo -n "        , " >> $out
  fi
  cnt=$(($cnt+1))
  printf "{%4d, %4d} // %s\n" $geom_x $geom_y $file >> $out
  echo -n "."
done
echo "done"
cat <<EOF >> $out
    };

    public static int x(int page) {
        if (page >= size.length) return 710;
        return size[page][0];
    };

    public static int y(int page) {
        if (page >= size.length) return 2048;
        return size[page][1];
    };
}
EOF
