MOBAC TileStore utility readme:

Requirements:
**************

Java 1.6 or higher installed

Mobile Atlas Creator v1.9.7 or higher - the main file 
Mobile_Atlas_Creator.jar has to be located in the same directory as ts-util.jar.

Starting:
**************
MOBAC TileStore utility is a command-line application. You have to execute it using the 
following command:

java -jar ts-util.jar

The details reference of the available command-line parameters will then be printed.

Background:
**************
The "tile store" of MOBAC consists of one database per map layer - for simple map 
sources with one layer this means one database per map source.
The different tile store databases are stored as subdirectories in the "tilestore"
directory of MOBAC. The present utility is designed to perform actions on one 
specific tile store database - not on the overall tile store. 