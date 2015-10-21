package nl.twente.bms.utils;

import jxl.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class is a excel utils
 *
 * @author Feng Zhao (feng.zhao@feedzai.com)
 * @since 1.0
 */
public class ExcelHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExcelHandler.class);
    private Workbook rwb;

    /**
     * construct the excel handler
     * @param filePath: the excel file path
     */
    public ExcelHandler(String filePath) {
        try{
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream is = classLoader.getResourceAsStream(filePath);
            rwb = Workbook.getWorkbook(is);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * close the work book
     */
    public void close(){
        rwb.close();
    }

    /**
     * set the work book
     * @param filePath: the excel file path
     */
    public void setWorkbook(String filePath){
        if(rwb!=null)
            rwb.close();
        try {
            //Get file from resources folder
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream is = classLoader.getResourceAsStream(filePath);
            rwb = Workbook.getWorkbook(is);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * read a cell from xsl
     * @param sheetName: the name of the sheet to read from
     * @param col, the column index to read, start from 0
     * @param row, the row index to read, start from 0
     * @return the contents in the read range
     */
    public String xlsread(String sheetName, int col, int row){
        if(col < 0){
            logger.error("Excel column {} out of range", col);
            return null;
        }
        if(row < 0){
            logger.error("Excel row {} out of range", row);
            return null;
        }
        Sheet st = rwb.getSheet(sheetName);
        return st.getCell(col,row).getContents();
    }

    /**
     * read a range of cells from xsl
     * @param sheetName: the name of the sheet to read from
     * @param col, the column index to read, start from 0
     * @param rowStart, the row read start index, start from 0
     * @param rowEnd, the row read end index, start from rowStart
     * @return the contents in the read range
     */
    public String[] xlsread(String sheetName, int col, int rowStart,int rowEnd){
        if(col < 0){
            logger.error("Excel column {} out of range", col);
            return null;
        }
        if(rowStart < 0 || rowEnd < rowStart) {
            logger.error("Excel row {} {} out of range", rowStart, rowEnd);
            return null;
        }
        Sheet st = rwb.getSheet(sheetName);
        String[] cells = new String[rowEnd - rowStart +1];
        for(int i = rowStart; i<= rowEnd; i++)
            cells[i - rowStart] = st.getCell(col, i).getContents();
        return cells;
    }

    /**
     * read a range of cells from xsl
     * @param sheetName: the name of the sheet to read from
     * @param colStart, the column read start index, start from 0
     * @param colEnd, the column read end index, start from colStart
     * @param rowStart, the row read start index, start from 0
     * @param rowEnd, the row read end index, start from rowStart
     * @return the contents in the read range
     */
    public String[][] xlsread(String sheetName, int colStart, int colEnd, int rowStart,int rowEnd){

        Sheet st = rwb.getSheet(sheetName);
        String[][] cells = new String[colEnd-colStart +1][rowEnd - rowStart +1];
        for(int i = colStart; i<= colEnd; i++){
            for(int j = rowStart; j<= rowEnd; j++)
                cells[i- colStart][j - rowStart] = st.getCell(i,j).getContents();
        }
        return cells;
    }
}
