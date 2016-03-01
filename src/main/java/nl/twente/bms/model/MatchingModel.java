package nl.twente.bms.model;


import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
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
import java.util.Arrays;

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
    private double detour;

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
        this(confFilePath, -1, -1);
    }

    public MatchingModel(String confFilePath, int numDriversInput, int numParcelsInput) {
        this(confFilePath, numDriversInput, numParcelsInput, -1);
    }

    public MatchingModel(String confFilePath, int numDriversInput, int numParcelsInput, double detourInput) {
        this(confFilePath, numDriversInput, numParcelsInput, detourInput, false, null, null);
    }

    public MatchingModel(String confFilePath, int numDriversInput,
                         int numParcelsInput, double detourInput, boolean isRandom, String driverIndicesStr, String parcelIndicesStr) {
        ExcelReader excelReader = new ExcelReader(confFilePath);
        id = Integer.parseInt(excelReader.xlsread("Input", 1, 15));
        numStations = Integer.parseInt(excelReader.xlsread("Input", 1, 2));
        if(numDriversInput == -1){
            numDrivers = Integer.parseInt(excelReader.xlsread("Input", 1, 0));
        }
        else{
            numDrivers = numDriversInput;
        }

        if(numParcelsInput == -1){
            numParcels = Integer.parseInt(excelReader.xlsread("Input", 1, 3));
        }
        else{
            numParcels = numParcelsInput;
        }

        if(detourInput == -1){
            detour = Double.parseDouble(excelReader.xlsread("Input", 1, 1));
        }
        else {
            detour = detourInput;
        }


        logger.info("id: {}", id);
        logger.info("numStations: {}", numStations);
        logger.info("numDrivers: {}", numDrivers);
        logger.info("numParcels: {}", numParcels);
        logger.info("detour: {}", detour);
        logger.info("driverIndicesStr: {}", driverIndicesStr);
        logger.info("parcelIndicesStr: {}", parcelIndicesStr);
        int[] driverIndicesIn = null;
        int[] parcelIndicesIn = null;
        if(driverIndicesStr != null){
            driverIndicesIn = Arrays.stream(driverIndicesStr.split(","))
                    .map(String::trim).mapToInt(Integer::parseInt).toArray();
        }

        if(parcelIndicesStr != null){
            parcelIndicesIn = Arrays.stream(parcelIndicesStr.split(","))
                    .map(String::trim).mapToInt(Integer::parseInt).toArray();
        }

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
        driverConfig = new DriverConfig(numDrivers, detour, excelReader, stationConfig.getStationGraph(), isRandom, driverIndicesIn);
        parcelConfig = new ParcelConfig(numParcels, excelReader, isRandom, parcelIndicesIn);

        excelReader.close();
    }

    public void shuffle(boolean isFullRandom){
        IntSet assignedDriverIdSet;
        if(isFullRandom){
            assignedDriverIdSet = new IntOpenHashSet();
        }
        else{
            assignedDriverIdSet = parcelConfig.getAssignedDriverIdSet();
        }
        parcelConfig.reset();
        driverConfig.shuffleAndRebuildTimeExpandedGraph(stationConfig.getStationGraph(), assignedDriverIdSet);
    }


    public void outputShortestDistance(String filePath) throws IOException, WriteException, BiffException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(filePath);
        Workbook wb = Workbook.getWorkbook(is);
        WritableWorkbook wwb = Workbook.createWorkbook(new File(filePath), wb);
        WritableSheet wSheet = wwb.getSheet("Input");
        for(ObjectCursor<Driver> driverCursor : driverConfig.getDriverList()){
            Driver driver = driverCursor.value;

            wSheet.addCell(new Number(6, driver.getId(),
                    driver.getShortestPathDistance()));
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
        for(ObjectCursor<Driver> driverCursor: driverConfig.getDriverList()){
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


    public void showDriversAndParcels() {
        System.out.println("Drivers: " + Arrays.toString(driverConfig.getDriverIndices()));
//        System.out.println("Drivers: ");
//        for (ObjectCursor<Driver> driver : driverConfig.getDriverList()) {
//            System.out.println(driver.value.getExecelOutoput());
//        }
        System.out.println("Parcels: " + Arrays.toString(parcelConfig.getParcelIndices()));
//        System.out.println("Parcels: ");
//        for (Parcel parcel : parcelConfig.getParcelSortedList()) {
//            System.out.println(parcel.getExecelOutoput());
//        }
    }
}
