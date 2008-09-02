REM This file will start the TrekBuddy Atlas Creator with custom memory settings for
REM the JVM. With the below settings the heap size (Available memory for the application)
REM will range from 64 megabyte up to 512 megabyte.

START javaw -jar TrekBuddy_Atlas_Creator_v0.8.jar -Xms64M -Xmx512M