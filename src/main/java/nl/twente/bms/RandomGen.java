package nl.twente.bms;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import nl.twente.bms.utils.ExcelReader;


import java.io.File;
import java.io.IOException;
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

    public static void main(String[] args) throws IOException, WriteException {
        ExcelReader excelReader = new ExcelReader("Locations.xls");
        int numCities = Integer.parseInt(excelReader.xlsread("Blad1", 1, 0));
        int numDrivers = Integer.parseInt(excelReader.xlsread("Blad1", 3, 0));

        List<Weighting> weightings = new ArrayList<>();
        for(int i = 1; i < numCities + 1 ; i++){
            String cityName = excelReader.xlsread("Blad1", 1, i);
            int weight = Integer.parseInt(excelReader.xlsread("Blad1", 3, i));
            weightings.add(new Weighting(cityName, weight));
        }

        excelReader.close();


        WritableWorkbook wworkbook;
        wworkbook = Workbook.createWorkbook(new File("drivers.xls"));
        WritableSheet wsheet = wworkbook.createSheet("drivers", 0);
        for(int i = 0; i < numDrivers; i++){
            String start = weightedRandom(weightings);
            String end = null;
            while((end = weightedRandom(weightings)).equals(start)){}

            wsheet.addCell(new Label(0, i, start));
            wsheet.addCell(new Label(1, i, end));
        }
        wworkbook.write();
        wworkbook.close();
    }
}
