#!/usr/bin/env bash

type="password"
size=256
number=1

while getopts t:s:n: flag
do
    case "${flag}" in
        t) type=${OPTARG};;
        s) size=${OPTARG};;
        n) number=${OPTARG};;
    esac
done

echo "Generating Key using the following parameters:"
echo "Type: $type (set using -t)";
echo "Size: $size (set using -s)";
echo "Number: $number (set using -n)";
echo "---"

java -cp buildtools-1.0.0.jar KeyGenerator -t $type -s $size -n $number
