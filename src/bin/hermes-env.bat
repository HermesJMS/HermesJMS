@echo off

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
REM $Id: hermes-env.bat,v 1.1 2006/06/21 07:46:18 colincrist Exp $

REM %~dp0 is expanded pathname of the current script under NT

REM Check for Java 1.5+ JVM but ensure system32 is first in the path as cygwin users may have a different find...

set PATH=c:\windows\system32;%PATH%

for /F "tokens=3" %%v in ('%JAVA_HOME%\bin\java -version 2^>^&1^| find "version"') do if "%%~v" lss "1.5.0" echo Java 1.5 or higher required to run Hermes JMS (Java version %%~vwas found at %JAVA_HOME%) && pause && goto :EOF

set HERMES_HOME=C:\Program Files\HermesJMS

if "%HERMES_CONFIG%"=="" goto tryDotHermes
goto setOtherVars

:tryDotHermes
if not exist "%HOME%\.hermes\hermes-config.xml" goto tryHermes
set HERMES_CONFIG=%HOME%\.hermes
goto setOtherVars

:tryHermes
if not exist "%HOME%\hermes\hermes-config.xml" goto tryHermesCfg
set HERMES_CONFIG=%HOME%\hermes
goto setOtherVars

:tryHermesCfg
if not exist "%HOME%\hermes\cfg\hermes-config.xml" goto setHermesConfigDefault
set HERMES_CONFIG=%HOME%\hermes
goto setOtherVars

:setHermesConfigDefault
set HERMES_CONFIG=%HOME%\.hermes

rem Bootstrap a configuration if it does not exist.

if exist "%HERMES_CONFIG%" goto checkHermesConfigXml

mkdir "%HERMES_CONFIG%"

:checkHermesConfigXml

if exist "%HERMES_CONFIG%\hermes-config.xml" goto setOtherVars

copy "%HERMES_HOME%\cfg\hermes-config.xml" "%HERMES_CONFIG%\hermes-config.xml"

:setOtherVars
set HERMES_LIBS=%HERMES_HOME%\lib
set HERMES_BIN=%HERMES_HOME%\bin

set CLASSPATH=%HERMES_LIBS%\xml-apis.jar;%HERMES_LIBS%\jms.jar;%HERMES_LIBS%\jax-qname.jar;%HERMES_LIBS%\relaxngDatatype.jar;%HERMES_LIBS%\jaas.jar;%HERMES_LIBS%\activation.jar;%HERMES_LIBS%\jaxb-libs.jar;%HERMES_LIBS%\jaxb-api.jar;%HERMES_LIBS%\jaxb-xjc.jar;%HERMES_LIBS%\jaxb-impl.jar;%HERMES_LIBS%\commons-digester.jar;%HERMES_LIBS%\commons-codec-1.1.jar;%HERMES_LIBS%\log4j-1.2.8.jar;%HERMES_LIBS%\hermes.jar;%HERMES_LIBS%\j2ee.jar;%HERMES_LIBS%\commons-logging.jar;%HERMES_LIBS%\jlfgr-1_0.jar;%HERMES_LIBS%\commons-lang-2.0.jar;%HERMES_LIBS%\commons-collections.jar;%HERMES_LIBS%\commons-beanutils.jar;%HERMES_LIBS%\xercesImpl.jar;%HERMES_LIBS%\namespace.jar;%HERMES_LIBS%\xsdlib.jar;%HERMES_LIBS%\datatips.jar;%HERMES_LIBS%\derby.jar;%HERMES_LIBS%\commons-dbutils-1.0.jar;%HERMES_LIBS%\ant.jar;%HERMES_LIBS%\db2cc.jar;%HERMES_LIBS%\hermes-selector.jar;%HERMES_LIBS%\jms-jmx.jar;%HERMES_LIBS%\selector-1.1.jar;%HERMES_LIBS%\cglib-2.1_3.jar;%HERMES_LIBS%\asm.jar;%HERMES_LIBS%\asm-attrs.jar;%HERMES_LIBS%\asm-util.jar;%HERMES_LIBS%\quickfixj.jar

cd %HERMES_CONFIG%

c:\local\jython-2.1\jython.bat