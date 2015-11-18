package nl.twente.bms.algo.struct;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import grph.algo.distance.DistanceMatrix;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;
import grph.path.SearchResultWrappedPath;
import grph.properties.NumericalProperty;
import nl.twente.bms.algo.DijkstraEnhancedAlgorithm;
import nl.twente.bms.algo.MaxDetourPaths;
import nl.twente.bms.model.elem.Offer;


import java.util.Collection;

/**
 * The class to store the station distance weighted graph
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class StationGraph extends InMemoryGrph {
    private final NumericalProperty weightProperty;
    private final Table<Integer, Integer, Integer> directDistanceTable;

    private DistanceMatrix shortestDistances;

    public StationGraph() {
        weightProperty = new NumericalProperty("weight", 16, 65535);
        directDistanceTable = HashBasedTable.create();
    }

    public void computeAllSourceShortestDistances(){
        shortestDistances = new DijkstraEnhancedAlgorithm(getWeightProperty()).computeDistanceMatrix(this);
    }

    public NumericalProperty getWeightProperty() {
        return weightProperty;
    }

    public int getEdgeWeight(int e) {
        assert getEdges().contains(e);
        return weightProperty.getValueAsInt(e);
    }

    public void setEdgeWeight(int e, int newWeight) {
        assert getEdges().contains(e);
        weightProperty.setValue(e, newWeight);
    }

    public int getDirectDistance(int u, int v) {
        assert getVertices().contains(u) : "vertex does not exist: " + u;
        assert getVertices().contains(v) : "vertex does not exist: " + v;

        return directDistanceTable.get(u, v);
    }

    public void setDirectDistance(int u, int v, int distance) {
        assert getVertices().contains(u) : "vertex does not exist: " + u;
        assert getVertices().contains(v) : "vertex does not exist: " + v;

        directDistanceTable.put(u, v, distance);
        directDistanceTable.put(v, u, distance);
    }

    /**
     * Get the shortest distance from source to destination
     *
     * @param source
     * @param destination
     * @return shortest distance
     */
    public int getShortestDistance(int source, int destination) {
        if(shortestDistances != null){
            return (int) shortestDistances.getDistance(destination, source);
        }
        else{
            return getDistance(getShortestPath(source, destination));
        }
    }


    public Path getShortestPath(int source, int destination) {
        return new SearchResultWrappedPath(new DijkstraEnhancedAlgorithm(getWeightProperty())
                .compute(this, source), source, destination);
    }

    /**
     * Get the path distance by adding the edge weight
     *
     * @param path the path on the station graph
     * @return the distance of the path
     */
    private int getDistance(Path path) {
        int distance = 0;
        for (int i = 1; i < path.getNumberOfVertices(); i++) {
            int v1 = path.getVertexAt(i - 1);
            int v2 = path.getVertexAt(i);
            int edge = getEdgesConnecting(v1, v2).toIntArray()[0];
            distance += getEdgeWeight(edge);
        }
        return distance;
    }

    public Collection<WeightedSmartPath> getMaxDetourPaths(int source, int destination, double maxDetour) {
        int shortestWeight = getShortestDistance(source, destination);

        double maxDetourWeight = shortestWeight * (1 + maxDetour);

        return (new MaxDetourPaths(weightProperty, directDistanceTable)).
                compute(this, source, destination, maxDetourWeight);
    }

    /**
     * @param offer
     * @param detourVertex vertex in the detour
     *                     Check the detour and the delay is within the bound for path source -> detourVertex -> target
     * @return the feasible status of this detour path
     */
    public boolean isFeasible(Offer offer, int detourVertex) {
        int distanceSourceDetourVertex = getShortestDistance(offer.getSource(), detourVertex);
        int distanceDetourVertexTarget = getShortestDistance(detourVertex, offer.getTarget());
        int distanceSourceTarget = getShortestDistance(offer.getSource(), offer.getTarget());

        // detour feasibility
        if (distanceSourceDetourVertex + distanceDetourVertexTarget
                > (1 + offer.getEpsilon()) * distanceSourceTarget) return false;

        // delay feasibility
        return offer.getDuration(distanceSourceDetourVertex) + offer.getDuration(distanceDetourVertexTarget)
                + offer.getHoldDuration() <= (1 + offer.getGamma()) * offer.getDuration(distanceSourceTarget);
    }

    /**
     * @param offer
     * @param detourSource vertex in the detour
     * @param detourTarget vertex in the detour
     *                     Check the detour and the delay is within the bound for path source -> detourSource -> detourTarget -> target
     * @return the feasible status of this detour
     */
    public boolean isFeasible(Offer offer, int detourSource, int detourTarget) {
        int distanceSourceDetourSource = getShortestDistance(offer.getSource(), detourSource);
        int distanceDetourSourceDetourTarget = getShortestDistance(detourSource, detourTarget);
        int distanceDetourTargetTarget = getShortestDistance(detourTarget, offer.getTarget());
        int distanceSourceTarget = getShortestDistance(offer.getSource(), offer.getTarget());

        // detour feasibility
        if (distanceSourceDetourSource + distanceDetourSourceDetourTarget
                + distanceDetourTargetTarget > (1 + offer.getEpsilon()) * distanceSourceTarget)
            return false;

        // delay feasibility
        return offer.getDuration(distanceSourceDetourSource) + offer.getDuration(distanceDetourSourceDetourTarget)
                + offer.getDuration(distanceDetourTargetTarget) + offer.getHoldDuration()
                <= (1 + offer.getGamma()) * offer.getDuration(distanceSourceTarget);
    }

    /**
     * The driver's offer travel duration on the station graph
     *
     * @param offer the driver's offer to travel from source to target
     * @param source the source station vertex
     * @param target the target station vertex
     * @return
     */
    public int getDuration(Offer offer, int source, int target) {
        return offer.getDuration(getShortestDistance(source, target));
    }

    /**
     * Get the vertex label in station graph
     *
     * @param vertex the station vertex id
     * @return the vertex label
     */
    public String getLabel(int vertex) {
        return this.getVertexLabelProperty().getValueAsString(vertex);
    }
}
