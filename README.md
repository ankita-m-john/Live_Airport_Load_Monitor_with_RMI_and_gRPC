# Live-Airport-Load-Monitor-with-RMI-and-gRPC

Implementing a mini application in both Java RMI (with callback) and gRPC (with server streaming) that:
- Allows clients to subscribe to a specific airport (e.g., "SEA")
- Streams flight updates every 10 seconds (only flights arriving or departing - within 10 miles of that airport)
- Calculates stats and displays to the client - number of arrivals and departures
- Compares gRPC and RMI on execution and programmability