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
        double detour = -1;
        boolean isRandom = false;
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
        else if(args.length == 4){
            confFilePath = args[0];
            numDrivers = Integer.parseInt(args[1]);
            numParcels = Integer.parseInt(args[2]);
            detour = Double.parseDouble(args[3]);
        }
        else if(args.length == 5){
            confFilePath = args[0];
            numDrivers = Integer.parseInt(args[1]);
            numParcels = Integer.parseInt(args[2]);
            detour = Double.parseDouble(args[3]);
            isRandom = Boolean.parseBoolean(args[4]);
        }

//        System.out.println("confFilePath: " + confFilePath);
//        System.out.println("numDrivers: " + numDrivers);
//        System.out.println("numParcels: " + numParcels);
//        System.out.println("detour: " + detour);
//        System.out.println("isRandom: " + isRandom);

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        long start = System.currentTimeMillis();
        MatchingModel model = new MatchingModel(confFilePath, numDrivers, numParcels, detour, isRandom, null, null);
//        model.solve();
        long end = System.currentTimeMillis();
//        System.out.println("Model running time: " + (end - start) + "ms");

        model.showDriversAndParcels();


//        double cost =  model.computeCost();
//        double totalShippingCost = model.getParcelConfig().getTotalShippingCost();
//        System.out.println("Objective value: " + cost);
//        System.out.println("Worst case value: " + totalShippingCost);
//        System.out.println(String.format("Saving: %.2f%%", (totalShippingCost - cost)*100/totalShippingCost));



    }
}
