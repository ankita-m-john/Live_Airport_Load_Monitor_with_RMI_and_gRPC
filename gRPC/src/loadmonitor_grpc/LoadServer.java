package loadmonitor_grpc;

import loadmonitor_grpc.AirportLoad;
import loadmonitor_grpc.LoadRequest;
import loadmonitor_grpc.LoadServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import Shared.code.FlightTracker;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class LoadServer {

    static class LoadServiceImpl extends LoadServiceGrpc.LoadServiceImplBase {
        private final Map<String, List<StreamObserver<AirportLoad>>> observers = new ConcurrentHashMap<>();
        private final FlightTracker tracker;

        public LoadServiceImpl(FlightTracker tracker) {
            this.tracker = tracker;
            startBroadcast();
        }

        @Override
        public void monitorLoad(LoadRequest req, StreamObserver<AirportLoad> responseObserver) {
            for (String airport : req.getAirportCodesList()) {
                observers.computeIfAbsent(airport, k -> new CopyOnWriteArrayList<>()).add(responseObserver);
            }
        }

        private void startBroadcast() {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                Map<String, FlightTracker.AirportStats> stats = tracker.scan();
                for (String airport : stats.keySet()) {
                    AirportLoad msg = AirportLoad.newBuilder()
                            .setAirportCode(airport)
                            .setArrivals(stats.get(airport).arrivals.size())
                            .setDepartures(stats.get(airport).departures.size())
                            .addAllArrivingFlights(stats.get(airport).arrivals)
                            .addAllDepartingFlights(stats.get(airport).departures)
                            .build();
                    for (StreamObserver<AirportLoad> obs : observers.getOrDefault(airport, List.of())) {
                        obs.onNext(msg);
                    }
                }
            }, 0, 10, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Usage: LoadServer <airports.txt> <flights.txt>");
            return;
        }

        FlightTracker tracker = new FlightTracker(args[0], args[1]);

        Server server = ServerBuilder.forPort(50051)
                .addService(new LoadServiceImpl(tracker))
                .build()
                .start();

        System.out.println("gRPC LoadServer running on port 50051");
        server.awaitTermination();
    }
}
