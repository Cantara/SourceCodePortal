#!/bin/bash

set -e

docker build -t cantara/sourcecodeportal .
docker run -it --rm -p 9090:9090 cantara/sourcecodeportal
