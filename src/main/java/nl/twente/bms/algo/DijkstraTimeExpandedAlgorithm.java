package nl.twente.bms.algo;

import grph.Grph;
import grph.algo.search.GraphSearchListener;
import grph.algo.search.SearchResult;
import grph.path.SearchResultWrappedPath;
import nl.twente.bms.algo.struct.FibonacciHeap;
import nl.twente.bms.algo.struct.TimeExpandedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DijkstraTimeExpandedAlgorithm
{
    private static final Logger logger = LoggerFactory.getLogger(DijkstraTimeExpandedAlgorithm.class);
    private final TimeExpandedGraph tGraph;

    public DijkstraTimeExpandedAlgorithm(TimeExpandedGraph tGraph)
    {
        this.tGraph = tGraph;
    }


    public SearchResultWrappedPath compute(int source, int destinationStationId, int volume, Grph.DIRECTION d, GraphSearchListener listener)
    {
        if (d != Grph.DIRECTION.out)
            throw new NotYetImplementedException("this direction is not supported: " + d.name());

        SearchResult r = new SearchResult(tGraph.getVertices().getGreatest() + 1);
        FibonacciHeap<Integer> notYetVisitedVertices = new FibonacciHeap<>();
        Map<Integer, FibonacciHeap.Entry<Integer>> entries = new HashMap<>();
        int destination = -1;

        for(IntCursor vertexIdCursor: tGraph.getVertices()){
            int vertexId = vertexIdCursor.value;
            r.distances[vertexId] = Integer.MAX_VALUE;
            r.predecessors[vertexId] = -1;
            entries.put(vertexId, notYetVisitedVertices.enqueue(vertexId, r.distances[vertexId]));
        }


        r.distances[source] = 0;
        notYetVisitedVertices.decreaseKey(entries.get(source), 0);

        if (listener != null)
            listener.searchStarted();

        while (!notYetVisitedVertices.isEmpty())
        {
            int minVertex = notYetVisitedVertices.dequeueMin().getValue();
            //all the remaining vertices are not accessible from the source
            if(r.distances[minVertex] == Integer.MAX_VALUE){
                break;
            }
            r.visitOrder.add(minVertex);

            if (listener != null)
                listener.vertexFound(minVertex);

            if(tGraph.getStationIdFromVertexId(minVertex) == destinationStationId){
                destination = minVertex;
                break;
            }

            for (IntCursor vCursor : tGraph.getOutNeighbors(minVertex))
            {
                int v = vCursor.value;
                if(tGraph.isMarkedRemoved(v)){
                    tGraph.removeVertex(v);
                    continue;
                }
                if(!tGraph.hasCapacity(v, volume)){
                    continue;
                }
                int newDistance = r.distances[minVertex] + weight(minVertex, v);

                if (newDistance < r.distances[v])
                {
                    r.predecessors[v] = minVertex;
                    r.distances[v] = newDistance;
                    FibonacciHeap.Entry<Integer>  entry = entries.get(v);
                    notYetVisitedVertices.decreaseKey(entry, r.distances[v]);
                }
            }
        }

        if (listener != null)
            listener.searchCompleted();

        if(destination == -1) return null;

        return new SearchResultWrappedPath(r, source, destination);
    }

    private int weight(int src, int dest)
    {
        IntSet connectingEdges = tGraph.getEdgesConnecting(src, dest);

        if (connectingEdges.isEmpty())
            throw new IllegalStateException("vertices are not connected");

        int w = Integer.MAX_VALUE;

        for (IntCursor c : connectingEdges)
        {
            int e = c.value;
            int p = tGraph.getWeightProperty() == null ? 1 : tGraph.getWeightProperty().getValueAsInt(e);

            if (p < w)
            {
                w = p;
            }
        }

        return w;
    }
}