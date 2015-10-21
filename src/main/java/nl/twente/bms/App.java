package nl.twente.bms;

import nl.twente.bms.model.conf.Configuration;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String confFilePath = "Data.xls";
        Configuration conf = new Configuration(confFilePath);
    }
}
