package com.ipssi.morphoTest;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ApachePOIExcelRead {

    private static final String FILE_NAME = "C:/Users/IPSSI/Desktop/Gevra.xlsx";

    public static void main(String[] args) {

        try {

            FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
        	XSSFWorkbook  wb = new XSSFWorkbook(excelFile);
    		
    		XSSFWorkbook test = new XSSFWorkbook(); 
    		
    		XSSFSheet sheet = wb.getSheetAt(0);
    		XSSFRow row; 
    		XSSFCell cell;

    		Iterator rows = sheet.rowIterator();

    		while (rows.hasNext())
    		{
    			row=(XSSFRow) rows.next();
    			Iterator cells = row.cellIterator();
    			while (cells.hasNext())
    			{
    				cell=(XSSFCell) cells.next();
    		
    				if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING)
    				{
    					System.out.print(cell.getStringCellValue()+" ");
    				}
    				else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
    				{
    					System.out.print(cell.getNumericCellValue()+" ");
    				}
    				else
    				{
    					//U Can Handel Boolean, Formula, Errors
    				}
    			}
                System.out.println();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
