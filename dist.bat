@echo off
REM ============================================================================
REM  dist.bat - Build the Guiced IntelliJ plugin distribution
REM
REM  Produces the installable plugin zip in: build\distributions\
REM  (e.g. GEEIntelliJPlugin-2.2.0.zip)
REM
REM  NOTE: Gradle 8.14.4 (this project's wrapper) cannot run on JDK 25.
REM        This script therefore builds with JDK 21. Override the JDK used by
REM        setting JAVA21_HOME before running, e.g.:
REM            set JAVA21_HOME=D:\jdk\21
REM            dist.bat
REM
REM  Usage:
REM    dist.bat            Build the plugin distribution (clean + buildPlugin)
REM    dist.bat verify     Build and run plugin verification
REM    dist.bat run        Build and launch a sandbox IDE with the plugin
REM ============================================================================

setlocal

cd /d "%~dp0"

REM --- Select a JDK 21 to run Gradle with ---------------------------------------
if not defined JAVA21_HOME (
    if exist "C:\software\jdk\21\bin\java.exe" (
        set "JAVA21_HOME=C:\software\jdk\21"
    )
)

if defined JAVA21_HOME (
    if exist "%JAVA21_HOME%\bin\java.exe" (
        set "JAVA_HOME=%JAVA21_HOME%"
        echo Using JAVA_HOME=%JAVA21_HOME%
    ) else (
        echo WARNING: JAVA21_HOME "%JAVA21_HOME%" is not valid; falling back to current JAVA_HOME.
    )
) else (
    echo WARNING: No JDK 21 found. Using current JAVA_HOME=%JAVA_HOME%
    echo          If the build fails with "IllegalArgumentException: 25", install JDK 21
    echo          and set JAVA21_HOME to its path.
)

set "GRADLE=gradlew.bat"
set "TASK=%~1"

echo.
echo ============================================================
echo  Building Guiced IntelliJ Plugin distribution
echo ============================================================
echo.

if /I "%TASK%"=="verify" (
    call "%GRADLE%" clean buildPlugin verifyPlugin --console=plain
) else if /I "%TASK%"=="run" (
    call "%GRADLE%" clean buildPlugin runIde --console=plain
) else (
    call "%GRADLE%" clean buildPlugin --console=plain
)

set "EXITCODE=%ERRORLEVEL%"

echo.
if "%EXITCODE%"=="0" (
    echo ============================================================
    echo  BUILD SUCCEEDED
    echo  Distribution artifacts:
    echo ============================================================
    if exist "build\distributions" (
        dir /b "build\distributions\*.zip"
    )
) else (
    echo ============================================================
    echo  BUILD FAILED ^(exit code %EXITCODE%^)
    echo ============================================================
)

endlocal & exit /b %EXITCODE%


