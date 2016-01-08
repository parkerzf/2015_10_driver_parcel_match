package nl.twente.bms;

import nl.twente.bms.model.MatchingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test application
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        String confFilePath = "Data.xls";
        int numDrivers = -1;
        int numParcels = -1;
        if(args.length == 1){
            confFilePath = args[0];
        }
        else if(args.length == 2){
            numDrivers = Integer.parseInt(args[0]);
            numParcels = Integer.parseInt(args[1]);
        }
        else if(args.length == 3){
            confFilePath = args[0];
            numDrivers = Integer.parseInt(args[1]);
            numParcels = Integer.parseInt(args[2]);
        }

        System.out.println("confFilePath: " + confFilePath);
        System.out.println("numDrivers: " + numDrivers);
        System.out.println("numParcels: " + numParcels);

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        long start = System.currentTimeMillis();
        MatchingModel model = new MatchingModel(confFilePath, numDrivers, numParcels);
        model.solve();
        long end = System.currentTimeMillis();

        System.out.println("Model objective value: " + model.computeCost());
        System.out.println("Model running time: " + (end - start) + "ms");
    }
}
