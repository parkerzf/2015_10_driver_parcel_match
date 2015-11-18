package nl.twente.bms;

import nl.twente.bms.utils.ExcelHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author zhaofeng
 * @since ${1.0}
 */
public class RandomGen {

    static class Weighting {

        String value;
        int weighting;

        public Weighting(String v, int w) {
            this.value = v;
            this.weighting = w;
        }

    }

    public static String weightedRandom(List<Weighting> weightingOptions) {

        //determine sum of all weightings
        int total = 0;
        for (Weighting w : weightingOptions) {
            total += w.weighting;
        }

        //select a random value between 0 and our total
        int random = new Random().nextInt(total);

        //loop thru our weightings until we arrive at the correct one
        int current = 0;
        for (Weighting w : weightingOptions) {
            current += w.weighting;
            if (random < current)
                return w.value;
        }

        //shouldn't happen.
        return null;
    }

    public static void main(String[] args) {
        ExcelHandler excelHandler = new ExcelHandler("Locations.xls");
        int numCities = Integer.parseInt(excelHandler.xlsread("Blad1", 1, 0));

        List<Weighting> weightings = new ArrayList<>();
        for(int i = 1; i < numCities + 1 ; i++){
            String cityName = excelHandler.xlsread("Blad1", 0, i);
            int weight = Integer.parseInt(excelHandler.xlsread("Blad1", 3, i));
            weightings.add(new Weighting(cityName, weight));
        }
        String start = weightedRandom(weightings);
        String end = null;
        while((end = weightedRandom(weightings)).equals(start)){}

        System.out.println("Start: " + start);
        System.out.println("End: " + end);
    }
}
