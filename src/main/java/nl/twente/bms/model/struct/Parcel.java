package nl.twente.bms.model.struct;

/**
 * The class to store the Parcel object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Parcel {

    private int id;
    private String startStationName;
    private String endStationName;

    private int earliestDepartureTime;
    private int latestArrivalTime;

    private double shippingCompanyCost;
    private int volume;

    public Parcel(int id, String startStationName, String endStationName,
                  int earliestDepartureTime, int latestArrivalTime, double shippingCompanyCost, int volume){
        this.id = id;
        this.startStationName = startStationName;
        this.endStationName = endStationName;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestArrivalTime = latestArrivalTime;
        this.shippingCompanyCost = shippingCompanyCost;
        this.volume = volume;
    }

    public String toString(){
        return String.format("Parcel[%d]: %s->%s, (%d, %d), $%.2f, %d", id, startStationName, endStationName,
                earliestDepartureTime, latestArrivalTime, shippingCompanyCost, volume);
    }
}
