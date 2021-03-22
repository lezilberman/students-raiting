#!/bin/bash
# create docker images for 3 service instances (partitions)
# cd ${PROJECT_DIR}/students-web-service
docker build -f Dockerfile0 -t gradestore0 .
docker build -f Dockerfile1 -t gradestore1 .
docker build -f Dockerfile2 -t gradestore2 .
# chmod 755 create_service_images.sh