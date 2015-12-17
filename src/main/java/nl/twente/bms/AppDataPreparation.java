package nl.twente.bms;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;
import nl.twente.bms.model.MatchingModel;

import java.io.IOException;

/**
 * Test application
 */
public class AppDataPreparation {
    public static void main(String[] args) throws IOException, WriteException, BiffException {
        String confFilePath = "Data.xls";
        if(args.length == 1){
            confFilePath = args[0];
        }

        MatchingModel model = new MatchingModel(confFilePath);
        model.outputShortestDistance(confFilePath);

    }
}
