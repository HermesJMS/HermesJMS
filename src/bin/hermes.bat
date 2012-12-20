echo off

REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.
REM
REM $Id$

REM %~dp0 is expanded pathname of the current script under NT


set PATH=
set HERMES_HOME=$INSTALL_PATH


if "%HERMES_CONFIG%"=="" goto tryDotHermes
goto setOtherVars

:tryDotHermes
if not exist "%HOMEDRIVE%\%HOMEPATH%\.hermes\hermes-config.xml" goto tryHermes
set HERMES_CONFIG=%HOMEDRIVE%\%HOMEPATH%\.hermes
goto setOtherVars

:tryHermes
if not exist "%HOMEDRIVE%\%HOMEPATH%\hermes\hermes-config.xml" goto tryHermesCfg
set HERMES_CONFIG=%HOMEDRIVE%\%HOMEPATH%\hermes
goto setOtherVars

:tryHermesCfg
if not exist "%HOMEDRIVE%\%HOMEPATH%\hermes\cfg\hermes-config.xml" goto tryCDrive
set HERMES_CONFIG=%HOMEDRIVE%\%HOMEPATH%\hermes
goto setOtherVars

:tryCDrive
if no exist "C:\.hermes\hermes-config.xml" goto setHermesConfigDefault
set HERMES_CONFIG=C:\.hermes
goto setOtherVars

:setHermesConfigDefault
set HERMES_CONFIG=%HOMEDRIVE%\%HOMEPATH%\.hermes

rem Bootstrap a configuration if it does not exist.

if exist "%HERMES_CONFIG%" goto checkHermesConfigXml

mkdir "%HERMES_CONFIG%"

:checkHermesConfigXml

if exist "%HERMES_CONFIG%\hermes-config.xml" goto setOtherVars

copy "%HERMES_HOME%\cfg\hermes-config.xml" "%HERMES_CONFIG%\hermes-config.xml"

:setOtherVars
set HERMES_LIBS=%HERMES_HOME%\lib
set HERMES_BIN=%HERMES_HOME%\bin
set CLASSPATH=%HERMES_LIBS%\*;

cd %HERMES_CONFIG%

start "HermesJMS" "%JAVA_HOME%\bin\javaw" -XX:NewSize=256m -Xmx1024m -Dhermes.home="%HERMES_HOME%" %HERMES_OPTS% -Dlog4j.configuration="file:%HERMES_HOME%\bin\log4j.props" -Dsun.java2d.noddraw=true -Dhermes="%HERMES_CONFIG%\hermes-config.xml" -Dhermes.libs="%HERMES_LIBS%"\ext hermes.browser.HermesBrowser

