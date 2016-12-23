#!/bin/bash
javac TextPrepare.java
rc=$?
if [ $rc -ne 0 ] ; then
    echo "Compilation failed, exiting."
    exit $rc
fi
java TextPrepare $@
