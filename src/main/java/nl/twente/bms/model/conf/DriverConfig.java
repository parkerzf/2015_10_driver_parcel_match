package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import nl.twente.bms.algo.struct.StationGraph;
import nl.twente.bms.algo.struct.TimeExpandedGraph;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.model.elem.Offer;
import nl.twente.bms.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class to record driver config
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class DriverConfig {
    private static final Logger logger = LoggerFactory.getLogger(DriverConfig.class);

    private int nextOfferId;
    private IntObjectMap<Driver> driverMap;
    private IntObjectMap<Offer> offerMap;
    private TimeExpandedGraph timeExpandedGraph;

    public DriverConfig(int numDrivers, ExcelReader excelReader,
                        StationGraph stationGraph) {
        nextOfferId = 0;

        double detour = Double.parseDouble(excelReader.xlsread("Input", 1, 1));
        double speed = Double.parseDouble(excelReader.xlsread("Input", 1, 4));
        double delay = Double.parseDouble(excelReader.xlsread("Input", 1, 17));
        int hold = Integer.parseInt(excelReader.xlsread("Input", 1, 18));

        String[] idStrArray = excelReader.xlsread("Input", 3, 1, numDrivers);
        String[] startStationArray = excelReader.xlsread("Input", 4, 1, numDrivers);
        String[] endStationArray = excelReader.xlsread("Input", 5, 1, numDrivers);
        String[] earliestDepartureArray = excelReader.xlsread("Input", 7, 1, numDrivers);
        String[] latestArrivalArray = excelReader.xlsread("Input", 8, 1, numDrivers);
        String[] capacityArray = excelReader.xlsread("Input", 10, 1, numDrivers);

        timeExpandedGraph = new TimeExpandedGraph(stationGraph, this);

        //index driver object in driver map
        driverMap = new IntObjectOpenHashMap<>(numDrivers);
        offerMap = new IntObjectOpenHashMap<>(numDrivers);
        for (int i = 0; i < numDrivers; i++) {
            Driver driver = new Driver(Integer.parseInt(idStrArray[i]),
                    Integer.parseInt(startStationArray[i]),
                    Integer.parseInt(endStationArray[i]),
                    detour,
                    delay,
                    Integer.parseInt(earliestDepartureArray[i]),
                    Integer.parseInt(latestArrivalArray[i]),
                    hold,
                    speed,
                    Integer.parseInt(capacityArray[i]));
            driverMap.put(Integer.parseInt(idStrArray[i]), driver);
            int currentId = getNextOfferId();
            Offer offer = driver.createInitOffer(currentId, stationGraph);
            offerMap.put(currentId, offer);
            timeExpandedGraph.addOffer(offer);
            logger.info(offer.toString());
        }
//         timeExpandedGraph.display();
    }

    public int getNextOfferId() {
        return nextOfferId++;
    }

    public TimeExpandedGraph getTimeExpandedGraph() {
        return timeExpandedGraph;
    }

    public Offer getOfferById(int offerId) {
        return offerMap.get(offerId);
    }

    public void addOffer(Offer offer) {
        offerMap.put(offer.getId(), offer);
    }

    public IntObjectMap<Driver> getDriverMap() {
        return driverMap;
    }
}
