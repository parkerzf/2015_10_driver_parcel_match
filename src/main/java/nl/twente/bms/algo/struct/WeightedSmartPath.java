package nl.twente.bms.algo.struct;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import grph.path.AbstractPath;
import toools.collections.Collections;
import toools.set.DefaultIntSet;


/**
 * Created by zhaofeng on 10/24/15.
 */
public class WeightedSmartPath extends AbstractPath {
    private final IntArrayList vertexList = new IntArrayList();
    private final DefaultIntSet vertexSet = new DefaultIntSet();
    private final IntArrayList edgeList = new IntArrayList();
    private final IntArrayList edgeWeightList = new IntArrayList();

    public int getSource() {
        if(this.vertexList.isEmpty()) {
            throw new IllegalStateException("path is empty");
        } else {
            return this.vertexList.get(0);
        }
    }

    public void setSource(int v) {
        if(this.vertexList.isEmpty()) {
            this.vertexList.add(v);
        } else {
            this.vertexList.set(0, v);
        }
    }

    public int getDestination() {
        if(this.vertexList.isEmpty()) {
            throw new IllegalStateException("path is empty");
        } else {
            return this.vertexList.get(this.vertexList.size() - 1);
        }
    }

    public int getNumberOfVertices() {
        return this.vertexList.size();
    }

    public int getVertexAt(int i) {
        return this.vertexList.get(i);
    }

    public int indexOfVertex(int v) {
        return this.vertexList.indexOf(v);
    }

    public boolean containsVertex(int v) {
        return this.vertexSet.contains(v);
    }

    public int getEdgeHeadingToVertexAt(int i) {
        if(i == 0) {
            throw new IllegalArgumentException("the source of a path has no edge heading to it");
        } else {
            return this.edgeList.get(i - 1);
        }
    }

    public void extend(int thoughLink, int v) {
        extend(thoughLink, v, 1);
    }

    public void extend(int thoughLink, int v, int weight) {
        if(thoughLink >= 0 && this.getNumberOfVertices() == 0) {
            throw new IllegalStateException("a path cannot start with an edge");
        } else {
            vertexList.add(v);
            vertexSet.add(v);
            edgeList.add(thoughLink);
            edgeWeightList.add(weight);
        }
    }

    public void removeLast(){
        if(this.getNumberOfVertices() == 0) {
            throw new IllegalStateException("can not remove anything from an empty path");
        } else {
            int removedVertex = vertexList.remove(vertexList.size()-1);
            vertexSet.remove(removedVertex);
            if(getNumberOfVertices() != 0){
                edgeList.remove(edgeList.size()-1);
                edgeWeightList.remove(edgeWeightList.size() - 1);
            }
        }
    }

    @Override
    public AbstractPath clone() {
        WeightedSmartPath c = new WeightedSmartPath();
        c.setSource(getSource());

        for(int i = 0; i < edgeList.size(); ++i) {
            c.extend(edgeList.get(i), this.getVertexAt(i + 1), edgeWeightList.get(i));
        }
        return c;
    }

    public void reverse() {
        Collections.reverse(this.vertexList.buffer, 0, this.vertexList.size());
        Collections.reverse(this.edgeList.buffer, 0, this.edgeList.size());
        Collections.reverse(this.edgeWeightList.buffer, 0, this.edgeWeightList.size());
    }

    public int[] toVertexArray() {
        return this.vertexList.toArray();
    }

    public boolean equals(Object o) {
        return o instanceof WeightedSmartPath?this.equals((WeightedSmartPath)o):super.equals(o);
    }

    public boolean equals(WeightedSmartPath p) {
        return this.vertexList.equals(p.vertexList) && this.edgeList.equals(p.edgeList)
                &&this.vertexSet.equals(p.vertexSet) && this.edgeWeightList.equals(p.edgeWeightList);
    }

    public int getWeight(){
        int pathWeight = 0;
        for(IntCursor weight: edgeWeightList){
            pathWeight += weight.value;
        }
        return pathWeight;
    }

    public String toString(){
        int n = this.getNumberOfVertices();
        if(n == 0) {
            return "[path does not exist]";
        } else {
            StringBuilder b = new StringBuilder();

            for(int i = 0; i < n; ++i) {
                int v = this.getVertexAt(i);
                b.append("v" + v);
                if(i < n - 1) {
                    int e = this.getEdgeHeadingToVertexAt(i + 1);
                    if(e >= 0) {
                        b.append(" e" + e);
                    }
                }

                if(i < n - 1) {
                    b.append(' ');
                }
            }
            b.append("[" + getWeight() + "]");
            return b.toString();
        }
    }

    public IntArrayList getEdgeList() {
        return edgeList;
    }
}
