package nl.twente.bms.model.conf;

import grph.properties.Property;
import grph.properties.StringProperty;
import nl.twente.bms.algo.struct.StationGraph;
import nl.twente.bms.utils.ExcelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class to record station config
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class StationConfig {
    private static final Logger logger = LoggerFactory.getLogger(StationConfig.class);


    private StationGraph stationGraph;

    public StationConfig(int numStations, ExcelHandler excelHandler) {
        createStationGrph(numStations, excelHandler);
    }

    private void createStationGrph(int numStations, ExcelHandler excelHandler) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        stationGraph = new StationGraph();
        Property vLabel = new StringProperty("Label");
        String[] stationNames = excelHandler.xlsread("Distance", 0, 1, numStations);
        for(int i = 0; i < numStations; i ++ ){
            stationGraph.addVertex(i + 1);
            vLabel.setValue(i+1, stationNames[i]);
        }

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Direct Distance", col, row));
                stationGraph.setDirectDistance(col, row, distance);
                logger.debug("{} <> {}: {}", col, row, distance);
            }
        }

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = col + 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Distance", col, row));
                // distance != 0 means that two stations have an edge
                if(distance != 0){
                    int e = stationGraph.addUndirectedSimpleEdge(col, row);
                    stationGraph.setEdgeWeight(e, distance);
                    logger.debug("{} <> {}: {}", col, row, stationGraph.getEdgeWeight(e));
                }
            }
        }
        stationGraph.setVerticesLabel(vLabel);
    }

    public StationGraph getStationGraph() {
        return stationGraph;
    }
}
