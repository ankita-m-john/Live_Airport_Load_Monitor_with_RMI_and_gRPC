#!/bin/sh

while true; do   
    curl -s "https://opensky-network.org/api/states/all" | python3 -m json.tool > "tmpfile" && mv "tmpfile" "flights.txt"
    echo "flights.txt updated at $(date)"
    sleep 10
done
