
USING DOBUDISH:
Dobudish is used to create javahelp and pdf end-user documentation
for Atlas. Basis for the documentation is the DocBook AtlasHelp.xml
which (with all its imported files) can be found in
documents/AtlasHelp/input. Pictures and such can be found in 
input's subfolder resource. AtlasHelp.xml includes several other
xml files representing the single chapters (i.e. introduction.xml).

generator.bat or generator.sh are used to create new DocBooks:
   usage: generator.bat book-name create book or
          ./generator.sh book-name create book
          
I.e. AtlasHelp was created with
          ./generator.sh AtlasHelp create book

Dobudish can then create wanted documentation formats with i.e.
          ./generator.sh AtlasHelp javahelp
which creates javahelp in the documents output directory.

The script atlas_help_builder.sh creates both javahelp and pdf
outputs from AtlasHelp.xml. It then copies the javahelp jar files
to Atlas' jar directory and the pdf to its base directory. Other output
formats like several html formats are listed in the dobudish manual 
section 3 (p. 2).

IMPORTANT: For provided graphics (i.e. <warning> to work in javahelp the line

           <xsl:param name="admon.graphics.path">images/admon/</xsl:param>

           has to be added to the document's stylesheet in 
           ...documents/"document"/custom-cfg/javahelp.xsl.

           For AtlasHelp the line was added in
           ...documents/AtlasHelp/custom-cfg/javahelp.xsl line 29.

INTEGRATION IN ATLAS:
The javahelp jar files provide all neccessary functionality for integration
into a java swing application. "AnnotationTool.java" contains the only 
changes in Atlas sources (see javahelp imports and search for 
"// init JavaHelp and add gui menue Help element". The three jars "jh.jar", 
"hsviewer.jar" and "AtlasHelp.jar" must of course be part of the build path
and be distributed with the binaries... .
                    

                    
WHY DOBUDISH:
Creating javahelp files from hand turned out to be no fun - period. Dobudish
also allows to create different output formats and is easy to use as long
as the DocBook xml-files are valid. Only one input source has to be managed
when changing the documentation for different formats.
Dobudish also brings along all it needs to work - no external jars or dependencies
need to be managed, java is neccessary though. The created javahelp output provides
all needed libraries, no external dependencies either for integration into a 
java swing application.
