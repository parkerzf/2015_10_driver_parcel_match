package nl.twente.bms;

import nl.twente.bms.model.MatchingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test application
 */
public class RandomApp {
    private static final Logger logger = LoggerFactory.getLogger(RandomApp.class);

    public static void main(String[] args) {
        String confFilePath = "Data.xls";
        int numIter = 10;
        int numDrivers = -1;
        int numParcels = -1;
        double detour = -1;
        boolean isRandom = false;
        if(args.length == 1){
            confFilePath = args[0];
        }
        else if(args.length == 2){
            confFilePath = args[0];
            numIter = Integer.parseInt(args[1]);
        }
        else if(args.length == 3){
            confFilePath = args[0];
            numDrivers = Integer.parseInt(args[1]);
            numParcels = Integer.parseInt(args[2]);
        }
        else if(args.length == 4){
            confFilePath = args[0];
            numIter = Integer.parseInt(args[1]);
            numDrivers = Integer.parseInt(args[2]);
            numParcels = Integer.parseInt(args[3]);
        }
        else if(args.length == 5){
            confFilePath = args[0];
            numIter = Integer.parseInt(args[1]);
            numDrivers = Integer.parseInt(args[2]);
            numParcels = Integer.parseInt(args[3]);
            detour = Double.parseDouble(args[4]);
        }
        else if(args.length == 6){
            confFilePath = args[0];
            numIter = Integer.parseInt(args[1]);
            numDrivers = Integer.parseInt(args[2]);
            numParcels = Integer.parseInt(args[3]);
            detour = Double.parseDouble(args[4]);
            isRandom = Boolean.parseBoolean(args[5]);
        }

        System.out.println("confFilePath: " + confFilePath);
        System.out.println("numDrivers: " + numDrivers);
        System.out.println("numParcels: " + numParcels);
        System.out.println("detour: " + detour);
        System.out.println("isRandom: " + isRandom);

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);


        Long globalStart = System.currentTimeMillis();

        int i = 1;
        System.out.println("Iteration: " + i);
        long start = System.currentTimeMillis();
        MatchingModel model = new MatchingModel(confFilePath, numDrivers, numParcels, detour, isRandom);
        model.solve();
        long end = System.currentTimeMillis();
        double bestCost = model.computeCost();
        int bestIter = i;
        model.display(true);

        System.out.println("Iteration " + i +" running time: " + (end - start) + "ms");


        while(++i <= numIter){
            model.shuffle();
            model.solve();
            double cost = model.computeCost();
            if(cost < bestCost){
                bestCost = cost;
                bestIter = i;
            }
        }

        Long globalEnd = System.currentTimeMillis();
        System.out.println("Model running time: " + (globalEnd - globalStart) + "ms");


        System.out.println("Best model result is Iteration: " + bestIter);
        double totalShippingCost = model.getParcelConfig().getTotalShippingCost();
        System.out.println("Objective value: " + bestCost);
        System.out.println("Worst case value: " + totalShippingCost);
        System.out.println(String.format("Saving: %.2f%%", (totalShippingCost - bestCost)*100/totalShippingCost));
    }
}
