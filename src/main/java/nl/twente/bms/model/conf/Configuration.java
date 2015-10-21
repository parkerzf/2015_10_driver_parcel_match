package nl.twente.bms.model.conf;

import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.conf.ParcelConfig;
import nl.twente.bms.model.conf.StationConfig;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class to store the configuration of the matching problem read from the excel
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private int Id;
    /**
     * Problem size configs
     */
    private int numStations;
    private int numDrivers;
    private int numParcels;
    /**
     * Complex configurations
     */
    private StationConfig stationConfig;
    private DriverConfig driverConfig;
    private ParcelConfig parcelConfig;

    /**
     * Weights for the object function
     */
    private double weightTravelTime;
    private double weightNumParcelTransfer;
    private double weightShippingCost;
    private double weightWaitingTime;
    private double weightArrivalTime;
    private double weightExtraTime;

    public Configuration(String confFilePath){
        ExcelHandler excelHandler = new ExcelHandler(confFilePath);
        Id = Integer.parseInt(excelHandler.xlsread("Input", 1, 16));
        numStations = Integer.parseInt(excelHandler.xlsread("Input", 1, 2));
        numDrivers = Integer.parseInt(excelHandler.xlsread("Input", 1, 0));
        numParcels = Integer.parseInt(excelHandler.xlsread("Input", 1, 3));

        logger.debug( "Id: {}", Id);
        logger.debug( "numStations: {}", numStations);
        logger.debug( "numDrivers: {}", numDrivers);
        logger.debug( "numParcels: {}", numParcels);

        String[] weightSettings = excelHandler.xlsread("Input", 1, 7, 12);
        weightTravelTime = Double.parseDouble(weightSettings[0]);
        weightNumParcelTransfer = Double.parseDouble(weightSettings[1]);
        weightShippingCost = Double.parseDouble(weightSettings[2]);
        weightWaitingTime = Double.parseDouble(weightSettings[3]);
        weightArrivalTime = Double.parseDouble(weightSettings[4]);
        weightExtraTime = Double.parseDouble(weightSettings[5]);

        logger.debug( "weightTravelTime: {}", weightTravelTime);
        logger.debug( "weightNumParcelTransfer: {}", weightNumParcelTransfer);
        logger.debug( "weightShippingCost: {}", weightShippingCost);
        logger.debug( "weightWaitingTime: {}", weightWaitingTime);
        logger.debug( "weightArrivalTime: {}", weightArrivalTime);
        logger.debug( "weightExtraTime: {}", weightExtraTime);

        stationConfig = new StationConfig(numStations, excelHandler);
        driverConfig = new DriverConfig(numDrivers, excelHandler);
        parcelConfig = new ParcelConfig(numParcels, excelHandler);

        excelHandler.close();
    }

}
