R E A D M E
===========
This is the readme file for the TrekBuddy Atlas Creator (TAC) application.


D E S C R I P T I O N
=====================
This application creates atlases for the J2ME application TrekBuddy 


R E Q U I R E M E N T S
=======================
This application requires that a Java Runtime Environment is installed on the computer 


I N S T A L L A T I O N
=======================

Copy or move the unzipped files to a folder where you would like to have TAC 
installed.


A P P L I C A T I O N  S T A R T
================================

The application is started by executing the start.bat in a Windows environment or
 the start.sh under an Linux / Unix environment. At the first application start
 all necessary files and folders are automatically created by the application.

 
 V E R S I O N  I N F O R M A T I O N
 ====================================
 
 0.61:
 + Added a possibility to select to download tiles from either ditu.google.com or maps.google.com
  
 ! Fixed hard coded look and feel, which resulted in an javax.swing.UnsupportedLookAndFeelException at other environments then Windows
 
0.7:
 + Added possibility to abort an ongoing download of Atlas
 + Added label that shows how many tiles that will be downloaded with current lat, long & zoom settings 
 + Added persistent tile store (cache of tiles in a store which is persistent between program sessions) 

 ! Fixed wrong download link to ditu.google.com
 
 0.8:
 + Added xml based settings file via Java Properties
 + Added possibility to change tile size for the downloaded tiles.
 + Added possibility to set map size in atlas. Giving the possibility to create large atlases 
  
 ! Fixed the non working setting tile store enabled. It was always enabled, despite of what was written in the settings file
 ! Fixed (hopefully) the generation of mixed up atlases at some Linux operating system(s).
 ! Fixed bugs related to defect profiles in profiles.xml file by adding some validation of the xml data at application start.
 
 