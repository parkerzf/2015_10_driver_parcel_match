package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.properties.NumericalProperty;
import nl.twente.bms.model.elem.Offer;

import java.util.TreeMap;

/**
 * The class to store the time table for one station
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class TimeTableImproved {
    // timeInMins to A list of time vertices map
    private TreeMap<Integer, IntArrayList> timeSlots;
    public TimeTableImproved() {
        timeSlots = new TreeMap<>();
    }

    /**
     * Check the time vertex presence in the associated time slot
     *
     * @param time             the time at this station
     * @param offer           the current driver's offer
     * @param offerIdProperty the vertex offerIdProperty in time expanded graph
     * @return the time vertex if exist, otherwise -1
     */
    public int getTimeVertex(int time, Offer offer, NumericalProperty offerIdProperty) {
        IntArrayList timeSlot = timeSlots.get(time);
        if (timeSlot == null) return -1;
        // We just need to compare to the previous time vertex in this time slot,
        // because we don't add a new time vertex if there is one from the same offer, in the same time slot.
        int lastVertexId = timeSlot.get(timeSlot.size() - 1);
        if (offerIdProperty.getValueAsInt(lastVertexId) == offer.getId()) {
            return lastVertexId;
        }
        return -1;
    }

    /**
     * Add time vertex to the time table of the station
     *
     * @param graph        the time expanded graph
     * @param vertex       the time vertex id in the time expanded graph
     * @param timeProperty the vertex timeProperty
     * @param offer       the driver's associated with the vertex
     */
    public void addTimeVertex(TimeExpandedGraph graph, int vertex, NumericalProperty timeProperty, Offer offer) {
        int vertexTime = timeProperty.getValueAsInt(vertex);
        Integer nearestPastTimeKey = timeSlots.floorKey(vertexTime);
        Integer nearestFutureTimeKey = timeSlots.ceilingKey(vertexTime + 1);

        int nearestPastVertex = -1;
        int nearestPastDelay = offer.getHoldDuration() + 1;
        if(nearestPastTimeKey != null){
            nearestPastDelay = vertexTime - nearestPastTimeKey;
            if(nearestPastDelay <= offer.getHoldDuration()){
                IntArrayList nearestPastTimeSlot = timeSlots.get(nearestPastTimeKey);
                nearestPastVertex = nearestPastTimeSlot.get(nearestPastTimeSlot.size() - 1);
                int e = graph.addDirectedSimpleEdge(nearestPastVertex, vertex);
                graph.setEdgeWeight(e, nearestPastDelay);
            }
        }

        int nearestFutureVertex = -1;
        int nearestFutureDelay = offer.getHoldDuration() + 1;
        if(nearestFutureTimeKey != null) {
            nearestFutureDelay = nearestFutureTimeKey - vertexTime;
            if (nearestFutureDelay <= offer.getHoldDuration()) {
                IntArrayList nearestFutureTimeSlot = timeSlots.get(nearestFutureTimeKey);
                nearestFutureVertex = nearestFutureTimeSlot.get(0);
                int e = graph.addDirectedSimpleEdge(vertex, nearestFutureVertex);
                graph.setEdgeWeight(e, nearestFutureDelay);
            }
        }

        IntArrayList timeSlot = timeSlots.get(vertexTime);
        if (timeSlot == null) {
            timeSlot = new IntArrayList();
            timeSlots.put(vertexTime, timeSlot);
        }
        timeSlot.add(vertex);

        // remove the previous connection between nearestPastVertex and nearestFutureVertex
        if (nearestPastDelay + nearestFutureDelay <= offer.getHoldDuration()) {
            for (IntCursor edgeCursor : graph.getEdgesConnecting(nearestPastVertex, nearestFutureVertex)) {
                int edge = edgeCursor.value;
                graph.removeEdge(edge);
            }
        }
    }

    public void removeTimeVertex(int vertexId, NumericalProperty nodeTimeProperty) {
        int time = nodeTimeProperty.getValueAsInt(vertexId);
        IntArrayList timeSlot = timeSlots.get(time);
        if(timeSlot != null){
            timeSlot.removeFirstOccurrence(vertexId);
        }
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
}
