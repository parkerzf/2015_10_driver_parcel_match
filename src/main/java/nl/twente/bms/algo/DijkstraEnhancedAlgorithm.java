package nl.twente.bms.algo;

import grph.Grph;
import grph.algo.search.GraphSearchListener;
import grph.algo.search.SearchResult;
import grph.algo.search.WeightedSingleSourceSearchAlgorithm;
import grph.algo.topology.ClassicalGraphs;
import grph.properties.NumericalProperty;
import nl.twente.bms.algo.struct.FibonacciHeap;
import nl.twente.bms.algo.struct.TimeTableImproved;
import nl.twente.bms.model.conf.DriverConfig;
import nl.twente.bms.model.elem.Offer;
import toools.NotYetImplementedException;
import toools.set.IntSet;

import com.carrotsearch.hppc.cursors.IntCursor;

import java.util.HashMap;
import java.util.Map;

/**
 * Computes the shortest paths in the graph, using the enhanced Dijkstra algorithm.
 *
 * @author zhaofeng
 * @since 1.0
 */
public class DijkstraEnhancedAlgorithm extends WeightedSingleSourceSearchAlgorithm
{
    NumericalProperty offerIdProperty;
    IntSet markedRemoveOfferIds;

    public DijkstraEnhancedAlgorithm(NumericalProperty weightProperty)
    {
        this(weightProperty, null, null);
    }

    public DijkstraEnhancedAlgorithm(NumericalProperty weightProperty,
                                     NumericalProperty offerIdProperty,
                                     IntSet markedRemoveOfferIds)
    {
        super(weightProperty);
        this.offerIdProperty = offerIdProperty;
        this.markedRemoveOfferIds = markedRemoveOfferIds;
    }

    @Override
    public SearchResult compute(Grph g, int source, Grph.DIRECTION d, GraphSearchListener listener){
        return compute(g, source, null, 0, d, listener);
    }


    public SearchResult compute(Grph g, int source, DriverConfig driverConfig, int volume, Grph.DIRECTION d, GraphSearchListener listener)
    {
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
                if(markedRemove(n)){
                    g.removeVertex(n);
                    continue;
                }
                if(!haveCapacity(n, driverConfig, volume)){
                    continue;
                }
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

    private boolean haveCapacity(int vertexId, DriverConfig config, int volume) {
        if(offerIdProperty == null || config == null || volume <= 0) return true;
        Offer offer = config.getOfferById(offerIdProperty.getValueAsInt(vertexId));
        return offer.getCapacity() >= volume;
    }

    private boolean markedRemove(int vertexId) {
        if(offerIdProperty == null || markedRemoveOfferIds == null) return false;
        int offerId = offerIdProperty.getValueAsInt(vertexId);
        if(markedRemoveOfferIds.contains(offerId)) return true;
        return false;
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

        SearchResult r = new DijkstraEnhancedAlgorithm(weightProperty).compute(g, 0, new GraphSearchListener() {

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