#!/bin/sh

#   Copyright 2004 Colin Crist
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

#
# Try and sort out hermes set up - liberally cribbed from Apache Ant.
#
# $Id$
#

#
# Load system-wide hermes configuration

if [ -f "/etc/hermes.conf" ] ; then
  . /etc/hermes.conf
fi

#
# load user hermes configuration

if [ -f "$HOME/.hermesrc" ] ; then
  . "$HOME/.hermesrc"
fi

#
# OS specific support.  $var _must_ be set to either true or false.

cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
	     HERMES_OPTS="-Xdock:name=HermesJMS -Dcom.apple.mrj.application.apple.menu.about.name=HermesJMS -Dcom.apple.mrj.application.growbox.intrudes=false -Dapple.laf.useScreenMenuBar=true $HERMES_OPTS"
           fi
           ;;
  Linux*) if [ -z "$HERMES_OPTS" ] ; then
             HERMES_OPTS="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
          else
             HERMES_OPTS="-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel $HERMES_OPTS"
          fi ;;
esac

if [ -z "$HERMES_HOME" -o ! -d "$HERMES_HOME" ] ; then
  # try to find HERMES
  if [ -d /opt/hermes ] ; then
    HERMES_HOME=/opt/hermes
  fi

  if [ -d "${HOME}/opt/hermes" ] ; then
    HERMES_HOME="${HOME}/opt/hermes"
  fi

  ## resolve links - $0 may be a link to hermes's home
  PRG="$0"
  progname=`basename "$0"`

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  HERMES_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  HERMES_HOME=`cd "$HERMES_HOME" && pwd`
fi

#
# For Cygwin, ensure paths are in UNIX format before anything is touched

if $cygwin ; then
  [ -n "$HERMES_HOME" ] &&
    HERMES_HOME=`cygpath --unix "$HERMES_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

#
# Set HERMES_LIB location

HERMES_LIB="${HERMES_HOME}/lib"

#
# Setup the Java VM

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

#
# Slurp up everything in lib into the LOCALCLASSPATH

for F in `ls $HERMES_LIB`
do
  if [ -z "$LOCALCLASSPATH" ] ; then
    LOCALCLASSPATH=$HERMES_LIB/$F 
  else
    LOCALCLASSPATH=$LOCALCLASSPATH:$HERMES_LIB/$F
  fi
done


#
# See if we can find a config file.

#
# $HOME/hermes/hermes-config.xml ?

if [ -z "$HERMES_CFG" ] ; then 
    if [ -d "$HOME/.hermes" ] ; then
	if [ -f "$HOME/.hermes/hermes-config.xml" ] ; then
	    HERMES_CFG=$HOME/.hermes/hermes-config.xml
	fi
    fi
fi

if [ -z "$HERMES_CFG" ] ; then 
    if [ -d "$HOME/hermes" ] ; then
	if [ -f "$HOME/hermes/hermes-config.xml" ] ; then
	    HERMES_CFG=$HOME/hermes/hermes-config.xml
	fi
    fi
fi

if [ -z "$HERMES_CFG" ] ; then 
    HERMES_CFG=$HERMES_HOME/cfg/hermes-config.xml
fi

#
# For Cygwin, switch paths to Windows format before running java

if $cygwin; then
  HERMES_HOME=`cygpath --windows "$HERMES_HOME"`
  HERMES_CFG=`cygpath --windows "$HERMES_CFG"`
  HERMES_LIB=`cygpath --windows "$HERMES_LIB"`
  JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  LOCALCLASSPATH=`cygpath --path --windows "$LOCALCLASSPATH"`
  CYGHOME=`cygpath --windows "$HOME"`
fi

#
# Run main(). 

"$JAVACMD" -XX:NewSize=256m -Xmx1024m $HERMES_OPTS -Dlog4j.configuration=file:$HERMES_HOME/bin/log4j.props -Dhermes.home=$HERMES_HOME -Dhermes=$HERMES_CFG -Dhermes.libs=$HERMES_LIB -classpath $LOCALCLASSPATH  hermes.browser.HermesBrowser
