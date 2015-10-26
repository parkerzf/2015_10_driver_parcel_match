package nl.twente.bms.model.conf;

import grph.properties.Property;
import grph.properties.StringProperty;
import nl.twente.bms.algo.struct.WeightedGrph;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * The class to record Station Config object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class StationConfig {
    private static final Logger logger = LoggerFactory.getLogger(StationConfig.class);


    private WeightedGrph stationGrph;

    public StationConfig(int numStations, ExcelHandler excelHandler) {;
        stationGrph = createStationGrph(numStations, excelHandler);
    }

    private WeightedGrph createStationGrph(int numStations,
                                           ExcelHandler excelHandler) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        WeightedGrph g = new WeightedGrph();
        Property vLabel = new StringProperty("Label");
        String[] stationNames = excelHandler.xlsread("Distance", 0, 1, numStations);
        for(int i = 0; i < numStations; i ++ ){
            g.addVertex(i+1);
            vLabel.setValue(i+1, stationNames[i]);
        }

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Direct Distance", col, row));
                g.setDirectDistance(col, row, distance);
                logger.debug("{} <> {}: {}", col, row, distance);
            }
        }

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = col + 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Distance", col, row));
                // distance != 0 means that two stations have an edge
                if(distance != 0){
                    int e = g.addUndirectedSimpleEdge(col, row);
                    g.setEdgeWeight(e, distance);
                    logger.debug("{} <> {}: {}", col, row, g.getEdgeWeight(e));
                }
            }
        }
        g.setVerticesLabel(vLabel);

        return g;
    }

    public WeightedGrph getStationGrph() {
        return stationGrph;
    }
}
