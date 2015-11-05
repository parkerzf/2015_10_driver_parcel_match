package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.properties.NumericalProperty;
import grph.properties.StringProperty;
import nl.twente.bms.model.elem.Driver;
import nl.twente.bms.model.elem.Offer;

/**
 * The class to store the time expanded graph with respect ot the station graph
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */

public class TimeExpandedGraph extends StationGraph {

    private final NumericalProperty nodeTimeProperty;
    private final NumericalProperty nodeStationIdProperty;
    private final NumericalProperty nodeDriverIdProperty;
    private final StringProperty nodeLabelProperty;

    private final StationGraph stationGraph;
    // stationId --> timeTable
    private IntObjectMap<TimeTable> stationTimeTableMap;

    public TimeExpandedGraph(StationGraph stationGraph){
        nodeTimeProperty = new NumericalProperty ("time", 11, 1441);
        nodeStationIdProperty = new NumericalProperty ("station", 16, 65535);
        nodeDriverIdProperty = new NumericalProperty ("driver", 16, 65535);
        nodeLabelProperty = new StringProperty("Label");

        this.stationGraph = stationGraph;
        stationTimeTableMap = new IntObjectOpenHashMap<>(stationGraph.getNumberOfVertices());
    }

    /**
     * Add driver offer's feasible paths to the time expanded graph
     * @param driver
     * @param offer
     */
    public void addOffer(Driver driver, Offer offer){
        IntArrayList candidate = new IntArrayList();

        for(IntCursor cursor: stationGraph.getVertices()){
            int v = cursor.value;
            if(v == driver.getSource() || v == driver.getTarget() ) continue;
            if(stationGraph.isFeasible(driver, v)) candidate.add(v);
        }

        int sourceTimeVertex = this.addTimeVertex(driver.getDepartureTime(), driver.getSource(), driver);
        int targetTime = driver.getDepartureTime() +
                stationGraph.getDuration(driver, driver.getSource(), driver.getTarget());
        int targetTimeVertex = this.addTimeVertex(targetTime, driver.getTarget(), driver);

        // assign the special case that directly travel from driver.sourceTimeVertex to driver.targetTimeVertex
        int driverEdge = this.addDirectedSimpleEdge(sourceTimeVertex, targetTimeVertex);
        setEdgeWeight(driverEdge, targetTime - driver.getDepartureTime());

        for(IntCursor source_candidate_cursor: candidate) {
            for (IntCursor target_candidate_cursor : candidate) {
                int s = source_candidate_cursor.value;
                int t = target_candidate_cursor.value;

                if (s == t || stationGraph.isFeasible(driver, s, t)) {

                    int sTime = driver.getDepartureTime() + stationGraph.getDuration(driver, driver.getSource(), s);
                    int sTimeVertex = this.addTimeVertex(sTime, s, driver);

                    if(this.getEdgesConnecting(sourceTimeVertex, sTimeVertex).isEmpty()) {
                        int edgeDriverSourceSource = this.addDirectedSimpleEdge(sourceTimeVertex, sTimeVertex);
                        setEdgeWeight(edgeDriverSourceSource, sTime - driver.getDepartureTime());
                    }

                    int tTime = sTime + stationGraph.getDuration(driver, s, t);
                    int tTimeVertex = this.addTimeVertex(tTime, t, driver);

                    if (s != t && this.getEdgesConnecting(sTimeVertex, tTimeVertex).isEmpty()) {
                        int e = this.addDirectedSimpleEdge(sTimeVertex, tTimeVertex);
                        setEdgeWeight(e, tTime - sTime);
                    }

                    int driverTargetTime = tTime + stationGraph.getDuration(driver, t, driver.getTarget());
                    if(driverTargetTime != targetTime){
                        int driverTargetTimeVertex = this.addTimeVertex(driverTargetTime, driver.getTarget(), driver);
                        if(this.getEdgesConnecting(tTimeVertex, driverTargetTimeVertex).isEmpty()) {
                            int edgeTargetDriverTarget = this.addDirectedSimpleEdge(tTimeVertex, driverTargetTimeVertex);
                            setEdgeWeight(edgeTargetDriverTarget, driverTargetTime - tTime);
                        }
                    }
                    else if(this.getEdgesConnecting(tTimeVertex, targetTimeVertex).isEmpty()) {
                        int edgeTargetDriverTarget = this.addDirectedSimpleEdge(tTimeVertex, targetTimeVertex);
                        setEdgeWeight(edgeTargetDriverTarget, targetTime - tTime);
                    }
                }
            }
        }
        this.setVerticesLabel(nodeLabelProperty);
    }

    /**
     * Add a time vertex with the associated properties
     * @param time the time arriving the station
     * @param stationId the station id in station graph
     * @param driver the driver travel on station graph
     * @return the time vertex id
     */
    private int addTimeVertex(int time, int stationId, Driver driver) {
        TimeTable timeTable = stationTimeTableMap.get(stationId);
        if (timeTable == null) {
            timeTable = new TimeTable();
            stationTimeTableMap.put(stationId, timeTable);
        }
        int vertex = timeTable.getTimeVertex(time, driver, nodeDriverIdProperty);
        if(vertex != -1) return vertex;

        vertex = this.addVertex();
        nodeTimeProperty.setValue(vertex, time);
        nodeStationIdProperty.setValue(vertex, stationId);
        nodeDriverIdProperty.setValue(vertex, driver.getId());
        nodeLabelProperty.setValue(vertex, vertex + "_" + stationGraph.getLabel(stationId) + "@" + time);

        timeTable.addTimeVertex(this, vertex, nodeTimeProperty, driver);
        return vertex;
    }

    public void markOfferRemoved(Driver driver, Offer offer){
        //TODO
    }

}
