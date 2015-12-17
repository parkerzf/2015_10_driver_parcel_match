package nl.twente.bms.model.conf;

import grph.properties.Property;
import grph.properties.StringProperty;
import nl.twente.bms.algo.struct.StationGraph;
import nl.twente.bms.utils.ExcelReader;

/**
 * The class to record station config
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class StationConfig {

    private StationGraph stationGraph;

    public StationConfig(int numStations, ExcelReader excelReader) {
        createStationGraph(numStations, excelReader);
    }

    private void createStationGraph(int numStations, ExcelReader excelReader) {
        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        stationGraph = new StationGraph();
        Property vLabel = new StringProperty("Label");
        for (int i = 0; i < numStations; i++) {
            stationGraph.addVertex(i + 1);
            vLabel.setValue(i + 1, Integer.toString(i + 1));
        }

        for (int col = 0; col < numStations; col++) {
            for (int row = 0; row < numStations; row++) {
                int distance = Integer.parseInt(excelReader.xlsread("Direct Distance", col, row));
                stationGraph.setDirectDistance(col+1, row+1, distance);
            }
        }

        for (int col = 0; col < numStations; col++) {
            for (int row = col; row < numStations; row++) {
                int distance = Integer.parseInt(excelReader.xlsread("Distance", col, row));
                // distance != 0 means that two stations have an edge
                if (distance != 0) {
                    int e = stationGraph.addUndirectedSimpleEdge(col+1, row+1);
                    stationGraph.setEdgeWeight(e, distance);
                }
            }
        }
        stationGraph.setVerticesLabel(vLabel);
        stationGraph.computeAllSourceShortestDistances();
    }

    public StationGraph getStationGraph() {
        return stationGraph;
    }
}
