package nl.twente.bms.model.conf;

import nl.twente.bms.model.struct.Parcel;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The class to record Parcel Config object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class ParcelConfig {
    private static final Logger logger = LoggerFactory.getLogger(ParcelConfig.class);

    private HashMap<Integer, Parcel> parcelMap;
    private ArrayList<Parcel> parcelSortedList;

    public ParcelConfig(int numParcels, ExcelHandler excelHandler) {

        String[] idStrArray = excelHandler.xlsread("Input", 12, 1, numParcels);
        String[] startStationArray = excelHandler.xlsread("Input", 13, 1, numParcels);
        String[] endStationArray = excelHandler.xlsread("Input", 14, 1, numParcels);
        String[] costArray = excelHandler.xlsread("Input", 15, 1, numParcels);
        String[] earliestDepartureArray = excelHandler.xlsread("Input", 16, 1, numParcels);
        String[] latestArrivalArray = excelHandler.xlsread("Input", 17, 1, numParcels);
        String[] volumeArray = excelHandler.xlsread("Input", 18, 1, numParcels);

        parcelMap = new HashMap<Integer, Parcel>(numParcels);
        parcelSortedList = new ArrayList<Parcel>(numParcels);

        for(int i =0; i < numParcels; i++){
            Parcel parcel = new Parcel(Integer.parseInt(idStrArray[i]),
                    startStationArray[i], endStationArray[i],
                    Integer.parseInt(earliestDepartureArray[i]),
                    Integer.parseInt(latestArrivalArray[i]),
                    Double.parseDouble(costArray[i]),
                    Integer.parseInt(volumeArray[i]));
            parcelMap.put(Integer.parseInt(idStrArray[i]),parcel);
            parcelSortedList.add(parcel);
            logger.debug(parcelMap.get(Integer.parseInt(idStrArray[i])).toString());
        }
        Collections.sort(parcelSortedList);

        logger.debug("Parcel sort list size: {}.", parcelSortedList.size());
        logger.debug("The most costly parcel: {}.", parcelSortedList.get(0));
    }
}
