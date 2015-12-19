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
        String confFilePath = "Data76.xls";
        MatchingModel model = new MatchingModel(confFilePath);

        ArrayList<Driver> driverList = model.getDriverConfig().getDriverList();

        WritableWorkbook wworkbook = Workbook.createWorkbook(new File("drivers76.xls"));

        int sheetIndex = 0;
        for(Driver driver: driverList){
            WritableSheet sheet = wworkbook.createSheet(Integer.toString(driver.getId()), sheetIndex++);
            model.setHeader(sheet, driver.getId());
            driver.setSpreadSheetByMaxDetourPaths(sheet);
        }
        wworkbook.write();
        wworkbook.close();
    }


}
