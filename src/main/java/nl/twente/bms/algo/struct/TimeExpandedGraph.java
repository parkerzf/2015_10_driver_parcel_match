package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.*;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.path.Path;
import grph.properties.NumericalProperty;
import grph.properties.StringProperty;
import nl.twente.bms.algo.DijkstraTimeExpandedAlgorithm;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.elem.Offer;
import nl.twente.bms.model.elem.Parcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toools.set.IntHashSet;
import toools.set.IntSet;


/**
 * The class to store the time expanded graph with respect ot the station graph
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */

public class TimeExpandedGraph extends StationGraph {
    private static final Logger logger = LoggerFactory.getLogger(DriverConfig.class);
    private final NumericalProperty nodeTimeProperty;
    private final NumericalProperty nodeStationIdProperty;
    private final NumericalProperty nodeOfferIdProperty;
    private final StringProperty nodeLabelProperty;

    private final StationGraph stationGraph;
    private final DriverConfig driverConfig;
    // stationId --> timeTable
    private IntObjectMap<TimeTable> stationTimeTableMap;

    private IntSet markRemovedOfferIds;

    public TimeExpandedGraph(StationGraph stationGraph, DriverConfig driverConfig) {
        nodeTimeProperty = new NumericalProperty("time", 11, 1441);
        nodeStationIdProperty = new NumericalProperty("station", 16, 65535);
        nodeOfferIdProperty = new NumericalProperty("offer", 16, 65535);
        nodeLabelProperty = new StringProperty("label");

        markRemovedOfferIds = new IntHashSet();
        stationTimeTableMap = new IntObjectOpenHashMap<>(stationGraph.getNumberOfVertices());

        this.stationGraph = stationGraph;
        this.driverConfig = driverConfig;

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
        TimeTable timeTable = getTimeTable(stationId);
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

    public void assignParcel(Parcel parcel){
        TimeTable startTimeTable = getTimeTable(parcel.getStartStationId());

        int startVertexId = -1;
        do{
            if(startVertexId != -1) this.removeVertex(startVertexId);
            startVertexId = startTimeTable.findFirstTimeVertex(parcel.getEarliestDepartureTime());
        }
        while(startVertexId == -1 || isMarkedRemoved(startVertexId));

        TimeTable endTimeTable = getTimeTable(parcel.getEndStationId());

//        int endVertexId = -1;
//        do{
//            if(endVertexId != -1) this.removeVertex(endVertexId);
//            endVertexId = endTimeTable.findLastTimeVertex(parcel.getMaxDuration());
//        }
//        while(endVertexId == -1 || isMarkedRemoved(endVertexId));

//        if(startVertexId != -1 && endVertexId != -1){
        if(startVertexId != -1){
            Path path = getShortestPath(startVertexId, parcel.getEndStationId(), parcel.getVolume());
            if(path != null){
                parcel.setPath(path);
                updateOffers(path, parcel);
            }
        }
    }

    public Path getShortestPath(int source, int destinationStationId, int volume) {
        return new DijkstraTimeExpandedAlgorithm(this)
                .compute(source, destinationStationId, volume, DIRECTION.out, null);
    }

    /**
     * Update offers on the assigned Path by marking them as removed,
     * and the drivers of the updated offers add some new offers
     *
     * @param path the assigned path
     * @param parcel
     */
    private void updateOffers(Path path, Parcel parcel) {
        // update capacities for the offers in the assigned path
        int prevOfferId = - 1;
        int prevVertexId = -1;
        Offer updatedOffer = null;
        for (int i = 0; i < path.getNumberOfVertices(); i++) {
            int currentVertexId = path.getVertexAt(i);
            int currentOfferId = nodeOfferIdProperty.getValueAsInt(currentVertexId);
            //a new offer hop
            if(currentOfferId != prevOfferId){
                addNewOffer(prevOfferId, prevVertexId);
                updatedOffer = updateCurrentOffer(currentOfferId, parcel);
            }
            prevOfferId = currentOfferId;
            prevVertexId = currentVertexId;
            if(updatedOffer.isFeasible()) {
                nodeOfferIdProperty.setValue(currentVertexId, updatedOffer.getId());
            }
        }
        //add new offer for the last offer
        addNewOffer(prevOfferId, prevVertexId);
    }

    private Offer updateCurrentOffer(int currentOfferId, Parcel parcel){
        // mark current offer as removed
        Offer curOffer = driverConfig.getOfferById(currentOfferId);
        curOffer.addParcel(parcel);
        markOfferRemoved(curOffer);
        // update a new offer on top of current offer
        int updatedOfferId = driverConfig.getNextOfferId();
        Offer updatedOffer = new Offer(updatedOfferId, curOffer, parcel.getVolume());
        if(updatedOffer.isFeasible()){
            driverConfig.addOffer(updatedOffer);
            System.out.println("Add updated offer: " + updatedOffer);
            logger.debug("Add updated offer: " + updatedOffer);
        }
        else{
            System.out.println("Updated offer is not feasible: " + updatedOffer);
            logger.debug("Updated offer is not feasible: " + updatedOffer);
        }
        return updatedOffer;
    }

    // add a new offer starting from the end point of the prev offer
    private void addNewOffer(int prevOfferId, int startVertexId){
        Offer prevOffer = driverConfig.getOfferById(prevOfferId);
        if(prevOffer != null && prevOffer.isExtendableOffer()) {
            int nextOfferId = driverConfig.getNextOfferId();
            Offer newOffer = new Offer(nextOfferId, prevOffer,
                    nodeStationIdProperty.getValueAsInt(startVertexId),
                    nodeTimeProperty.getValueAsInt(startVertexId));

            if(newOffer.isFeasible()){
                driverConfig.addOffer(newOffer);
                addOffer(newOffer);
                System.out.println("Add new offer: " + newOffer);
                logger.debug("Add new offer: " + newOffer);
            }
            else{
                System.out.println("New offer is not feasible: " + newOffer);
                logger.debug("New offer is not feasible: " + newOffer);
            }
        }
    }

    private void markOfferRemoved(Offer offer) {
        markRemovedOfferIds.add(offer.getId());
    }

    public boolean isMarkedRemoved(int vertexId) {
        int offerId = nodeOfferIdProperty.getValueAsInt(vertexId);
        if(markRemovedOfferIds.contains(offerId)) return true;
        return false;
    }

    public boolean hasCapacity(int vertexId, int volume) {
        if(volume <= 0) return true;
        Offer offer = driverConfig.getOfferById(nodeOfferIdProperty.getValueAsInt(vertexId));
        return offer.getCapacity() >= volume;
    }

    public void removeVertex(int vertexId) {
        System.out.println("Remove vertex: " + getLabel(vertexId));
        super.removeVertex(vertexId);
        TimeTable timeTable = getTimeTable(nodeStationIdProperty.getValueAsInt(vertexId));
        timeTable.removeTimeVertex(vertexId, nodeTimeProperty);
    }

    public int getStationIdFromVertexId(int vertexId){
        return nodeStationIdProperty.getValueAsInt(vertexId);
    }

    private TimeTable getTimeTable(int stationId){
        TimeTable timeTable = stationTimeTableMap.get(stationId);
        if (timeTable == null) {
            timeTable = new TimeTable();
            stationTimeTableMap.put(stationId, timeTable);
        }
        return timeTable;
    }
}
