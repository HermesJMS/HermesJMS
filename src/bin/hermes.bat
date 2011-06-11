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
REM $Id$

REM %~dp0 is expanded pathname of the current script under NT


set PATH=
set HERMES_HOME=$INSTALL_PATH


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
set JAXB=%HERMES_LIBS%\jaxb-api.jar
set JIDE=%HERMES_LIBS%\jide-action.jar;%HERMES_LIBS%\jide-common.jar;%HERMES_LIBS%\jide-components.jar;%HERMES_LIBS%\jide-dialogs.jar;%HERMES_LIBS%\jide-dock.jar;%HERMES_LIBS%\jide-grids.jar

set CLASSPATH=%JAXB%;%HERMES_LIBS%/jta-spec1_0_1.jar;%HERMES_LIBS%\xml-apis.jar;%HERMES_LIBS%\jms.jar;%HERMES_LIBS%\relaxngDatatype.jar;%HERMES_LIBS%\jaas.jar;%HERMES_LIBS%\activation.jar;%HERMES_LIBS%\commons-digester.jar;%HERMES_LIBS%\commons-codec-1.1.jar;%HERMES_LIBS%\log4j-1.2.15.jar;%HERMES_LIBS%\j2ee.jar;%HERMES_LIBS%\commons-logging.jar;%HERMES_LIBS%\jlfgr-1_0.jar;%HERMES_LIBS%\commons-lang-2.1.jar;%HERMES_LIBS%\xercesImpl.jar;%HERMES_LIBS%\namespace.jar;%HERMES_LIBS%\xsdlib.jar;%HERMES_LIBS%\datatips.jar;%HERMES_LIBS%\derby.jar;%HERMES_LIBS%\commons-dbutils-1.0.jar;%HERMES_LIBS%\ant.jar;%HERMES_LIBS%\db2cc.jar;%HERMES_LIBS%\hermes-selector.jar;%HERMES_LIBS%\jms-jmx.jar;%HERMES_LIBS%\selector-1.1.jar;%HERMES_LIBS%\asm-all-4.0_RC1.jar;%HERMES_LIBS%\quickfixj.jar;%HERMES_LIBS%\jython.jar;%HERMES_LIBS%\ArtTk.jar;%HERMES_LIBS%\JyConsole.jar;%HERMES_LIBS%\mina-core-1.1.0-SNAPSHOT.jar;%HERMES_LIBS%\slf4j-jdk14-1.0.1.jar;%HERMES_LIBS%\commons-collections.jar;%HERMES_LIBS%\jsyntaxpane-0.9.1.jar
set CLASSPATH=%CLASSPATH%;%HERMES_LIBS%\hermes.jar;%HERMES_LIBS%\cglib-nodep-2.2.2.jar;%HERMES_LIBS%\commons-beanutils.jar;%JIDE%;%HERMES_LIBS%\forms-1.3.0.jar
set CLASSPATH=%HERMES_LIBS%;%CLASSPATH%

cd %HERMES_CONFIG%

start "HermesJMS" "%JAVA_HOME%\bin\javaw" -XX:NewSize=256m -Xmx1024m -Dhermes.home="%HERMES_HOME%" %HERMES_OPTS% -Dlog4j.configuration="file:%HERMES_HOME%\bin\log4j.props" -Dsun.java2d.noddraw=true -Dhermes="%HERMES_CONFIG%\hermes-config.xml" -Dhermes.libs="%HERMES_LIBS%"\ext hermes.browser.HermesBrowser

