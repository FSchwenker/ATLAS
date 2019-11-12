#!/bin/sh

# usage of dobudish: ./generator.sh <project-name> <out-file-type>
# for all out-file-types and their usage see 
# dobudish-manual.pdf section 3 - Features (p. 2)

# create javahelp and pdf from docbook AtlasHelp.xml
./generator.sh AtlasHelp javahelp
JAVAHELP_EXIT=$?
./generator.sh AtlasHelp pdf
PDF_EXIT=$?
# ./generator.sh AtlasHelp html
# ./generator.sh AtlasHelp xhtml


echo

if [ $JAVAHELP_EXIT -eq 0 ]
then
  cp documents/AtlasHelp/output/javahelp/*.jar ./../../jars/
  if [ $? -ne 0 ]
  then echo "copying jar files failed"
  fi
  echo "javahelp generation: ok"
else
  echo "javahelp generation: failed - exitcode "$JAVAHELP_EXIT
fi

if [ $PDF_EXIT -eq 0 ]
then
  cp documents/AtlasHelp/output/pdf/AtlasHelp.pdf ./../../AtlasHelp.pdf
  if [ $? -ne 0 ]
  then echo "copying pdf failed"
  fi
  echo "pdf generation:      ok"
else
  echo "pdf generation:      failed - exitcode "$PDF_EXIT
fi

