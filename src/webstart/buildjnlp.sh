#!/bin/sh
for F in `ls ../../build/webstart/*.jar`
do
	echo "<jar href=\"`basename $F`\"/>"
done
