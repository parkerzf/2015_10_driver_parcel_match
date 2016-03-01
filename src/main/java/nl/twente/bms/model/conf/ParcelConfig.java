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

    public ParcelConfig(int numParcels, ExcelReader excelReader, boolean isRandom, int[] parcelIndicesIn) {

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
            if(parcelIndicesIn != null){
                parcelIndices = parcelIndicesIn;
            }
            else{
                parcelIndices = generated.toArray();
            }
//            parcelIndices = new int[]{664,896,82,488,201,267,1000,853,657,408,262,992,367,715,152,268,519,587,747,951,881,327,360,851,292,410,157,841,806,629};
//            parcelIndices = new int[]{532,988,82,425,545,442,371,976,62,1000,467,754,186,367,997,256,122,521,112,171,482,966,184,3,804,560,373,169,693,955};
//            parcelIndices = new int[]{46,946,896,82,612,437,942,514,589,789,787,446,870,518,114,842,167,802,670,529,604,737,481,334,783,48,218,563,939,972};
//            parcelIndices = new int[]{46,970,445,845,56,285,372,880,776,546,818,509,89,354,416,256,614,675,280,565,489,495,863,380,714,48,615,155,393,254};
//            parcelIndices = new int[]{811,445,337,463,129,612,168,335,378,25,432,494,767,53,24,672,734,469,802,769,792,737,159,644,529,331,481,174,225,539};
//            parcelIndices = new int[]{623,423,850,859,534,213,385,330,562,583,653,768,381,710,208,303,993,727,597,722,233,317,12,813,673,492,947,292,910,393};
//            parcelIndices = new int[]{431,831,392,257,488,921,319,913,4,207,440,478,969,183,662,633,295,93,915,805,792,353,769,515,678,52,35,374,133,191};
//            parcelIndices = new int[]{397,916,26,988,996,153,161,974,437,914,791,740,715,115,587,769,350,869,461,565,495,472,647,147,470,759,829,819,806,994};


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

    public double getTotalShippingCost(){
        double totalShippingCost = 0;
        for(Parcel parcel: parcelSortedList){
            totalShippingCost += parcel.getShippingCompanyCost();
        }

        return totalShippingCost;
    }


    public IntSet getAssignedDriverIdSet() {
        IntSet assignedDriverIdSet = new IntOpenHashSet();
        for(Parcel parcel: parcelSortedList){
            if(parcel.getPath() == null) continue;
            for (IntCursor driverIdCursor : parcel.getDriverIdSet()) {
                assignedDriverIdSet.add(driverIdCursor.value);
            }
        }
        return assignedDriverIdSet;
    }

    public void reset(){
        parcelSortedList.forEach(nl.twente.bms.model.elem.Parcel::reset);
    }

    public int[] getParcelIndices() {
        return parcelIndices;
    }
}
