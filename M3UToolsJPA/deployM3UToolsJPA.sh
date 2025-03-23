#!/bin/bash
cd /home/jonathan/2TBSSD/git/M3UTools/M3UToolsJPA
mvn clean package
cd target
echo "Stopping M3UToolsJPA"
sudo systemctl stop M3UToolsJPA
rm /home/jonathan/M3UToolsJPA/M3UToolsJPA.log.*.gz
cp M3uJpa-0.0.1-SNAPSHOT.jar /home/jonathan/M3UToolsJPA/M3UToolsJPA.jar
echo "Starting M3UToolsJPA"
sudo systemctl start M3UToolsJPA


# cd /home/jonathan/videoDownloader
# sudo java -jar -Xms1024M -Xmx4096M SpringVideoDownloader-0.0.1-SNAPSHOT.jar
