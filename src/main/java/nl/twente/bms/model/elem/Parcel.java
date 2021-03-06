package nl.twente.bms.model.elem;

import grph.path.Path;
import nl.twente.bms.algo.struct.StationGraph;
import nl.twente.bms.algo.struct.TimeExpandedGraph;
import toools.set.IntSet;

/**
 * The class to store the Parcel object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Parcel implements Comparable<Parcel> {
    private int id;
    private int startStationId;
    private int endStationId;

    private int earliestDepartureTime;
    private int latestArrivalTime;

    private double shippingCompanyCost;
    private int volume;

    private Path path;
    private int numOffers;
    private IntSet driverIdSet;

    public Parcel(int id, int startStationId, int endStationId,
                  int earliestDepartureTime, int latestArrivalTime, double shippingCompanyCost, int volume) {
        this.id = id;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestArrivalTime = latestArrivalTime;
        this.shippingCompanyCost = shippingCompanyCost;
        this.volume = volume;
        this.path = null;
        this.numOffers = 0;
    }

    public int getId() {
        return id;
    }

    public int getStartStationId() {
        return startStationId;
    }

    public int getEndStationId() {
        return endStationId;
    }

    public int getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public int getLatestArrivalTime() {
        return latestArrivalTime;
    }

    public double getShippingCompanyCost() {
        return shippingCompanyCost;
    }

    public int getVolume() {
        return volume;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isAssigned(){
        return path != null;
    }

    public String toString() {
        return String.format("Parcel[%d]: %d->%d, (%d, %d), $%.2f, %d", id, startStationId, endStationId,
                earliestDepartureTime, latestArrivalTime, shippingCompanyCost, volume);
    }

    public String getExecelOutoput(){
        return String.format("%d\t%d\t%d\t%.1f\t%d\t%d\t%d",
                id, startStationId, endStationId, shippingCompanyCost, earliestDepartureTime, latestArrivalTime, volume).replace('.', ',');
    }

    public int compareTo(Parcel p) {
        // order parcel in desc order
        return Double.compare(p.shippingCompanyCost, shippingCompanyCost);
    }

    public int getShortestPathDistance(StationGraph stationGraph){
        return stationGraph.getShortestDistance(startStationId, endStationId);
    }

    public double getCost(double weightTravelDistanceInKilometer, double weightNumParcelTransfer,
                          double weightShippingCost, TimeExpandedGraph tGraph, StationGraph stationGraph){
        if(path == null){
            return weightShippingCost * shippingCompanyCost;
        }
        else{
            int realDistance = 0;
            for(int i = 0; i< path.getNumberOfVertices() - 1; i++){
                int uTimeVertex = path.getVertexAt(i);
                int vTimeVertex = path.getVertexAt(i + 1);
                int u = tGraph.getStationIdFromVertexId(uTimeVertex);
                int v = tGraph.getStationIdFromVertexId(vTimeVertex);

                realDistance += stationGraph.getShortestDistance(u, v);
            }
            double distanceCost = 0.3 * weightTravelDistanceInKilometer * realDistance;

            double transferCost = weightNumParcelTransfer * (numOffers - 1);

            return distanceCost+transferCost;
        }
    }

    public void setNumOffers(int numOffers) {
        this.numOffers = numOffers;
    }

    public void reset(){
        path = null;
        numOffers = 0;
        driverIdSet = null;
    }

    public void setDriverIdSet(IntSet driverIdSet) {
        this.driverIdSet = driverIdSet;
    }

    public IntSet getDriverIdSet() {
        return driverIdSet;
    }
}
