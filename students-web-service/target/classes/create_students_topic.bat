:: rem @ECHO OFF
:: rem ECHO create_students_topic.bat
:: rem PAUSE

::@ECHO OFF
::ECHO cd C:\kafka_2.11-2.0.0\bin\windows
::dir
::PAUSE

@ECHO OFF
kafka-topics.bat --create --topic students --partitions 3 --replication-factor 1 --zookeeper localhost:2181 
PAUSE
