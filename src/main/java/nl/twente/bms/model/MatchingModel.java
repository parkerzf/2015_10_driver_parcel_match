package nl.twente.bms.model;


import nl.twente.bms.algo.struct.TimeExpandedGraph;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.conf.ParcelConfig;
import nl.twente.bms.model.conf.StationConfig;
import nl.twente.bms.model.elem.Parcel;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

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

    private HashMap<String, Integer> stationNameIndexMap;

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

    public MatchingModel(String confFilePath) {
        load(confFilePath);
    }

    private void load(String confFilePath) {
        ExcelHandler excelHandler = new ExcelHandler(confFilePath);
        id = Integer.parseInt(excelHandler.xlsread("Input", 1, 16));
        numStations = Integer.parseInt(excelHandler.xlsread("Input", 1, 2));
        numDrivers = Integer.parseInt(excelHandler.xlsread("Input", 1, 0));
        numParcels = Integer.parseInt(excelHandler.xlsread("Input", 1, 3));

        String[] stationNames = excelHandler.xlsread("Distance", 0, 1, numStations);
        stationNameIndexMap = new HashMap<>(numStations);
        for (int i = 0; i < stationNames.length; i++) {
            stationNameIndexMap.put(stationNames[i], i + 1);
        }

        logger.debug("id: {}", id);
        logger.debug("numStations: {}", numStations);
        logger.debug("numDrivers: {}", numDrivers);
        logger.debug("numParcels: {}", numParcels);

        String[] weightSettings = excelHandler.xlsread("Input", 1, 7, 12);
        weightTravelTime = Double.parseDouble(weightSettings[0]);
        weightNumParcelTransfer = Double.parseDouble(weightSettings[1]);
        weightShippingCost = Double.parseDouble(weightSettings[2]);
        weightWaitingTime = Double.parseDouble(weightSettings[3]);
        weightArrivalTime = Double.parseDouble(weightSettings[4]);
        weightExtraTime = Double.parseDouble(weightSettings[5]);

        logger.debug("weightTravelTime: {}", weightTravelTime);
        logger.debug("weightNumParcelTransfer: {}", weightNumParcelTransfer);
        logger.debug("weightShippingCost: {}", weightShippingCost);
        logger.debug("weightWaitingTime: {}", weightWaitingTime);
        logger.debug("weightArrivalTime: {}", weightArrivalTime);
        logger.debug("weightExtraTime: {}", weightExtraTime);

        stationConfig = new StationConfig(numStations, excelHandler);
        driverConfig = new DriverConfig(numDrivers, excelHandler, stationNameIndexMap, stationConfig.getStationGraph());
        parcelConfig = new ParcelConfig(numParcels, excelHandler, stationNameIndexMap);

        excelHandler.close();
    }

    /**
     * solve the model by assigning parcels to drivers
     */
    public void solve(){
        TimeExpandedGraph tGraph = driverConfig.getTimeExpandedGraph();
        int count = 0;
        for(Parcel parcel: parcelConfig.getParcelSortedList()){
            System.out.println("Assign parcel: " + parcel);
            tGraph.assignParcel(parcel);
            if(count++ >= 1) break;
        }
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

    public HashMap<String, Integer> getStationNameIndexMap() {
        return stationNameIndexMap;
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

    public double getWeightArrivalTime() {
        return weightArrivalTime;
    }

    public double getWeightExtraTime() {
        return weightExtraTime;
    }
}
