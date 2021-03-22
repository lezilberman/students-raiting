#!/bin/bash
#create topic "students" 
exec $(dirname $0)/kafka-topics.sh --create --topic students --partitions 3 --replication-factor 1 --zookeeper localhost:2181 
#chmod 755 create_students_topic.sh
#export PATH=$PATH:.
