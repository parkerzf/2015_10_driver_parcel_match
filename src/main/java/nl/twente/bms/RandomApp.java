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
        String driverIndicesStr = null;
        String parcelIndicesStr = null;

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
        else if(args.length == 8){
            confFilePath = args[0];
            numIter = Integer.parseInt(args[1]);
            numDrivers = Integer.parseInt(args[2]);
            numParcels = Integer.parseInt(args[3]);
            detour = Double.parseDouble(args[4]);
            isRandom = Boolean.parseBoolean(args[5]);
            driverIndicesStr = args[6];
            parcelIndicesStr = args[7];
        }


        System.out.println("##########  Configurations:");
        System.out.println("confFilePath: " + confFilePath);
        System.out.println("# Iterations: " + numIter);
        System.out.println("numDrivers: " + numDrivers);
        System.out.println("numParcels: " + numParcels);
        System.out.println("detour: " + detour);
        System.out.println("isRandom: " + isRandom);
        System.out.println("driverIndicesStr: " + driverIndicesStr);
        System.out.println("parcelIndicesStr: " + parcelIndicesStr);

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);


        long globalStart = System.currentTimeMillis();


        int i = 1;

        long start = System.currentTimeMillis();
        MatchingModel model = new MatchingModel(confFilePath, numDrivers, numParcels,
                detour, isRandom, driverIndicesStr, parcelIndicesStr);
        model.showDriversAndParcels();

        System.out.println("##########  Iteration 1:");
        model.solve();
        long end = System.currentTimeMillis();
        double initCost = model.computeCost();
        double bestCost = initCost;
        int bestIter = i;

        System.out.println("Iteration 1 running time: " + (end - start) + "ms");

        double totalShippingCost = model.getParcelConfig().getTotalShippingCost();
        System.out.println("Objective value: " + bestCost);
        System.out.println("Worst case value: " + totalShippingCost);
        System.out.println(String.format("Saving: %.2f%%", (totalShippingCost - bestCost)*100/totalShippingCost));


        System.out.println("########## Constrained Random: ");
        while(++i <= numIter){
            model.shuffle(false);
            model.solve();
            double cost = model.computeCost();
            if(cost < bestCost){
                System.out.println(i + "\t" + cost);
                bestCost = cost;
                bestIter = i;
            }
        }


        long globalEnd = System.currentTimeMillis();
        System.out.println("Model running time: " + (globalEnd - globalStart) + "ms");


        System.out.println("Best model result is Iteration: " + bestIter);
        totalShippingCost = model.getParcelConfig().getTotalShippingCost();
        System.out.println("Objective value: " + bestCost);
        System.out.println("Worst case value: " + totalShippingCost);
        System.out.println(String.format("Saving: %.2f%%", (totalShippingCost - bestCost)*100/totalShippingCost));



        System.out.println("########### Full Random: ");
        globalStart = System.currentTimeMillis();
        i = 1;

        bestCost = initCost;
        bestIter = i;

        while(++i <= numIter * 4){
            model.shuffle(true);
            model.solve();
            double cost = model.computeCost();
            if(cost < bestCost){
                System.out.println(i + "\t" + cost);
                bestCost = cost;
                bestIter = i;
            }
        }

        globalEnd = System.currentTimeMillis();
        System.out.println("Model running time: " + (globalEnd - globalStart) + "ms");


        System.out.println("Best model result is Iteration: " + bestIter);
        totalShippingCost = model.getParcelConfig().getTotalShippingCost();
        System.out.println("Objective value: " + bestCost);
        System.out.println("Worst case value: " + totalShippingCost);
        System.out.println(String.format("Saving: %.2f%%", (totalShippingCost - bestCost)*100/totalShippingCost));
    }
}
