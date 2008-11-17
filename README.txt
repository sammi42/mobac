*******************************************************************************
**                              R E A D M E                                  **
*******************************************************************************

This is the readme file for the TrekBuddy Atlas Creator (TAC) application.
TAC is an open source project hosted on SourceForge.net:

http://sourceforge.net/projects/trekbuddyatlasc/

*******************************************************************************
**                             L I C E N S E                                 **
*******************************************************************************

TrekBuddy Atlas Creator (TAC) is under GPL. For details on the GPL
see the license file gpl.txt.


*******************************************************************************
**                         D E S C R I P T I O N                             **
*******************************************************************************

This application creates atlases for the J2ME application TrekBuddy 


*******************************************************************************
**                        R E Q U I R E M E N T S                            **
*******************************************************************************

This application requires that a Java Runtime Environment version 5 or higher 
is installed on the computer. 


*******************************************************************************
**                        I N S T A L L A T I O N                            **
*******************************************************************************

Copy or move the unzipped files to a folder where you would like to have TAC 
installed.


*******************************************************************************
**                    A P P L I C A T I O N  S T A R T                       **
*******************************************************************************

The application is started by executing the "start.cmd" in a Windows 
environment or the "start.sh" under an Linux / Unix environment. At the first 
application start all necessary files and folders are automatically created by 
the application.


*******************************************************************************
**                       K N O W N   P R O B L E M S                         **
*******************************************************************************

*********************************************
 java.lang.OutOfMemoryError: Java heap space
*********************************************

If you are using a custom tile size height and/or width the maximum size of a 
map is limited by the maximum memory available to TAC. By default each Java 
program can access in maximum 64 MB - independently of your system free memory 
status. For extending this limit please start TAC via the provided startup 
scripts 'start.cmd' (Windows) or 'startup.sh' (Linux/Mac OS X). Those scripts 
extend the maximum memory usable by TAC to 512 MB.
 

*******************************************************************************
**                     F U R T H E R   P R O B L E M S                       **
*******************************************************************************

If you encounter problems please download the following file and save it in the
directory where the jar file of TrekBuddy Atlas Creator is installed to. 

https://trekbuddyatlasc.svn.sourceforge.net/svnroot/trekbuddyatlasc/trunk/log4j.xml

The next start TrekBuddy Atlas Creator will create a log file in the current 
directory (on Windows this is usually the directory where the JAR file is 
located on Linux usually the profile directory). Please note that the log file 
is erased on each program start.

If you think you have found a bug please file it in the bug tracker at 
SourceForge:
 
http://sourceforge.net/tracker/?group_id=238075&atid=1105494

