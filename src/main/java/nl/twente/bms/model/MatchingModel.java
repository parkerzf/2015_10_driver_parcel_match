package nl.twente.bms.model;


import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.conf.ParcelConfig;
import nl.twente.bms.model.conf.StationConfig;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class to store the configuration/status of the matching model read from the excel
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class MatchingModel {

    private static final Logger logger = LoggerFactory.getLogger(MatchingModel.class);

    private int id;
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
    private double weightExtraTime;

    public MatchingModel(String confFilePath) {
        load(confFilePath);
    }

    private void load(String confFilePath) {
        ExcelHandler excelHandler = new ExcelHandler(confFilePath);
        id = Integer.parseInt(excelHandler.xlsread("Input", 1, 15));
        numStations = Integer.parseInt(excelHandler.xlsread("Input", 1, 2));
        numDrivers = Integer.parseInt(excelHandler.xlsread("Input", 1, 0));
        numParcels = Integer.parseInt(excelHandler.xlsread("Input", 1, 3));


        logger.info("id: {}", id);
        logger.info("numStations: {}", numStations);
        logger.info("numDrivers: {}", numDrivers);
        logger.info("numParcels: {}", numParcels);

        String[] weightSettings = excelHandler.xlsread("Input", 1, 7, 11);
        weightTravelTime = Double.parseDouble(weightSettings[0]);
        weightNumParcelTransfer = Double.parseDouble(weightSettings[1]);
        weightShippingCost = Double.parseDouble(weightSettings[2]);
        weightWaitingTime = Double.parseDouble(weightSettings[3]);
        weightExtraTime = Double.parseDouble(weightSettings[4]);

        logger.info("weightTravelTime: {}", weightTravelTime);
        logger.info("weightNumParcelTransfer: {}", weightNumParcelTransfer);
        logger.info("weightShippingCost: {}", weightShippingCost);
        logger.info("weightWaitingTime: {}", weightWaitingTime);
        logger.info("weightExtraTime: {}", weightExtraTime);

        stationConfig = new StationConfig(numStations, excelHandler);
        driverConfig = new DriverConfig(numDrivers, excelHandler, stationConfig.getStationGrph());
        parcelConfig = new ParcelConfig(numParcels, excelHandler);

        excelHandler.close();
    }

    public int getId() {
        return id;
    }

    public int getNumStations() {
        return numStations;
    }

    public int getNumDrivers() {
        return numDrivers;
    }

    public int getNumParcels() {
        return numParcels;
    }

    public StationConfig getStationConfig() {
        return stationConfig;
    }

    public DriverConfig getDriverConfig() {
        return driverConfig;
    }

    public ParcelConfig getParcelConfig() {
        return parcelConfig;
    }

    public double getWeightTravelTime() {
        return weightTravelTime;
    }

    public double getWeightNumParcelTransfer() {
        return weightNumParcelTransfer;
    }

    public double getWeightShippingCost() {
        return weightShippingCost;
    }

    public double getWeightWaitingTime() {
        return weightWaitingTime;
    }

    public double getWeightExtraTime() {
        return weightExtraTime;
    }

    public void setHeader(WritableSheet sheet, int driverId) throws WriteException {
        for(int i = 1;  i <= numStations; i++){
            sheet.addCell(new Number(0, i, driverId));
            sheet.addCell(new Number(1, i, i));
            sheet.addCell(new Number(i+1, 0, i));
        }

    }

}
