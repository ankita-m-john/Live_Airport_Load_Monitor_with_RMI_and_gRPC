package Shared.code;

/**
 * A simple POJO for one flight’s data of interest:
 * - callSign (e.g. "UAL123")
 * - latitude, longitude
 * - (optional) velocity, verticalRate
 *
 * We’ll parse these from flights.txt JSON.
 */
public class FlightRecord {
    public final String callSign;
    public final double latitude;
    public final double longitude;
    public final double velocity; // in whatever units are in flights.txt (meters/sec)
    public final double verticalRate; // in meters/sec

    public FlightRecord(String callSign, double latitude, double longitude,
            double velocity, double verticalRate) {
        this.callSign = callSign;
        this.latitude = latitude;
        this.longitude = longitude;
        this.velocity = velocity;
        this.verticalRate = verticalRate;
    }
}
