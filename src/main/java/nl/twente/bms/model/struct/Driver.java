package nl.twente.bms.model.struct;

/**
 * The class to store the Drive object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Driver {

    private int id;
    private String startStationName;
    private String endStationName;

    private int earliestDepartureTime;
    private int latestArrivalTime;

    private int capacity;

    public Driver(int id, String startStationName, String endStationName,
                  int earliestDepartureTime, int latestArrivalTime, int capacity) {
        this.id = id;
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestArrivalTime = latestArrivalTime;
        this.capacity = capacity;
    }

    public String toString(){
        return String.format("Driver[%d]: %s->%s, (%d, %d), %d", id, startStationName, endStationName,
                earliestDepartureTime, latestArrivalTime, capacity);
    }
}
