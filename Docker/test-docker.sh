#!/bin/bash

set -e

sudo docker build -t cantara/sourcecodeportal .
sudo docker run -it --rm -p 80:9090 cantara/sourcecodeportal
