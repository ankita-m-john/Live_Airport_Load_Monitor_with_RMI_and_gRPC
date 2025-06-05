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
import java.util.concurrent.atomic.AtomicReference;

public class LoadServer {
    static class LoadServiceImpl extends LoadServiceGrpc.LoadServiceImplBase {

        private final FlightTracker tracker;

        public LoadServiceImpl(FlightTracker tracker) {
            this.tracker = tracker;
        }

        @Override
        public StreamObserver<LoadRequest> monitorLoad(StreamObserver<AirportLoad> responseObserver) {
            // Each client has its own subscription set
            AtomicReference<Set<String>> subscribedAirports = new AtomicReference<>(Collections.emptySet());

            // Create a scheduled task specific to this client
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            final Runnable task = () -> {
                try {
                    Map<String, FlightTracker.AirportStats> stats = tracker.scan();
                    Set<String> codes = subscribedAirports.get();

                    for (String code : codes) {
                        if (stats.containsKey(code)) {
                            FlightTracker.AirportStats s = stats.get(code);
                            AirportLoad msg = AirportLoad.newBuilder()
                                    .setAirportCode(code)
                                    .setArrivals(s.arrivals.size())
                                    .setDepartures(s.departures.size())
                                    .addAllArrivingFlights(s.arrivals)
                                    .addAllDepartingFlights(s.departures)
                                    .build();
                            responseObserver.onNext(msg);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Error in per-client task: " + e.getMessage());
                }
            };

            final ScheduledFuture<?> taskHandle = scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);

            return new StreamObserver<LoadRequest>() {
                @Override
                public void onNext(LoadRequest req) {
                    Set<String> newSubs = new HashSet<>(req.getAirportCodesList());
                    subscribedAirports.set(newSubs);
                    System.out.println("🔄 Updated subscription: " + newSubs);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("❌ Client stream error: " + t.getMessage());
                    taskHandle.cancel(true);
                    scheduler.shutdown();
                }

                @Override
                public void onCompleted() {
                    System.out.println("✅ Client stream completed.");
                    taskHandle.cancel(true);
                    scheduler.shutdown();
                    responseObserver.onCompleted();
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: LoadServer <airports.txt> <flights.txt>");
            return;
        }

        FlightTracker tracker = new FlightTracker(args[0], args[1]);

        Server server = ServerBuilder.forPort(50051)
                .addService(new LoadServiceImpl(tracker))
                .build()
                .start();

        System.out.println("🚀 Bidirectional gRPC LoadServer running on port 50051");
        server.awaitTermination();
    }
}
