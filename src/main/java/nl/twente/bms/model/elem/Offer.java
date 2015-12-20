package nl.twente.bms.model.elem;

import nl.twente.bms.algo.struct.StationGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * The class to store the offer object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */

//TODO add parallel offer that start periodically
public class Offer {
    private int id;

    private boolean extendableOffer;

    private Driver driver;

    private int source;
    private int target;

    private int sourceTimeVertex = -1;
    private int targetTimeVertex = -1;

    private int departureTime;
    private int capacity;

    private int maxDuration;
    private int maxDetour;

    private List<Parcel> parcels;

    public Offer(int id, int source, int target, int departureTime, int capacity, Driver driver, StationGraph stationGraph) {
        this.id = id;
        this.extendableOffer = true;
        this.source = source;
        this.target = target;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.driver = driver;

        int distanceSourceTarget = stationGraph.getShortestDistance(source, target);
        maxDetour = (int) ((1 + getEpsilon()) * distanceSourceTarget);
        maxDuration = (int) ((1 + getGamma()) * getDuration(distanceSourceTarget));

        this.parcels = new ArrayList<>();
        if(isFeasible()){
            this.driver.addOffer(this);
        }
    }

    public Offer(int id, Offer offer, int volume){
        this.id = id;
        this.extendableOffer = false;
        this.source = offer.source;
        this.sourceTimeVertex = offer.sourceTimeVertex;
        this.departureTime = offer.departureTime;
        this.capacity = offer.capacity - volume;
        this.driver = offer.driver;
        maxDetour = offer.getMaxDetour();
        maxDuration = offer.getMaxDuration();
        this.parcels = new ArrayList<>();
        if(isFeasible()){
            this.driver.addOffer(this);

        }
    }

    public Offer(int id, Offer offer, int newSourceTimeVertexId, int newSourceStationId, int timeAtNewSourceStationId) {
        this.id = id;
        this.extendableOffer = offer.extendableOffer;
        this.source = newSourceStationId;
        this.sourceTimeVertex = newSourceTimeVertexId;
        this.target = offer.target;
        this.targetTimeVertex = offer.targetTimeVertex;
        this.departureTime = timeAtNewSourceStationId;
        this.capacity = offer.capacity;
        this.driver = offer.driver;
        maxDuration = offer.getMaxDuration() - (this.departureTime - offer.departureTime);
        maxDetour = offer.getMaxDetour() - getDistance(this.departureTime - offer.departureTime);
        this.parcels = new ArrayList<>();
        if(isFeasible()){
            this.driver.addOffer(this);
        }
    }

    public boolean isFeasible(){
        return  source != target &&
                capacity >0 && maxDuration > 0 && maxDetour > 0;
    }

    public int getId() {
        return id;
    }

    public int getDriverId() {
        return driver.getId();
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public double getEpsilon() {
        return driver.getEpsilon();
    }

    public double getGamma() {
        return driver.getGamma();
    }

    public int getDepartureTime() {
        return departureTime;
    }

    public int getHoldDuration() {
        return driver.getHoldDuration();
    }

    public double getSpeed() {
        return driver.getSpeed();
    }

    public int getCapacity() {
        return capacity;
    }

    public Driver getDriver() {
        return driver;
    }

    public int getDuration(int distance) {
        return driver.getDuration(distance);
    }
    public int getDistance(int duration) {
        return driver.getDistance(duration);
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public int getMaxDetour() {
        return maxDetour;
    }

    public void addParcel(Parcel parcel){
        parcels.add(parcel);
    }

    public boolean isExtendableOffer() {
        return extendableOffer;
    }

    public String toString() {
        return String.format("Offer[%d] in Driver[%d]: %d(%d)->%d(%d), Capacity: %d, " +
                             "Depart: %d, maxDuration: %d, maxDetour: %d",
                id, getDriverId(), source, sourceTimeVertex, target, targetTimeVertex,
                capacity, departureTime, maxDuration, maxDetour);
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public void setSourceTimeVertex(int sourceTimeVertex) {
        this.sourceTimeVertex = sourceTimeVertex;
    }

    public void setTargetTimeVertex(int targetTimeVertex) {
        this.targetTimeVertex = targetTimeVertex;
    }

    public int getSourceTimeVertex() {
        return sourceTimeVertex;
    }
}
