# Live-Airport-Load-Monitor-with-RMI-and-gRPC

Implementing a mini application in both Java RMI (with callback) and gRPC (with server streaming) that:
- Allows clients to subscribe to a specific airport (e.g., "SEA")
- Streams flight updates every 10 seconds (only flights arriving or departing - within 10 miles of that airport)
- Calculates stats and displays to the client - number of arrivals and departures
- Compares gRPC and RMI on execution and programmability

Step-by-step commands to run this mini-application:
- Run this command to compile and create the dependencies using the pom file of the folder. You need to run this in Shared, gRPC and RMI folders one by one.
```
    cd Shared
    mvn clean install
    mvn clean compile
```
- Real-time flights update script must be run in the background before running the servver and client programs in either versions
```
    ./resources/get_flights_data.sh
    #To end the loop, exit using break - <ctrl>+c
```

Testing shared code:
- Run SharedTest file to check if the bounding and stats calculation logic is working
    `mvn exec:java -Dexec.mainClass=Shared.code.SharedTest`

In gRPC version:
- Install protoc
```
    mkdir -p ~/tools/protoc
    cd ~/tools/protoc
    wget https://github.com/protocolbuffers/protobuf/releases/download/v21.12/protoc-21.12-linux-x86_64.zip
    unzip protoc-21.12-linux-x86_64.zip
```
- Run the command to move to the gRPC directory and compile the files (Check paths in all pom files)
```
    cd ~/Live_Airport_Load_Monitor_with_RMI_and_gRPC/gRPC
    mvn clean compile
```
- Run this command to run the server program in the gRPC version
    `mvn compile exec:java   -Dexec.mainClass="loadmonitor_grpc.LoadServer"   -Dexec.args="../Shared/resources/airports.txt ../Shared/resources/flights.txt"`
-  Run this command to run the client program in the gRPC version
    `mvn exec:java -Dexec.mainClass="loadmonitor_grpc.LoadClient"   -Dexec.args="<aisport1> <airport2>..."`
