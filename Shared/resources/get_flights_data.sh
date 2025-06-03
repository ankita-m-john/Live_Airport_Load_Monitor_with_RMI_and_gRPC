#!/bin/sh
 while true; do   
 curl -s "https://opensky-network.org/api/states/all" | python -m json.tool > "./Shared/resources/flights.txt";
 echo "Updating script"; 
 sleep 8; 
 done


