package nl.twente.bms.algo;

import com.carrotsearch.hppc.cursors.IntCursor;
import grph.Grph;
import grph.algo.search.GraphSearchListener;
import grph.algo.search.SearchResult;
import grph.algo.search.WeightedSingleSourceSearchAlgorithm;
import grph.algo.topology.ClassicalGraphs;
import grph.properties.NumericalProperty;
import nl.twente.bms.algo.struct.FibonacciHeap;
import toools.NotYetImplementedException;
import toools.set.IntSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Computes the shortest paths in the graph, using the Dijkstra algorithm with FibonacciHeap
 *
 * @author zhaofeng
 * @since 1.0
 */
public class DijkstraHeapAlgorithm extends WeightedSingleSourceSearchAlgorithm
{
    public DijkstraHeapAlgorithm(NumericalProperty weightProperty) {
        super(weightProperty);
    }

    @Override
    public SearchResult compute(Grph g, int source, Grph.DIRECTION d, GraphSearchListener listener){
        if (d != Grph.DIRECTION.out)
            throw new NotYetImplementedException("this direction is not supported: " + d.name());

        SearchResult r = new SearchResult(g.getVertices().getGreatest() + 1);
        FibonacciHeap<Integer> notYetVisitedVertices = new FibonacciHeap<>();
        Map<Integer, FibonacciHeap.Entry<Integer>> entries = new HashMap<>();
        for (int i = 0; i < r.distances.length; ++i)
        {
            r.distances[i] = Integer.MAX_VALUE;
            r.predecessors[i] = -1;
            entries.put(i, notYetVisitedVertices.enqueue(i, r.distances[i]));
        }

        r.distances[source] = 0;
        notYetVisitedVertices.decreaseKey(entries.get(source), 0);

        if (listener != null)
            listener.searchStarted();

        int[][] neighbors = g.getOutNeighborhoods();

        while (!notYetVisitedVertices.isEmpty())
        {
            int minVertex = notYetVisitedVertices.dequeueMin().getValue();
            r.visitOrder.add(minVertex);

            if (listener != null)
                listener.vertexFound(minVertex);

            for (int n : neighbors[minVertex])
            {
                int newDistance = r.distances[minVertex] + weight(g, minVertex, n, getWeightProperty());

                if (newDistance < r.distances[n])
                {
                    r.predecessors[n] = minVertex;
                    r.distances[n] = newDistance;
                    FibonacciHeap.Entry<Integer>  entry = entries.get(n);
                    notYetVisitedVertices.decreaseKey(entry, r.distances[n]);
                }
            }
        }

        if (listener != null)
            listener.searchCompleted();

        return r;

    }

    private int weight(Grph g, int src, int dest, NumericalProperty weightProperty)
    {
        IntSet connectingEdges = g.getEdgesConnecting(src, dest);

        if (connectingEdges.isEmpty())
            throw new IllegalStateException("vertices are not connected");

        int w = Integer.MAX_VALUE;

        for (IntCursor c : connectingEdges)
        {
            int e = c.value;
            int p = weightProperty == null ? 1 : weightProperty.getValueAsInt(e);

            if (p < w)
            {
                w = p;
            }
        }

        return w;
    }

    @Override
    protected SearchResult[] createArray(int n)
    {
        return new SearchResult[n];
    }

    public static void main(String[] args)
    {
        Grph g = ClassicalGraphs.grid(3, 3);
        NumericalProperty weightProperty = new NumericalProperty("weights", 4, 1);

        for (int e : g.getEdges().toIntArray())
        {
            weightProperty.setValue(e, (int) (Math.random() * 10));
        }

        g.setEdgesLabel(weightProperty);
        g.display();

        SearchResult r = new DijkstraHeapAlgorithm(weightProperty).compute(g, 0, new GraphSearchListener() {

            @Override
            public DECISION vertexFound(int v)
            {
                System.out.println("found vertex: " + v);
                return DECISION.CONTINUE;
            }

            @Override
            public void searchStarted()
            {
                System.out.println("search starting");
            }

            @Override
            public void searchCompleted()
            {
                System.out.println("search terminated");
            }
        });

        System.out.println(r.toString(g.getVertices()));
        System.out.println(r.visitOrder);
        System.out.println(r.farestVertex());
    }

}