package nl.twente.bms.model.elem;


import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import jxl.Workbook;
import jxl.write.*;
import jxl.write.Number;
import jxl.write.biff.RowsExceededException;
import nl.twente.bms.algo.struct.WeightedGrph;
import nl.twente.bms.algo.struct.WeightedSmartPath;

import java.io.File;
import java.util.Collection;

/**
 * The class to store the Drive object
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class Driver {

    private int id;
    private int startStationId;
    private int endStationId;

    private int earliestDepartureTime;
    private int latestArrivalTime;

    private int capacity;

    private Collection<WeightedSmartPath> maxDetourPaths;

    public Driver(int id, int startStationId, int endStationId,
                  int earliestDepartureTime, int latestArrivalTime, int capacity) {
        this.id = id;
        this.startStationId = startStationId;
        this.endStationId = endStationId;
        this.earliestDepartureTime = earliestDepartureTime;
        this.latestArrivalTime = latestArrivalTime;
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }

    public int getStartStationId() {
        return startStationId;
    }

    public int getEndStationId() {
        return endStationId;
    }

    public int getEarliestDepartureTime() {
        return earliestDepartureTime;
    }

    public int getLatestArrivalTime() {
        return latestArrivalTime;
    }

    public int getCapacity() {
        return capacity;
    }

    public Collection<WeightedSmartPath> getMaxDetourPaths() {
        return maxDetourPaths;
    }

    public void setMaxDetourPaths(Collection<WeightedSmartPath> maxDetourPaths) {
        this.maxDetourPaths = maxDetourPaths;
    }

    public void setSpreadSheetByMaxDetourPaths(WritableSheet sheet) throws WriteException {
        if(maxDetourPaths == null) return;

        for(WeightedSmartPath path: maxDetourPaths){
            for(int i = 1; i < path.getNumberOfVertices(); i++){
                int u = path.getVertexAt(i - 1);
                int v = path.getVertexAt(i);
                sheet.addCell(new Number(v+1, u, 1));
            }
        }
    }

    public String toString(){
        return String.format("Driver[%d]: %d->%d, #Paths: %d, (%d, %d), %d", id, startStationId, endStationId,
                maxDetourPaths.size(), earliestDepartureTime, latestArrivalTime, capacity);
    }
}
