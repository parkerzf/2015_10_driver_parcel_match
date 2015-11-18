package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.algo.search.GraphSearchListener;
import grph.path.Path;
import grph.path.SearchResultWrappedPath;
import grph.properties.NumericalProperty;
import grph.properties.StringProperty;
import nl.twente.bms.algo.DijkstraEnhancedAlgorithm;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.elem.Offer;
import nl.twente.bms.model.elem.Parcel;
import toools.set.IntHashSet;
import toools.set.IntSet;


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
    private IntObjectMap<TimeTableImproved> stationTimeTableMap;

    private IntSet markRemovedOfferIds;

    public TimeExpandedGraph(StationGraph stationGraph) {
        nodeTimeProperty = new NumericalProperty("time", 11, 1441);
        nodeStationIdProperty = new NumericalProperty("station", 16, 65535);
        nodeOfferIdProperty = new NumericalProperty("offer", 16, 65535);
        nodeLabelProperty = new StringProperty("label");

        markRemovedOfferIds = new IntHashSet();

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
        TimeTableImproved timeTable = getTimeTable(stationId);
        int vertexId = timeTable.getTimeVertex(time, offer, nodeOfferIdProperty);
        if (vertexId != -1) return vertexId;

        vertexId = this.addVertex();
        nodeTimeProperty.setValue(vertexId, time);
        nodeStationIdProperty.setValue(vertexId, stationId);
        nodeOfferIdProperty.setValue(vertexId, offer.getId());
        nodeLabelProperty.setValue(vertexId, time + "@" + vertexId + "_" + stationGraph.getLabel(stationId));

        timeTable.addTimeVertex(this, vertexId, nodeTimeProperty, offer);
        return vertexId;
    }

    public void assignParcel(Parcel parcel, DriverConfig driverConfig){
        TimeTableImproved startTimeTable = getTimeTable(parcel.getStartStationId());
        //TODO change it to consider markedRemoveOffers
        int startVertexId = startTimeTable.findFirstTimeVertex(parcel.getEarliestDepartureTime());

        TimeTableImproved endTimeTable = getTimeTable(parcel.getEndStationId());
        //TODO change it to consider markedRemoveOffers
        int endVertexId = endTimeTable.findLastTimeVertex(parcel.getLatestArrivalTime());
        if(startVertexId != -1 && endVertexId != -1){
            //TODO change endVertexId to endStationId, so that we can find the earliest vertices in the endStation
            Path path = getShortestPath(startVertexId, endVertexId, driverConfig, parcel.getVolume());
            if(path != null){
                parcel.setPath(path);
                updateOffers(path, driverConfig, parcel);
            }
        }
    }

    public Path getShortestPath(int source, int destination, DriverConfig driverConfig, int volume) {
        return new SearchResultWrappedPath(new DijkstraEnhancedAlgorithm(
                getWeightProperty(), nodeOfferIdProperty, markRemovedOfferIds)
                .compute(this, source, driverConfig, volume, DIRECTION.out, null), source, destination);
    }

    /**
     * Update offers on the assigned Path by marking them as removed,
     * and the drivers of the updated offers add some new offers
     *  @param path the assigned path
     * @param driverConfig
     * @param parcel
     */
    private void updateOffers(Path path, DriverConfig driverConfig, Parcel parcel) {

        // update capacities for the offers in the assigned path
        int preVertexId = -1;
        int startVertexId = -1;
        int endVertexId = -1;
        int prevOfferId = -1;
        int newOfferId = -1;
        for (int i = 0; i < path.getNumberOfVertices(); i++) {
            int currentVertex = path.getVertexAt(i);
            int currentOfferId = nodeOfferIdProperty.getValueAsInt(currentVertex);

            //a new offer hop
            if(currentOfferId != prevOfferId){
                if (newOfferId != -1) {
                    Offer prevOffer = driverConfig.getOfferById(prevOfferId);
                    markOfferRemoved(prevOffer);
                    int prevCapacity = prevOffer.getCapacity() - parcel.getVolume();
                    endVertexId = preVertexId;
                    driverConfig.addOffer(
                            new Offer(newOfferId,
                                    nodeStationIdProperty.getValueAsInt(startVertexId),
                                    nodeStationIdProperty.getValueAsInt(endVertexId),
                                    nodeTimeProperty.getValueAsInt(startVertexId),
                                    prevCapacity, prevOffer.getDriver()));
                }
                newOfferId = driverConfig.getNextOfferId();
                startVertexId = currentVertex;
            }

            nodeOfferIdProperty.setValue(currentVertex, newOfferId);
            preVertexId = currentVertex;
            prevOfferId = currentOfferId;
        }
    }

    private void markOfferRemoved(Offer offer) {
        markRemovedOfferIds.add(offer.getId());
    }

    public void removeVertex(int vertexId){
        super.removeVertex(vertexId);
        TimeTableImproved timeTable = getTimeTable(nodeStationIdProperty.getValueAsInt(vertexId));
        timeTable.removeTimeVertex(vertexId, nodeTimeProperty);
    }
    private TimeTableImproved getTimeTable(int stationId){
        TimeTableImproved timeTable = stationTimeTableMap.get(stationId);
        if (timeTable == null) {
            timeTable = new TimeTableImproved();
            stationTimeTableMap.put(stationId, timeTable);
        }
        return timeTable;
    }

}
