@echo off

SET codec=%1

:: try to convert all CIFF files which are not converted yet
for /R "..\resources\" %%f in (*.ciff*) do (
    echo Checking if %%f is already converted...
    if exist ..\lucene\%%~nf\ (
        echo Lucene index for %%~nf already exists
    ) else (
        echo Converting CIFF file %%~nxf in %%~nf to Lucene index...
        call %~dp0\import_index.bat %%~nxf %%~nf %codec%
        echo Conversion of %%~nxf in %%~nf completed successfully
    )
)

cd..\search-service\
call mvn clean compile
call mvn -Dmaven.test.skip=true package