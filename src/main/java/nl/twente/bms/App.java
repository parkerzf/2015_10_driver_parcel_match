package nl.twente.bms;

import nl.twente.bms.model.MatchingModel;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String confFilePath = "Data.xls";
        MatchingModel model = new MatchingModel();
        model.load(confFilePath);

    }
}
