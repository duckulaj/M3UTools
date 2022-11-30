#!/bin/bash
cd /home/jonathan/git/M3UTools/M3UToolsJPA
mvn clean package
cd target
# echo "Stopping videoDownloader"
# sudo systemctl stop videoDownloader
rm /home/jonathan/M3UToolsJPA/M3UToolsJPA.log.*.gz
cp M3uJpa-0.0.1-SNAPSHOT.jar /home/jonathan/M3UToolsJPA/M3UToolsJPA.jar
# echo "Starting videoDownloader"
# sudo systemctl start videoDownloader


# cd /home/jonathan/videoDownloader
# sudo java -jar -Xms1024M -Xmx4096M SpringVideoDownloader-0.0.1-SNAPSHOT.jar
