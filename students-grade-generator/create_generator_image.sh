#!/bin/bash
# create docker image for gradegenerator
# cd ${PROJECT_DIR}/students-grade-generator
docker build -f Dockerfile -t gradegenerator .
# chmod 755 create_generator_image.sh