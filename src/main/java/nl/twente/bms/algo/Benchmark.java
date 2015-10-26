package nl.twente.bms.algo;

import grph.path.Path;
import nl.twente.bms.algo.struct.WeightedGrph;
import nl.twente.bms.algo.struct.WeightedSmartPath;

import java.util.Collection;

/**
 * Created by zhaofeng on 10/21/15.
 */
public class Benchmark {
    public static void main(String args[]){
        WeightedGrph g = new WeightedGrph();
        g.addVertex(1);
        g.addVertex(2);
        g.addVertex(3);
        g.addVertex(4);
        g.addVertex(5);
        g.addVertex(6);
        g.addVertex(7);
        g.addUndirectedSimpleEdge(1, 2);
        g.addUndirectedSimpleEdge(1, 3);
        g.addUndirectedSimpleEdge(2, 4);
        g.addUndirectedSimpleEdge(3, 4);
        g.addUndirectedSimpleEdge(4, 5);
        g.addUndirectedSimpleEdge(4, 6);
        g.addUndirectedSimpleEdge(4, 7);
        g.addUndirectedSimpleEdge(5, 7);
        g.addUndirectedSimpleEdge(6, 7);


        Path p = g.getShortestPath(1, 7);
        p.setColor(g, 6);

        Collection<WeightedSmartPath> paths = g.getMaxDetourPaths(1, 7, 0.5);

        System.out.println("max detour paths: " + paths);

        g.display();
    }

}
