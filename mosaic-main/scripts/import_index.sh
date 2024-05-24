#!/bin/bash

ciff_name="$1"
lucene_name="$2"
codec="${3:-[CODEC]}"

cd ../lucene-ciff/
java -jar lucene-ciff.jar ../resources/$2/$1 ../lucene/$2/ $3