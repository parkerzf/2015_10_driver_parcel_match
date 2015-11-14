package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.properties.NumericalProperty;
import grph.properties.StringProperty;
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
    private final NumericalProperty nodeOfferIdProperty;
    private final StringProperty nodeLabelProperty;

    private final StationGraph stationGraph;
    // stationId --> timeTable
    private IntObjectMap<TimeTable> stationTimeTableMap;

    public TimeExpandedGraph(StationGraph stationGraph) {
        nodeTimeProperty = new NumericalProperty("time", 11, 1441);
        nodeStationIdProperty = new NumericalProperty("station", 16, 65535);
        nodeOfferIdProperty = new NumericalProperty("driver", 16, 65535);
        nodeLabelProperty = new StringProperty("Label");

        this.stationGraph = stationGraph;
        stationTimeTableMap = new IntObjectOpenHashMap<>(stationGraph.getNumberOfVertices());
    }

    /**
     * Add driver offer's feasible paths to the time expanded graph
     *
     * @param offer driver's offer
     */
    public void addOffer(Offer offer) {
        IntArrayList candidates = new IntArrayList();

        for (IntCursor cursor : stationGraph.getVertices()) {
            int v = cursor.value;
            if (v == offer.getSource() || v == offer.getTarget()) continue;
            if (stationGraph.isFeasible(offer, v)) candidates.add(v);
        }

        int sourceTimeVertex = this.addTimeVertex(offer.getDepartureTime(), offer.getSource(), offer);
        int targetTime = offer.getDepartureTime() +
                stationGraph.getDuration(offer, offer.getSource(), offer.getTarget());
        int targetTimeVertex = this.addTimeVertex(targetTime, offer.getTarget(), offer);

        // assign the special case that directly travel from driver.sourceTimeVertex to driver.targetTimeVertex
        int driverEdge = this.addDirectedSimpleEdge(sourceTimeVertex, targetTimeVertex);
        setEdgeWeight(driverEdge, targetTime - offer.getDepartureTime());

        for (IntCursor source_candidate_cursor : candidates) {
            for (IntCursor target_candidate_cursor : candidates) {
                int s = source_candidate_cursor.value;
                int t = target_candidate_cursor.value;

                if (s == t || stationGraph.isFeasible(offer, s, t)) {

                    int sTime = offer.getDepartureTime() + stationGraph.getDuration(offer, offer.getSource(), s);
                    int sTimeVertex = this.addTimeVertex(sTime, s, offer);

                    if (this.getEdgesConnecting(sourceTimeVertex, sTimeVertex).isEmpty()) {
                        int edgeDriverSourceSource = this.addDirectedSimpleEdge(sourceTimeVertex, sTimeVertex);
                        setEdgeWeight(edgeDriverSourceSource, sTime - offer.getDepartureTime());
                    }

                    int tTime = sTime + stationGraph.getDuration(offer, s, t);
                    int tTimeVertex = this.addTimeVertex(tTime, t, offer);

                    if (s != t && this.getEdgesConnecting(sTimeVertex, tTimeVertex).isEmpty()) {
                        int e = this.addDirectedSimpleEdge(sTimeVertex, tTimeVertex);
                        setEdgeWeight(e, tTime - sTime);
                    }

                    int driverTargetTime = tTime + stationGraph.getDuration(offer, t, offer.getTarget());
                    if (driverTargetTime != targetTime) {
                        int driverTargetTimeVertex = this.addTimeVertex(driverTargetTime, offer.getTarget(), offer);
                        if (this.getEdgesConnecting(tTimeVertex, driverTargetTimeVertex).isEmpty()) {
                            int edgeTargetDriverTarget = this.addDirectedSimpleEdge(tTimeVertex, driverTargetTimeVertex);
                            setEdgeWeight(edgeTargetDriverTarget, driverTargetTime - tTime);
                        }
                    } else if (this.getEdgesConnecting(tTimeVertex, targetTimeVertex).isEmpty()) {
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
     *
     * @param time      the time arriving the station
     * @param stationId the station id in station graph
     * @param offer    the driver'offer on station graph
     * @return the time vertex id
     */
    private int addTimeVertex(int time, int stationId, Offer offer) {
        TimeTable timeTable = stationTimeTableMap.get(stationId);
        if (timeTable == null) {
            timeTable = new TimeTable();
            stationTimeTableMap.put(stationId, timeTable);
        }
        int vertex = timeTable.getTimeVertex(time, offer, nodeOfferIdProperty);
        if (vertex != -1) return vertex;

        vertex = this.addVertex();
        nodeTimeProperty.setValue(vertex, time);
        nodeStationIdProperty.setValue(vertex, stationId);
        nodeOfferIdProperty.setValue(vertex, offer.getId());
        nodeLabelProperty.setValue(vertex, vertex + "_" + stationGraph.getLabel(stationId) + "@" + time);

        timeTable.addTimeVertex(this, vertex, nodeTimeProperty, offer);
        return vertex;
    }

    /**
     * Mark an offer removed
     *
     * @param offer    the driver'offer on station graph
     */
    public void markOfferRemoved(Offer offer) {
        //TODO
    }

}
