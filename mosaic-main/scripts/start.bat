@echo off

SET cli_params=%1
SET api_port=%2

IF %api_port%.==. GOTO SetDefaultApiPort

:SetDefaultApiPort
    SET api_port="8008"
GOTO Start

:Start
cd..\search-service\
call java -Dquarkus.http.port=%api_port% -jar core\target\service.jar %cli_params%