package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.*;
import nl.twente.bms.algo.struct.WeightedGrph;
import nl.twente.bms.algo.struct.WeightedSmartPath;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;

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

    public DriverConfig(int numDrivers, ExcelHandler excelHandler, HashMap<String, Integer> stationNameIndexMap,
                        WeightedGrph stationGrph) {

        maxDetour = Double.parseDouble(excelHandler.xlsread("Input", 1, 1));
        avgSpeed = Double.parseDouble(excelHandler.xlsread("Input", 1, 4));

        logger.debug( "maxDetour: {}", maxDetour);
        logger.debug( "avgSpeed: {}", avgSpeed);

        String[] idStrArray = excelHandler.xlsread("Input", 3, 1, numDrivers);
        String[] startStationArray = excelHandler.xlsread("Input", 4, 1, numDrivers);
        String[] endStationArray = excelHandler.xlsread("Input", 5, 1, numDrivers);
        String[] earliestDepartureArray = excelHandler.xlsread("Input", 7, 1, numDrivers);
        String[] latestArrivalArray = excelHandler.xlsread("Input", 8, 1, numDrivers);
        String[] capacityArray = excelHandler.xlsread("Input", 10, 1, numDrivers);

        //index driver object in driver map
        driverMap = new IntObjectOpenHashMap<Driver>(numDrivers);
        for(int i =0; i < numDrivers; i++){
            Driver driver = new Driver(Integer.parseInt(idStrArray[i]),
                    stationNameIndexMap.get(startStationArray[i]),
                    stationNameIndexMap.get(endStationArray[i]),
                    Integer.parseInt(earliestDepartureArray[i]),
                    Integer.parseInt(latestArrivalArray[i]),
                    Integer.parseInt(capacityArray[i]));

            setMaxDetourPaths(driver, stationGrph);
            driverMap.put(Integer.parseInt(idStrArray[i]), driver);
            logger.debug(driver.toString());
        }

    }

    /**
     * Assign max detour paths for each driver
     * @param driver The driver to compute the max detour
     * @param stationGrph The station graph to search for the max detour paths
     */
    private void setMaxDetourPaths(Driver driver, WeightedGrph stationGrph){
        Collection<WeightedSmartPath> maxDetourPaths = stationGrph.getMaxDetourPaths(driver.getStartStationId(),
                driver.getEndStationId(), maxDetour);
        driver.setMaxDetourPaths(maxDetourPaths);
    }
}
