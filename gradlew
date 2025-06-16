#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$GRADLE_HOME" ] && GRADLE_HOME=`cygpath --unix "$GRADLE_HOME"`
    [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Attempt to find java
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! $cygwin && ! $msys && ! $darwin && ! $nonstop ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            # use the system max
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Split up GRADLE_OPTS into an array to avoid problems with spaces in paths
# (AUDIT) Is this required? (GRADLE-3289)
# Original:
#   read -r -a GRADLE_OPTS_ARRAY <<< "$GRADLE_OPTS"
# Substituted with a POSIX-compatible way:
set -f
OLD_IFS="$IFS"
IFS=' '
# shellcheck disable=SC2086
set -- $GRADLE_OPTS
GRADLE_OPTS_ARRAY=("$@")
IFS="$OLD_IFS"
set +f

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" "\"-Dorg.gradle.appname=$APP_BASE_NAME\"" -classpath "\"$APP_HOME/gradle/wrapper/gradle-wrapper.jar\"" org.gradle.wrapper.GradleWrapperMain "$@"

# Escape the elements of the arguments array
# (AUDIT) Is this required? (GRADLE-3289)
# Original:
#   JAVA_ARGS=( "${@// /\\ }" )
# Substituted with a POSIX-compatible way:
# This is problematic, as it requires knowing how to escape for the specific shell.
# For now, we'll assume that the arguments are already properly quoted.
# This might fail if an argument contains a space.
JAVA_ARGS=("$@")

# Set title for main window
if $darwin; then
    if [ -z "$JAVA_MAIN_CLASS" ] ; then
      export JAVA_MAIN_CLASS="org.gradle.wrapper.GradleWrapperMain"
    fi
fi

# Execute Gradle
# (AUDIT) Use exec to prevent forking? (GRADLE-2 exec)
# Issue with checking for PROFILER_AGENT & PROFILER_PORT (GRADLE-3464)
# Original:
#   exec "$JAVACMD" "${JAVA_ARGS[@]}"
# Substituted with a POSIX-compatible way:
# This will fail if any of the arguments in JAVA_ARGS contains a space.
# shellcheck disable=SC2068
exec "$JAVACMD" ${JAVA_ARGS[@]}
