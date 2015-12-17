package nl.twente.bms;

import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import nl.twente.bms.model.MatchingModel;
import nl.twente.bms.model.elem.Driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException, WriteException {
        String confFilePath = "Data.xls";
        MatchingModel model = new MatchingModel(confFilePath);

        ArrayList<Driver> driverList = model.getDriverConfig().getDriverList();

        WritableWorkbook wworkbook = Workbook.createWorkbook(new File("drivers.xls"));

        int sheetIndex = 0;
        for(Driver driver: driverList){
            WritableSheet sheet = wworkbook.createSheet("driver "+ driver.getId(), sheetIndex++);
            driver.setSpreadSheetByMaxDetourPaths(sheet);
        }
        wworkbook.write();
        wworkbook.close();
    }


}
