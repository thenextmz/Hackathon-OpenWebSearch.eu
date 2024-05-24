#!/bin/bash

codec="${1:-[CODEC]}"

# try to convert all CIFF files which are not converted yet
for f in ../resources/*/*.ciff*
do
    dir="${f%/*}"
    dir="${dir##*/}"
    f=$(echo $f | sed s:.*/::)
    echo "Checking if $f in $dir is already import..."
    if [ ! -d "../lucene/$dir" ]; then
        echo "Converting CIFF file $f in $dir to Lucene index..."
        d=$(echo $f | sed "s/\..*//")
        echo "Creating directory ../lucene/$dir"
        ./import_index.sh "$f" "$dir"
        echo "Import of $f completed successfully"
    else
        echo "$f is already converted"
    fi
done

cd ../search-service/
./mvnw clean compile
./mvnw -Dmaven.test.skip=true package