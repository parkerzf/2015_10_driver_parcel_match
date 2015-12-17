package nl.twente.bms.model.conf;

import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import nl.twente.bms.model.elem.Parcel;
import nl.twente.bms.utils.ExcelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

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

    public ParcelConfig(int numParcels, ExcelReader excelReader) {

        String[] idStrArray = excelReader.xlsread("Input", 12, 1, numParcels);
        String[] startStationArray = excelReader.xlsread("Input", 13, 1, numParcels);
        String[] endStationArray = excelReader.xlsread("Input", 14, 1, numParcels);
        String[] costArray = excelReader.xlsread("Input", 15, 1, numParcels);
        String[] earliestDepartureArray = excelReader.xlsread("Input", 16, 1, numParcels);
        String[] latestArrivalArray = excelReader.xlsread("Input", 17, 1, numParcels);
        String[] volumeArray = excelReader.xlsread("Input", 18, 1, numParcels);

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
}
