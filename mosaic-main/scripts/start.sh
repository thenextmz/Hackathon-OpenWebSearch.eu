#!/bin/bash

cli_params="${1:-}"
cli_params="${cli_params%\"}"
cli_params="${cli_params#\"}"
api_port="${2:-8008}"

# get script path to allow the start of the script from any location
scriptdir=$(dirname $0)

cd $scriptdir/../search-service/

java -Dquarkus.http.port=$api_port -jar core/target/service.jar $cli_params
