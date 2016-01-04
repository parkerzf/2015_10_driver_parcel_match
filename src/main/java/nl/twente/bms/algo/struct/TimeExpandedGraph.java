package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
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
    private static final Logger logger = LoggerFactory.getLogger(TimeExpandedGraph.class);
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

        //add sourceTimeVertex and targetTimeVertex to offer
        offer.setSourceTimeVertex(sourceTimeVertex);
        offer.setTargetTimeVertex(targetTimeVertex);
        offer.setEarliestArrivalTime(targetTime);

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
     * @param offer     the driver'offer on station graph
     * @return the time vertex id
     */
    private int addTimeVertex(int time, int stationId, Offer offer) {
        TimeTable timeTable = getOrCreateTimeTable(stationId);
        int vertexId = timeTable.getTimeVertex(time, offer.getId(), nodeOfferIdProperty);
        if(vertexId != -1) return vertexId;
        vertexId = this.addVertex();
        nodeTimeProperty.setValue(vertexId, time);
        nodeStationIdProperty.setValue(vertexId, stationId);
        nodeOfferIdProperty.setValue(vertexId, offer.getId());
        nodeLabelProperty.setValue(vertexId, getVertexString(vertexId));

        timeTable.addTimeVertex(this, vertexId, nodeTimeProperty, offer);
        return vertexId;
    }

    private int getTimeVertex(int time, int stationId, int offerId){
        TimeTable timeTable = getOrCreateTimeTable(stationId);
        int vertexId = timeTable.getTimeVertex(time, offerId, nodeOfferIdProperty);
        return vertexId;
    }

    public void assignParcel(Parcel parcel) {
        if(parcel.getId() == 18){
            System.out.println("debug");
        }
        TimeTable startTimeTable = stationTimeTableMap.get(parcel.getStartStationId());
        if (startTimeTable == null) return;

        //find the first not marking removed start vertex
        int startVertexId = getNextVertexId(startTimeTable, parcel.getEarliestDepartureTime(), -1, parcel.getVolume());

        Path path = null;
        while (startVertexId != -1 && path == null) {
            path = getShortestPath(startVertexId, parcel.getEndStationId(), parcel.getVolume());
            if (path != null && path.getLength() != 0) {
                logger.info("Assign " + parcel + " to path " + getPathString(path));
                parcel.setPath(path);
                path.setColor(this, 6);
                int numOffers = updateOffers(path, parcel);
                parcel.setNumOffers(numOffers);
            } else {
                startVertexId = getNextVertexId(startTimeTable, nodeTimeProperty.getValueAsInt(startVertexId), startVertexId, parcel.getVolume());
            }
        }
    }

    //TODO find first outgoing vertex
    private int getNextVertexId(TimeTable timetable, int startTime, int prevTimeVertex, int volume) {
        int startVertexId;
        if(prevTimeVertex == -1){
            startVertexId = timetable.findFirstTimeVertex(startTime);
        }
        else{
            startVertexId = timetable.findNextTimeVertex(startTime, prevTimeVertex);
        }

        while (startVertexId != -1) {
            if(isMarkedRemoved(startVertexId)){
                this.removeVertex(startVertexId);
                startVertexId = timetable.findFirstTimeVertex(startTime);
            }
            else if(!hasCapacity(startVertexId, volume)){
                startVertexId = timetable.findNextTimeVertex(nodeTimeProperty.getValueAsInt(startVertexId), startVertexId);
            }
            else{
                break;
            }
        }

        return startVertexId;
    }

    public Path getShortestPath(int source, int destinationStationId, int volume) {
        return new DijkstraTimeExpandedAlgorithm(this)
                .compute(source, destinationStationId, volume, DIRECTION.out, null);
    }

    /**
     * Update offers on the assigned Path by marking them as removed,
     * and the drivers of the updated offers add some new offers
     *
     * @param path   the assigned path
     * @param parcel
     */
    private int updateOffers(Path path, Parcel parcel) {
        // update capacities for the offers in the assigned path
        int numOffers = 0;
        int prevVertexId = -1;
        int prevStartVertexId = -1;
        int prevOfferId = -1;
        for (int i = 0; i < path.getNumberOfVertices(); i++) {
            int currentVertexId = path.getVertexAt(i);
            int currentOfferId = nodeOfferIdProperty.getValueAsInt(currentVertexId);
            //a new offer hop
            if (currentOfferId != prevOfferId) {
                if(prevStartVertexId != -1 && prevVertexId != -1 && prevOfferId != -1){
                    numOffers++;
                    // prev offer is from prevStartVertexId to prevVertexId
                    updatePrevOffer(prevOfferId, prevStartVertexId, prevVertexId, parcel);
                    addNewOffer(prevOfferId, prevVertexId);
                }
                prevStartVertexId = currentVertexId;
            }
            prevVertexId = currentVertexId;
            prevOfferId = currentOfferId;
        }
        // the last offer
        if(prevStartVertexId != -1 && prevVertexId != -1 && prevOfferId != -1){
            numOffers++;
            updatePrevOffer(prevOfferId, prevStartVertexId, prevVertexId, parcel);
            addNewOffer(prevOfferId, prevVertexId);
        }

        return numOffers;
    }

    private void setTimeVerticesToUpdatedOffer(Offer updatedOffer, int prevOfferId, int prevStartVertexId) {
        int s = updatedOffer.getSource();
        int sTime =  updatedOffer.getDepartureTime();
        int sTimeVertexId = updatedOffer.getSourceTimeVertex();
        int sPrime = nodeStationIdProperty.getValueAsInt(prevStartVertexId);
        int sPrimeTime = nodeTimeProperty.getValueAsInt(prevStartVertexId);
        int t = updatedOffer.getTarget();

        if(s == sPrime){
            updatePathToOffer(s, sTimeVertexId, sTime, t, updatedOffer, prevOfferId);
        }
        else {
            updatePathToOffer(s, sTimeVertexId, sTime, sPrime, updatedOffer, prevOfferId);
            updatePathToOffer(sPrime, prevStartVertexId, sPrimeTime, t, updatedOffer, prevOfferId);
        }
    }

    private void updatePathToOffer(int s, int timeVertexId, int time, int t, Offer offer, int prevOfferId){
        Path pathToUpdate = stationGraph.getShortestPath(s, t);
        updateVertexOfferId(timeVertexId, offer.getId());

        logger.info("Path to update: " + pathToUpdate);
        logger.info("Set v" + timeVertexId + " to o" + offer.getId() + "@" + time);

        for (int i = 1; i < pathToUpdate.getNumberOfVertices(); i++) {
            int preV = pathToUpdate.getVertexAt(i-1);
            int v = pathToUpdate.getVertexAt(i);
            time += offer.getDuration(stationGraph.getDirectDistance(preV, v));
            int timeV = getTimeVertex(time, v, prevOfferId);
            logger.info("Set v" + timeV + " to o" + offer.getId()+ "@" + time);
            if(timeV != -1){
                updateVertexOfferId(timeV, offer.getId());
            }
        }
    }

    private void updatePrevOffer(int prevOfferId, int prevStartVertexId, int prevVertexId, Parcel parcel) {
        Offer prevOffer = driverConfig.getOfferById(prevOfferId);
        prevOffer.addParcel(parcel);
        if(!prevOffer.isUpdatedOffer()){
            markOfferRemoved(prevOffer);
            // create a updated offer on top of prev offer
            int updatedOfferId = driverConfig.getNextOfferId();
            Offer updatedOffer = new Offer(updatedOfferId, prevOffer, parcel.getVolume(),
                    prevVertexId,
                    nodeStationIdProperty.getValueAsInt(prevVertexId),
                    nodeTimeProperty.getValueAsInt(prevVertexId));

            if (updatedOffer.isFeasible()) {
                logger.info("Add updated offer: " + updatedOffer);
                driverConfig.addOffer(updatedOffer);
                setTimeVerticesToUpdatedOffer(updatedOffer, prevOfferId, prevStartVertexId);
            } else {
                logger.warn("Updated offer is not feasible: " + updatedOffer);
            }
        }
        else{
            int remainingCapacity = prevOffer.getCapacity() - parcel.getVolume();
            prevOffer.setCapacity(remainingCapacity);
            if(remainingCapacity > 0){
                logger.info("Update prev offer: " + prevOffer);
            } else {
                logger.warn("Updated prev offer is not feasible: " + prevOffer);
                markOfferRemoved(prevOffer);
            }
        }
    }

    // add a new offer starting from the end point of the prev offer
    private void addNewOffer(int prevOfferId, int startVertexId) {
        Offer prevOffer = driverConfig.getOfferById(prevOfferId);
        if (!prevOffer.isUpdatedOffer()) {
            int nextOfferId = driverConfig.getNextOfferId();
            Offer newOffer = new Offer(nextOfferId, prevOffer, startVertexId,
                    nodeStationIdProperty.getValueAsInt(startVertexId),
                    nodeTimeProperty.getValueAsInt(startVertexId));

            if (newOffer.isFeasible()) {
                driverConfig.addOffer(newOffer);
                addOffer(newOffer);
                logger.info("Add new offer: " + newOffer);
            } else {
                logger.warn("New offer is not feasible: " + newOffer);
            }
        }
        else {
            logger.warn("Updated offer is not extendable: " + prevOffer);
        }
    }

    private void markOfferRemoved(Offer offer) {
        markRemovedOfferIds.add(offer.getId());
    }

    public boolean isMarkedRemoved(int vertexId) {
        int offerId = nodeOfferIdProperty.getValueAsInt(vertexId);
        if (markRemovedOfferIds.contains(offerId)) return true;
        return false;
    }

    public boolean hasCapacity(int vertexId, int volume) {
        if (volume <= 0) return true;
        Offer offer = driverConfig.getOfferById(nodeOfferIdProperty.getValueAsInt(vertexId));
        return offer.getCapacity() >= volume;
    }
    public void updateVertexOfferId(int vertexId, int offerId) {
        logger.debug("Update vertex: " + getLabel(vertexId));
        int prevOfferId = nodeOfferIdProperty.getValueAsInt(vertexId);
        nodeOfferIdProperty.setValue(vertexId, offerId);

        TimeTable timeTable = stationTimeTableMap.get(nodeStationIdProperty.getValueAsInt(vertexId));
        int time = nodeTimeProperty.getValueAsInt(vertexId);
        timeTable.updateTimeVertexOfferId(vertexId, time, prevOfferId, offerId);
    }


    public void removeVertex(int vertexId) {
        logger.debug("Remove vertex: " + getLabel(vertexId));
        super.removeVertex(vertexId);
        TimeTable timeTable = stationTimeTableMap.get(nodeStationIdProperty.getValueAsInt(vertexId));
        int time = nodeTimeProperty.getValueAsInt(vertexId);
        int offerId = nodeOfferIdProperty.getValueAsInt(vertexId);
        timeTable.removeTimeVertex(vertexId, time, offerId);
    }

    public int getStationIdFromVertexId(int vertexId) {
        return nodeStationIdProperty.getValueAsInt(vertexId);
    }


    private TimeTable getOrCreateTimeTable(int stationId) {
        TimeTable timeTable = stationTimeTableMap.get(stationId);
        if (timeTable == null) {
            timeTable = new TimeTable();
            stationTimeTableMap.put(stationId, timeTable);
        }
        return timeTable;
    }

    private String getPathString(Path path) {
        int n = path.getNumberOfVertices();
        if (n == 0) {
            return "[path does not exist]";
        }
        else {
            StringBuilder b = new StringBuilder();

            for (int i = 0; i < n; i++) {
                int v = path.getVertexAt(i);
                b.append('[');
                b.append(getVertexString(v));
                b.append(']');
                if (i < n - 1) {
                    b.append("->");
                }
            }
            return b.toString();
        }
    }

    private String getVertexString(int timeVertexId){
        int time = nodeTimeProperty.getValueAsInt(timeVertexId);
        int stationId = nodeStationIdProperty.getValueAsInt(timeVertexId);
        int offerId = nodeOfferIdProperty.getValueAsInt(timeVertexId);
        return String.format("v%d_s%d_o%d@%d", timeVertexId, stationId, offerId, time);
    }
}
