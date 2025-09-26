set OPTION=-verbose
::set OPTION=%OPTION% -listing
::set OPTION=%OPTION% -inputTrace
::set OPTION=-compilerMode simulaClassLoader
echo The Options used: %OPTION%
pause

::java -jar C:\SPORT\BEC.jar -verbose -listing C:/Simuletta/SCode/simulaRTS/RT.scd

java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/RT.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/SYSR.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/KNWN.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/UTIL.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/STRG.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/CENT.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/CINT.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/ARR.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/FORM.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/LIBR.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/FIL.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/SMST.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/SML.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/EDIT.scd
java -jar C:\SPORT\BEC.jar %OPTION% C:/Simuletta/SCode/simulaRTS/MNTR.scd

pause
