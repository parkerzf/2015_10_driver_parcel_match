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
        if(args.length == 1){
            confFilePath = args[0];
        }

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        MatchingModel model = new MatchingModel(confFilePath);
        model.solve();

        logger.info("Model objective value: " + model.computeCost());
    }
}
