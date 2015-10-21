package nl.twente.bms.model.conf;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nl.twente.bms.model.struct.Station;
import nl.twente.bms.utils.ExcelHandler;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class to record Station Config object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class StationConfig {
    private static final Logger logger = LoggerFactory.getLogger(StationConfig.class);

    private Table<String, String, Integer> directDistanceTable;
    private SimpleWeightedGraph<Station, DefaultWeightedEdge> stationGraph;

    public StationConfig(int numStations, ExcelHandler excelHandler) {
        directDistanceTable = createStationDirectDistanceTable(numStations, excelHandler);
        stationGraph = createStationGraph(numStations, excelHandler);
    }

    private Table<String, String, Integer> createStationDirectDistanceTable(int numStations, ExcelHandler excelHandler){
        Table<String, String, Integer> distanceTable = HashBasedTable.create();

        String[] stationNames = excelHandler.xlsread("Direct Distance", 0, 1, numStations);

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Direct Distance", col, row));
                distanceTable.put(stationNames[col-1], stationNames[row-1], distance);
                logger.debug("{} <> {}: {}", stationNames[col-1], stationNames[row-1], distance);
            }
        }

        return distanceTable;
    }

    private SimpleWeightedGraph<Station, DefaultWeightedEdge> createStationGraph(int numStations, ExcelHandler excelHandler){
        SimpleWeightedGraph<Station, DefaultWeightedEdge> graph =
                new SimpleWeightedGraph<Station, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        String[] stationNames = excelHandler.xlsread("Distance", 0, 1, numStations);
        Station[] stationArray = new Station[numStations];
        for(int i = 0; i < stationNames.length; i++){
            stationArray[i] = new Station(stationNames[i]);
            graph.addVertex(stationArray[i]);
        }

        for(int col = 1; col < numStations + 1; col++ ){
            for(int row = col + 1; row < numStations +1; row++){
                int distance = Integer.parseInt(excelHandler.xlsread("Distance", col, row));
                // distance != 0 means that two stations have an edge
                if(distance != 0){
                    DefaultWeightedEdge edge = graph.addEdge(stationArray[col-1], stationArray[row-1]);
                    graph.setEdgeWeight(edge, distance);
                    logger.debug("{} <> {}: {}", stationNames[col - 1], stationNames[row - 1], distance);
                }
            }
        }

        return graph;
    }
}
