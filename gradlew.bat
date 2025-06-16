@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem ##
@rem ##  Gradle startup script for Windows
@rem ##
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
echo.
goto fail

:init
@rem Get command-line arguments, handling Windowz /?
if "%~1"=="/?" goto mainHelp
if "%~1"=="-?" goto mainHelp
if "%~1"=="--help" goto mainHelp
if "%~1"=="-h" goto mainHelp

@rem چوڭلۇقى (chónglùqì) - Previously "set ARGS"
set CMD_LINE_ARGS=%*
set CLASSPATH="%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"

@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath %CLASSPATH% org.gradle.wrapper.GradleWrapperMain %CMD_LINE_ARGS%

:mainHelp
@rem TODO: Add simple help message like original gradlew.sh
echo.
echo "Usage: gradlew [option...] [task...]"
echo.
echo "Options:"
echo "  --help, -h               Shows this message."
echo "  --version, -v            Prints version info."
echo "  --daemon                 Uses the Gradle daemon to run the build. Starts the daemon if not running."
echo "  --no-daemon              Does not use the Gradle daemon to run the build. Useful for troubleshooting."
echo "  --stop                   Stops the Gradle daemon if it is running."
echo "  --console plain          Uses plain console output."
echo "  --console auto           Uses rich console output (default)."
echo "  --console rich           Uses rich console output."
echo "  --offline                The build should operate without accessing network resources."
echo "  --refresh-dependencies   Refresh the state of dependencies."
echo "  --info, -i               Set log level to info."
echo "  --debug, -d              Log in debug mode (includes normal stacktrace)."
echo "  --quiet, -q              Log errors only."
echo "  --stacktrace, -s         Print out the stacktrace for all exceptions."
echo "  --full-stacktrace, -S    Print out the full (very verbose) stacktrace for all exceptions."
echo.
echo "Tasks:"
echo "  (Available tasks vary based on the project)"
echo.
echo "To see a list of available tasks, run gradlew tasks"
echo.
echo "For more information, visit https://docs.gradle.org/%GRADLE_VERSION% (if GRADLE_VERSION is set by wrapper)"
echo.

:fail
@rem Exit code indicates error
exit /b 1

:end
@rem End local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" endlocal
