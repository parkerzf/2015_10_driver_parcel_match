package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import grph.properties.NumericalProperty;
import nl.twente.bms.model.elem.Driver;

/**
 * The class to store the time table for one station
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class TimeTable {
    private IntArrayList[] timeSlots;

    public TimeTable(){
        // one day has 1440 mins, so we split one day's timeTable to be 1440 time slots
        timeSlots = new IntArrayList[1440];
    }

    /**
     * Check the time vertex presence
     * @param time the time at this station
     * @param driver the current driver
     * @param driverIdProperty the vertex driverIdProperty in time expanded graph
     * @return the time vertex if exist, otherwise -1
     */
    public int getTimeVertex(int time, Driver driver, NumericalProperty driverIdProperty){
        if(timeSlots[time] == null) return -1;
        int lastVertexId = timeSlots[time].get(timeSlots[time].size()-1);
        if(driverIdProperty.getValueAsInt(lastVertexId) == driver.getId()){
            return lastVertexId;
        }
        return -1;
    }

    /**
     * Add time vertex to the time table of the station
     * @param graph the time expanded graph
     * @param vertex the time vertex id in the time expanded graph
     * @param timeProperty the vertex timeProperty
     * @param driver the driver associated with the vertex
     */
    public void addTimeVertex(TimeExpandedGraph graph, int vertex, NumericalProperty timeProperty, Driver driver){
        int vertexTime = timeProperty.getValueAsInt(vertex);

        // Adds the vertex to the nearest past vertex with in [vertexTime - holdDuration, vertexTime]
        int nearestPastVertex = -1;
        int nearestPastDelay = -1;
        for(int offset = 0; offset <= driver.getHoldDuration(); offset++){
            if(vertexTime - offset < 0) break;
            if(timeSlots[vertexTime - offset] != null) {
                nearestPastVertex = timeSlots[vertexTime - offset].get(timeSlots[vertexTime - offset].size() - 1);
                nearestPastDelay = offset;
                break;
            }
        }

        if(nearestPastVertex != -1){
            int e = graph.addDirectedSimpleEdge(nearestPastVertex, vertex);
            graph.setEdgeWeight(e, nearestPastDelay);
        }

        if(timeSlots[vertexTime] == null){
            timeSlots[vertexTime] = new IntArrayList();
        }
        timeSlots[vertexTime].add(vertex);

        // Adds the vertex to the nearest future vertex with in (vertexTime, vertexTime + holdDuration]
        int nearestFutureVertex = -1;
        int nearestFutureDelay = -1;
        for(int offset = 1; offset <= driver.getHoldDuration(); offset++){
            if(vertexTime + offset > 1439) break;
            if(timeSlots[vertexTime + offset] != null) {
                nearestFutureVertex = timeSlots[vertexTime + offset].get(timeSlots[vertexTime + offset].size() - 1);
                nearestFutureDelay = offset;
                break;
            }
        }

        if(nearestFutureVertex != -1){
            int e = graph.addDirectedSimpleEdge(vertex, nearestFutureVertex);
            graph.setEdgeWeight(e, nearestFutureDelay);
        }
    }

    public void removeDriverTimeVertex(int nodeId, NumericalProperty nodeTimeProperty){
    //TODO
    }
}
