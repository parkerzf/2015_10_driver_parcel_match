package nl.twente.bms.model.conf;

import nl.twente.bms.model.struct.Driver;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private HashMap<Integer, Driver> driverMap;

    public DriverConfig(int numStations, ExcelHandler excelHandler) {

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

        driverMap = new HashMap<Integer, Driver>(numStations);
        for(int i =0; i < numStations; i++){
            driverMap.put(Integer.parseInt(idStrArray[i]),
                    new Driver(Integer.parseInt(idStrArray[i]),
                            startStationArray[i], endStationArray[i],
                            Integer.parseInt(earliestDepartureArray[i]),
                            Integer.parseInt(latestArrivalArray[i]),
                            Integer.parseInt(capacityArray[i])));
            logger.debug(driverMap.get(Integer.parseInt(idStrArray[i])).toString());
        }

    }
}
