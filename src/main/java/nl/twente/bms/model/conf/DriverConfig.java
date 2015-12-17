package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.*;
import nl.twente.bms.algo.struct.WeightedGrph;
import nl.twente.bms.algo.struct.WeightedSmartPath;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The class to record Driver Config object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class DriverConfig {
    private static final Logger logger = LoggerFactory.getLogger(DriverConfig.class);

    private double maxDetour;
    private double avgSpeed;

    private IntObjectMap<Driver> driverMap;
    private ArrayList<Driver> driverList;

    public DriverConfig(int numStations, ExcelHandler excelHandler, WeightedGrph stationGrph) {

        maxDetour = Double.parseDouble(excelHandler.xlsread("Input", 1, 1));
        avgSpeed = Double.parseDouble(excelHandler.xlsread("Input", 1, 4));

        logger.debug( "maxDetour: {}", maxDetour);
        logger.debug( "avgSpeed: {}", avgSpeed);

        String[] idStrArray = excelHandler.xlsread("Input", 3, 1, numStations);
        String[] startStationArray = excelHandler.xlsread("Input", 4, 1, numStations);
        String[] endStationArray = excelHandler.xlsread("Input", 5, 1, numStations);
        String[] earliestDepartureArray = excelHandler.xlsread("Input", 7, 1, numStations);
        String[] latestArrivalArray = excelHandler.xlsread("Input", 8, 1, numStations);
        String[] capacityArray = excelHandler.xlsread("Input", 10, 1, numStations);

        //index driver object in driver map
        driverMap = new IntObjectOpenHashMap<Driver>(numStations);
        driverList = new ArrayList<Driver>(numStations);
        for(int i =0; i < numStations; i++){
            Driver driver = new Driver(Integer.parseInt(idStrArray[i]),
                    Integer.parseInt(startStationArray[i]),
                    Integer.parseInt(endStationArray[i]),
                    Integer.parseInt(earliestDepartureArray[i]),
                    ((int) Double.parseDouble(latestArrivalArray[i])),
                    Integer.parseInt(capacityArray[i]));

            setMaxDetourPaths(driver, stationGrph);
            driverMap.put(Integer.parseInt(idStrArray[i]), driver);
            driverList.add(driver);
            logger.debug(driver.toString());
        }

    }

    /**
     * Assign max detour paths for all the drivers
     * @param driver The driver to compute the max detour
     * @param stationGrph The station graph to search for the max detour paths
     */
    private void setMaxDetourPaths(Driver driver, WeightedGrph stationGrph){
        Collection<WeightedSmartPath> maxDetourPaths = stationGrph.getMaxDetourPaths(driver.getStartStationId(),
                driver.getEndStationId(), maxDetour);
        driver.setMaxDetourPaths(maxDetourPaths);
    }

    public ArrayList<Driver> getDriverList() {
        return driverList;
    }
}
