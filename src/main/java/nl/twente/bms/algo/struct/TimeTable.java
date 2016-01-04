package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import grph.properties.NumericalProperty;
import nl.twente.bms.model.elem.Offer;

import java.util.TreeMap;

/**
 * The class to store the time table for one station
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class TimeTable {
    // timeInMins to A list of time vertices map
    private TreeMap<Integer, IntArrayList> timeSlots;
    private Table<Integer, Integer, Integer> timeOfferIdToTimeVertexIdTable;

    public TimeTable() {
        timeSlots = new TreeMap<>();
        timeOfferIdToTimeVertexIdTable = HashBasedTable.create();
    }

    /**
     * Check the time vertex presence in the associated time slot
     *
     * @param time             the time at this station
     * @param offerId           the current driver's offer Id
     * @param offerIdProperty the vertex offerIdProperty in time expanded graph
     * @return the time vertex if exist, otherwise -1
     */
    public int getTimeVertex(int time, int offerId, NumericalProperty offerIdProperty) {
        Integer tVertexId = timeOfferIdToTimeVertexIdTable.get(time, offerId);
        return tVertexId == null? -1 : tVertexId;
    }

    /**
     * Add time vertexId to the time table of the station
     *
     * @param graph        the time expanded graph
     * @param vertexId       the time vertexId id in the time expanded graph
     * @param timeProperty the vertexId timeProperty
     * @param offer       the driver's associated with the vertexId
     */
    public void addTimeVertex(TimeExpandedGraph graph, int vertexId, NumericalProperty timeProperty, Offer offer) {
        int vertexTime = timeProperty.getValueAsInt(vertexId);
        Integer nearestPastTimeKey = timeSlots.floorKey(vertexTime);
        Integer nearestFutureTimeKey = timeSlots.ceilingKey(vertexTime + 1);

        int nearestPastVertex = -1;
//        int nearestPastDelay = offer.getHoldDuration() + 1;
        if(nearestPastTimeKey != null){
                IntArrayList nearestPastTimeSlot = timeSlots.get(nearestPastTimeKey);
                nearestPastVertex = nearestPastTimeSlot.get(nearestPastTimeSlot.size() - 1);
                int e = graph.addDirectedSimpleEdge(nearestPastVertex, vertexId);
                graph.setEdgeWeight(e, vertexTime -  nearestPastTimeKey);
        }

        int nearestFutureVertex = -1;
//        int nearestFutureDelay = offer.getHoldDuration() + 1;
        if(nearestFutureTimeKey != null) {
                IntArrayList nearestFutureTimeSlot = timeSlots.get(nearestFutureTimeKey);
                nearestFutureVertex = nearestFutureTimeSlot.get(0);
                int e = graph.addDirectedSimpleEdge(vertexId, nearestFutureVertex);
                graph.setEdgeWeight(e, nearestFutureTimeKey - vertexTime);
        }

        IntArrayList timeSlot = timeSlots.get(vertexTime);
        if (timeSlot == null) {
            timeSlot = new IntArrayList();
            timeSlots.put(vertexTime, timeSlot);
        }
        timeSlot.add(vertexId);

        timeOfferIdToTimeVertexIdTable.put(vertexTime, offer.getId(), vertexId);

        // remove the previous connection between nearestPastVertex and nearestFutureVertex
        if (nearestPastVertex != -1 && nearestFutureVertex != -1) {
            for (IntCursor edgeCursor : graph.getEdgesConnecting(nearestPastVertex, nearestFutureVertex)) {
                int edge = edgeCursor.value;
                graph.removeEdge(edge);
            }
        }
    }

    public void removeTimeVertex(int vertexId, int time, int offerId) {
        IntArrayList timeSlot = timeSlots.get(time);
        if(timeSlot != null){
            timeSlot.removeFirstOccurrence(vertexId);
            if(timeSlot.isEmpty()) timeSlots.remove(time);
        }

        timeOfferIdToTimeVertexIdTable.remove(time, offerId);
    }

    public void updateTimeVertexOfferId(int vertexId, int time, int prevOfferId, int offerId) {
        timeOfferIdToTimeVertexIdTable.remove(time, prevOfferId);
        timeOfferIdToTimeVertexIdTable.put(time, offerId, vertexId);
    }

    public boolean isEmpty(){
        return timeSlots.isEmpty();
    }

    public int findFirstTimeVertex(int departureTime) {
        Integer nearestFutureTimeKey = timeSlots.ceilingKey(departureTime);
        if(nearestFutureTimeKey == null ) return -1;
        IntArrayList nearestFutureTimeSlot = timeSlots.get(nearestFutureTimeKey);
        return nearestFutureTimeSlot.get(0);
    }


    public int findLastTimeVertex(int arrivalTime) {
        Integer nearestPastTimeKey = timeSlots.floorKey(arrivalTime);
        if(nearestPastTimeKey == null ) return -1;
        IntArrayList nearestPastTimeSlot = timeSlots.get(nearestPastTimeKey);
        return nearestPastTimeSlot.get(nearestPastTimeSlot.size() - 1);
    }

    public int findNextTimeVertex(int departureTime, int startVertexId) {
        Integer nearestFutureTimeKey = timeSlots.ceilingKey(departureTime);
        if(nearestFutureTimeKey == null ) return -1;
        IntArrayList nearestFutureTimeSlot = timeSlots.get(nearestFutureTimeKey);
        int index = nearestFutureTimeSlot.indexOf(startVertexId);
        if(index == nearestFutureTimeSlot.size() -1){
            return findFirstTimeVertex(departureTime + 1);
        }
        else{
            return index == -1? -1 : nearestFutureTimeSlot.get(index+1);
        }
    }
}
