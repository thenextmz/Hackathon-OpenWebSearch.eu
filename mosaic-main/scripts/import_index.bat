@echo off

SET ciff_name=%~1
SET lucene_name=%~2
SET codec=%~3

cd..\search-service\
call java -jar lucene-ciff.jar ..\resources\%lucene_name%\%ciff_name% ..\lucene\%lucene_name%\ %codec%