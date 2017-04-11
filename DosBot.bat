@echo off
rem   You should find Ansicon by googling it if you want it.
rem   Place the x86 and x64 folder from the Ansicon zip in the applications
rem   directory and this script should find and use it to start the bot.
cls
title Twitch Bot DosBot

rem detect x64 or x86
reg Query "HKLM\Hardware\Description\System\CentralProcessor\0" | find /i "x86" > NUL && set OS=x86 || set OS=x64
echo You machine operate %OS% Architecture
set ansicon=%~dp0%OS%\ansicon.exe

rem check if ansicon is installed and use it if so
rem otherwise just start java
if EXIST %ansicon% (
 %~dp0%OS%\ansicon.exe -p java.exe -jar %~dp0dosbot.jar
) ELSE (
 echo Ansicon could not be found. Things may look strange!
 java.exe -jar %~dp0dosbot.jar
)
pause