package nl.twente.bms.algo.struct;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;
import grph.properties.NumericalProperty;
import nl.twente.bms.algo.MaxDetourPaths;

import java.util.Collection;

/**
 * Created by zhaofeng on 10/23/15.
 */
public class WeightedGrph extends InMemoryGrph {
    private final NumericalProperty weightProperty;
    private final Table<Integer, Integer, Integer> directDistanceTable;

    public WeightedGrph(){
        weightProperty = new NumericalProperty ("weight", 16, 65535);
        directDistanceTable = HashBasedTable.create();
    }

    public int getEdgeWeight(int e)
    {
        assert getEdges().contains(e);
        return weightProperty.getValueAsInt(e);
    }
    public void setEdgeWeight(int e , int newWeight)
    {
        assert getEdges().contains(e);
        weightProperty.setValue(e, newWeight);
    }

    public int getDirectDistance(int u, int v)
    {
        assert getVertices().contains(u) : "vertex does not exist: " + u;
        assert getVertices().contains(v) : "vertex does not exist: " + v;

        return directDistanceTable.get(u, v);
    }

    public void setDirectDistance(int u, int v, int distance){
        assert getVertices().contains(u) : "vertex does not exist: " + u;
        assert getVertices().contains(v) : "vertex does not exist: " + v;

        directDistanceTable.put(u, v, distance);
        directDistanceTable.put(v, u, distance);
    }

    public Path getShortestPath(int source, int destination){
        return getShortestPath(source, destination, weightProperty);
    }

    public Collection<WeightedSmartPath> getMaxDetourPaths(int source, int destination, double maxDetour){
        Path shortestPath = getShortestPath(source, destination);
        int shortestWeight = 0;
        for (int i = 1; i < shortestPath.getNumberOfVertices(); i++)
        {
            int v1 = shortestPath.getVertexAt(i - 1);
            int v2 = shortestPath.getVertexAt(i);
            int edge = getEdgesConnecting(v1, v2).toIntArray()[0];
            shortestWeight += getEdgeWeight(edge);
        }

        double maxDetourWeight = shortestWeight * (1+ maxDetour);

        return (new MaxDetourPaths(weightProperty, directDistanceTable)).
                compute(this, source, destination, maxDetourWeight);
    }




}
