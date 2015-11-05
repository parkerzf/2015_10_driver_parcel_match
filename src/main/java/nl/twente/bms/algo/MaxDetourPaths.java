package nl.twente.bms.algo;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.google.common.collect.Table;
import grph.Grph;
import grph.GrphAlgorithm;
import grph.path.Path;
import grph.properties.NumericalProperty;
import nl.twente.bms.algo.struct.WeightedSmartPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by zhaofeng on 10/24/15.
 */
public class MaxDetourPaths extends GrphAlgorithm<Collection<Path>> {
    private final NumericalProperty weightProperty;
    private final Table<Integer, Integer, Integer> directDistanceTable;

    public MaxDetourPaths(NumericalProperty weightProperty, Table<Integer, Integer, Integer> directDistanceTable) {
        this.weightProperty = weightProperty;
        this.directDistanceTable = directDistanceTable;
    }

    @Override
    public Collection<Path> compute(Grph grph) {
        throw new IllegalStateException("unsupported");
    }

    public Collection<WeightedSmartPath> compute(Grph grph, int source, int destination, double maxWeight) {
        assert maxWeight >= 0 : "maxLen: " + maxWeight + " is less than 0";

        ArrayList<WeightedSmartPath> pathList = new ArrayList<WeightedSmartPath>();
        WeightedSmartPath currentPath = new WeightedSmartPath();
        currentPath.setSource(source);

        DFS(grph, source, destination, maxWeight, currentPath, pathList);

        return pathList;
    }


    private void DFS(Grph grph, int currentVertex, int destination, double remainingWeight,
                     WeightedSmartPath currentPath, ArrayList<WeightedSmartPath> pathList) {

        if (currentVertex == destination) {
            WeightedSmartPath clonedPath = (WeightedSmartPath) currentPath.clone();
            pathList.add(clonedPath);
        } else {
            Iterator<IntCursor> iter = grph.getOutEdges(currentVertex).iterator();
            while (iter.hasNext()) {
                int edge = iter.next().value;
                int edgeWeight = weightProperty.getValueAsInt(edge);
                int neighbor = grph.getTheOtherVertex(edge, currentVertex);

                boolean noDirectDistance = directDistanceTable.get(neighbor, destination) == null;

                double newRemainingWeight = remainingWeight - edgeWeight;
                if (newRemainingWeight >= 0
                        && (noDirectDistance || directDistanceTable.get(neighbor, destination) <= newRemainingWeight)
                        && !currentPath.containsVertex(neighbor)) {
                    currentPath.extend(edge, neighbor, edgeWeight);
                    DFS(grph, neighbor, destination, newRemainingWeight, currentPath, pathList);
                    currentPath.removeLast();
                }
            }
        }
    }

}
