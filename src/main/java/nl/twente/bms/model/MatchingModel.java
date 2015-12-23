package nl.twente.bms.model;


import com.carrotsearch.hppc.cursors.ObjectCursor;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;
import nl.twente.bms.algo.struct.TimeExpandedGraph;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.conf.ParcelConfig;
import nl.twente.bms.model.conf.StationConfig;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.model.elem.Parcel;
import nl.twente.bms.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
    private double weightTravelDistanceInKilometer;
    private double weightNumParcelTransfer;
    private double weightShippingCost;
    private double weightWaitingTime;
    private double weightExtraTime;

    public MatchingModel(String confFilePath) {
        load(confFilePath);
    }

    private void load(String confFilePath) {
        ExcelReader excelReader = new ExcelReader(confFilePath);
        id = Integer.parseInt(excelReader.xlsread("Input", 1, 15));
        numStations = Integer.parseInt(excelReader.xlsread("Input", 1, 2));
        numDrivers = Integer.parseInt(excelReader.xlsread("Input", 1, 0));
        numParcels = Integer.parseInt(excelReader.xlsread("Input", 1, 3));


        logger.info("id: {}", id);
        logger.info("numStations: {}", numStations);
        logger.info("numDrivers: {}", numDrivers);
        logger.info("numParcels: {}", numParcels);

        String[] weightSettings = excelReader.xlsread("Input", 1, 7, 11);
        weightTravelDistanceInKilometer = Double.parseDouble(weightSettings[0]);
        weightNumParcelTransfer = Double.parseDouble(weightSettings[1]);
        weightShippingCost = Double.parseDouble(weightSettings[2]);
        weightWaitingTime = Double.parseDouble(weightSettings[3]);
        weightExtraTime = Double.parseDouble(weightSettings[4]);

        logger.info("weightTravelDistanceInKilometer: {}", weightTravelDistanceInKilometer);
        logger.info("weightNumParcelTransfer: {}", weightNumParcelTransfer);
        logger.info("weightShippingCost: {}", weightShippingCost);
        logger.info("weightWaitingTime: {}", weightWaitingTime);
        logger.info("weightExtraTime: {}", weightExtraTime);

        stationConfig = new StationConfig(numStations, excelReader);
        driverConfig = new DriverConfig(numDrivers, excelReader, stationConfig.getStationGraph());
        parcelConfig = new ParcelConfig(numParcels, excelReader);

        excelReader.close();
    }

    public void outputShortestDistance(String filePath) throws IOException, WriteException, BiffException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(filePath);
        Workbook wb = Workbook.getWorkbook(is);
        WritableWorkbook wwb = Workbook.createWorkbook(new File(filePath), wb);
        WritableSheet wSheet = wwb.getSheet("Input");
        for(ObjectCursor<Driver> driverCursor : driverConfig.getDriverMap().values()){
            Driver driver = driverCursor.value;

            wSheet.addCell(new Number(6, driver.getId(),
                    driver.getShortestPathDistance(stationConfig.getStationGraph())));
        }

        for(ObjectCursor<Parcel> parcelCursor : parcelConfig.getParcelMap().values()){
            Parcel parcel = parcelCursor.value;
            wSheet.addCell(new Number(19, parcel.getId(),
                    parcel.getShortestPathDistance(stationConfig.getStationGraph())));
        }

        wwb.write();
        wwb.close();
        wb.close();
    }

    /**
     * solve the model by assigning parcels to drivers
     */
    public void solve(){
        TimeExpandedGraph tGraph = driverConfig.getTimeExpandedGraph();
        for(Parcel parcel: parcelConfig.getParcelSortedList()){
            logger.debug("Process " + parcel);
            tGraph.assignParcel(parcel);
        }
    }

    public double computeCost(){
        double totalCost = 0;
        for(ObjectCursor<Driver> driverCursor: driverConfig.getDriverMap().values()){
            totalCost += driverCursor.value.getCost(weightWaitingTime, weightExtraTime, stationConfig.getStationGraph());
        }
        for(ObjectCursor<Parcel> parcelCursor: parcelConfig.getParcelMap().values()){
            totalCost += parcelCursor.value.getCost(weightTravelDistanceInKilometer,
                    weightNumParcelTransfer, weightShippingCost,
                    driverConfig.getTimeExpandedGraph(), stationConfig.getStationGraph());
        }
        return totalCost;
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

    public double getWeightTravelDistanceInKilometer() {
        return weightTravelDistanceInKilometer;
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

}
