#!/bin/bash

COUNTER=0

while [  $COUNTER -lt 200 ]; do
  java -jar trace-collect-1.0-SNAPSHOT-all.jar 5
  sudo systemctl restart besu

  sleep 10
  COUNTER=$((COUNTER + 1))
done
