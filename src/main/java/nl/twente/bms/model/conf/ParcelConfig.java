package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntSet;
import com.carrotsearch.hppc.cursors.IntCursor;
import nl.twente.bms.model.elem.Parcel;
import nl.twente.bms.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * The class to record parcel config
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class ParcelConfig {
    private static final Logger logger = LoggerFactory.getLogger(ParcelConfig.class);

    private IntObjectMap<Parcel> parcelMap;
    private ArrayList<Parcel> parcelSortedList;

    private int[] parcelIndices;

    public ParcelConfig(int numParcels, ExcelReader excelReader, boolean isRandom) {

        String[] idStrArray;
        String[] startStationArray;
        String[] endStationArray;
        String[] costArray;
        String[] earliestDepartureArray;
        String[] latestArrivalArray;
        String[] volumeArray;

        if(!isRandom) {
            idStrArray = excelReader.xlsread("Input", 12, 1, numParcels);
            startStationArray = excelReader.xlsread("Input", 13, 1, numParcels);
            endStationArray = excelReader.xlsread("Input", 14, 1, numParcels);
            costArray = excelReader.xlsread("Input", 15, 1, numParcels);
            earliestDepartureArray = excelReader.xlsread("Input", 16, 1, numParcels);
            latestArrivalArray = excelReader.xlsread("Input", 17, 1, numParcels);
            volumeArray = excelReader.xlsread("Input", 18, 1, numParcels);

            parcelIndices = IntStream.rangeClosed(1, numParcels).toArray();
        }
        else{
            Random rand = new Random(System.currentTimeMillis());
            idStrArray = new String[numParcels];
            startStationArray = new String[numParcels];
            endStationArray = new String[numParcels];
            costArray = new String[numParcels];
            earliestDepartureArray = new String[numParcels];
            latestArrivalArray = new String[numParcels];
            volumeArray = new String[numParcels];

            IntSet generated = new IntOpenHashSet();
            while (generated.size() < numParcels)
            {
                Integer next = rand.nextInt(1000) + 1;
                generated.add(next);
            }

            parcelIndices = generated.toArray();
            //parcelIndices = new int[]{252,58,178,594,208,5,987,908,682,92,42,475,229,527,245,40,345,606,668,854,598,486,653,394,265,239,680,907,814,755,168,118,388,368,555,339,970,333,615,635,228,859,113,923,666,705,718,219,261,675,62,468,828,647,144,652,752,220,23,874,622,643,37,478,827,163,768,663,837,591,724,156,893,743,465,831,494,503,214,204,678,686,690,904,738,684,170,320,378,485};

            for(int i = 0; i < numParcels; i++){
                idStrArray[i] = excelReader.xlsread("Input", 12, parcelIndices[i]);
                startStationArray[i] = excelReader.xlsread("Input", 13, parcelIndices[i]);
                endStationArray[i] = excelReader.xlsread("Input", 14, parcelIndices[i]);
                costArray[i] =  excelReader.xlsread("Input", 15, parcelIndices[i]);
                earliestDepartureArray[i] = excelReader.xlsread("Input", 16, parcelIndices[i]);
                latestArrivalArray[i] = excelReader.xlsread("Input", 17, parcelIndices[i]);
                volumeArray[i] = excelReader.xlsread("Input", 18, parcelIndices[i]);
            }
        }

        parcelMap = new IntObjectOpenHashMap<>(numParcels);
        parcelSortedList = new ArrayList<>(numParcels);

        for (int i = 0; i < numParcels; i++) {
            Parcel parcel = new Parcel(Integer.parseInt(idStrArray[i]),
                    Integer.parseInt(startStationArray[i]),
                    Integer.parseInt(endStationArray[i]),
                    Integer.parseInt(earliestDepartureArray[i]),
                    Integer.parseInt(latestArrivalArray[i]),
                    Double.parseDouble(costArray[i]),
                    Integer.parseInt(volumeArray[i]));
            parcelMap.put(Integer.parseInt(idStrArray[i]), parcel);
            parcelSortedList.add(parcel);
            logger.debug(parcelMap.get(Integer.parseInt(idStrArray[i])).toString());
        }
        Collections.sort(parcelSortedList);

        logger.info("Parcel sort list size: {}", parcelSortedList.size());
        logger.info("The most costly parcel: {}", parcelSortedList.get(0));
    }

    public ArrayList<Parcel> getParcelSortedList() {
        return parcelSortedList;
    }

    public IntObjectMap<Parcel> getParcelMap() {
        return parcelMap;
    }

    public int getTotalShippingCost(){
        int totalShippingCost = 0;
        for(Parcel parcel: parcelSortedList){
            totalShippingCost += parcel.getShippingCompanyCost();
        }

        return totalShippingCost;
    }


    public IntSet getAssignedDriverIdSetAndReset() {
        IntSet assignedDriverIdSet = new IntOpenHashSet();
        for(Parcel parcel: parcelSortedList){
            if(parcel.getDriverIdSet() == null) continue;
            for(IntCursor driverIdCursor: parcel.getDriverIdSet()){
                assignedDriverIdSet.add(driverIdCursor.value);
            }
            parcel.reset();
        }
        return assignedDriverIdSet;
    }

    public int[] getParcelIndices() {
        return parcelIndices;
    }
}
