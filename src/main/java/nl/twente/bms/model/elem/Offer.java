package nl.twente.bms.model.elem;

/**
 * The class to store the offer object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Offer {
    private int id;

    private Driver driver;

    //private boolean isFixed; //???

    private int source;
    private int target;

    private int departureTime;
    private int capacity;

    public Offer(int id, int source, int target, int departureTime, int capacity, Driver driver) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.driver = driver;
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

    public int getArrivalTimeFromSource(int distance) {
        return departureTime + (int) (distance * 60 / getSpeed());
    }

    public String toString() {
        return String.format("Offer[%d] in Driver[%d]: %d->%d, Depart: %d, Capacity: %d",
                id, getDriverId(), source, target, departureTime, capacity);
    }
}
