package itea.project.utils;

import itea.project.MainApp;
import itea.project.model.DataRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static itea.project.MainApp.LOGGER;
import static itea.project.utils.FxUtils.alertError;

public class Utils {
    public static String[] getStoreList() {
        return Ini4J.getInstance().getParam("STORES_NUM", "Stores_num").split(",");
    }

    private static Map<String, CellStyle> styleMap = new HashMap<>();


    public static void checkSQLFolder() {
        try {
            File sql_dir_res = new File(MainApp.class.getClassLoader().getResource("SQL").toURI());
            Path sql_dir_local = Paths.get("SQL");
            if (sql_dir_res.exists() && sql_dir_res.isDirectory()) {
                if (!Files.exists(sql_dir_local) || !Files.isDirectory(sql_dir_local)) {
                    if (!new File(sql_dir_local.toUri()).mkdir()) {
                        throw new Exception("Cannot create \"SQL\" dir here");
                    }
                }
                for (String sql_filename : sql_dir_res.list()) {
                    if (!Files.exists(Paths.get("SQL/" + sql_filename))) {
                        Files.copy(new File(sql_dir_res.getAbsolutePath() + "/" + sql_filename).toPath(), new File(sql_dir_local.toFile().getAbsolutePath() + "/" + sql_filename).toPath());
                    }
                }
            } else {
                System.out.println("No SQL directory in resources");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSQLFromFile(String sql_filename) {
        Path p = Paths.get("SQL/" + sql_filename);
        if (Files.exists(p)) {
            try {
                return String.join(" ", Files.readAllLines(p));
            } catch (Exception e) {
                alertError(e);
            }
        } else {
            alertError(new FileNotFoundException("No sql file with name \"" + sql_filename + "\" in SQL folder"));
        }
        return null;
    }

    private static void fillStyleMap(Workbook wb) {
        CellStyle cellStyleYellow = wb.createCellStyle();
        cellStyleYellow.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyleYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleRed = wb.createCellStyle();
        cellStyleRed.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyleRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleCoral = wb.createCellStyle();
        cellStyleCoral.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        cellStyleCoral.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleOddCell = wb.createCellStyle();
        cellStyleOddCell.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        cellStyleOddCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleHeader = wb.createCellStyle();
        cellStyleHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        cellStyleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        cellStyleHeader.setFont(font);

        styleMap.put("Yellow", cellStyleYellow);
        styleMap.put("Red", cellStyleRed);
        styleMap.put("Header", cellStyleHeader);
        styleMap.put("OddCell", cellStyleOddCell);
        styleMap.put("Coral", cellStyleCoral);
    }

    public static void save2Excel(String fileName, Map<String, List<DataRow>> excelMap) {

        if (excelMap.size() > 0) {
            Workbook wb = new XSSFWorkbook();
            fillStyleMap(wb);

            boolean writeToFile = false;
            try {
                Files.deleteIfExists(Paths.get(new File("").getAbsolutePath().substring(0, new File("").getAbsolutePath().length()) + "\\" + fileName));
                int tableNum = 0;
                for (String s : excelMap.keySet()) {
                    writeToFile = true;
                    tableNum++;
                    List<DataRow> tempList = new ArrayList<>(excelMap.get(s));
                    int maxCol = tempList.get(0).size();
                    int maxRow = tempList.size();
                    XSSFSheet sheet = (XSSFSheet) wb.createSheet(s);

                    DataRow td = tempList.get(0);
                    XSSFRow trow = sheet.createRow(0);
                    for (int j = 0; j < td.size(); j++) {
                        XSSFCell cell = trow.createCell(j);
                        cell.setCellValue(td.get(j).toString());
                        cell.setCellStyle(styleMap.get("Header"));
                    }

                    for (int i = 1; i < maxRow; i++) {
                        XSSFRow row = sheet.createRow(i);
                        DataRow d = tempList.get(i);
                        for (int j = 0; j < d.size(); j++) {
                            XSSFCell cell = row.createCell(j);
                            cell.setCellValue(d.get(j).toString());
                        }
                    }
                    sheet.setAutoFilter(new CellRangeAddress(0, maxRow - 1, 0, maxCol - 1));
                    // ------ Sheet customising ---------
                    for (int i = 0; i < maxCol; i++) {
                        sheet.autoSizeColumn(i);
                        sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 500);
                    }
                }
                if (writeToFile) {
                    try (FileOutputStream out = new FileOutputStream(fileName)) {
                        wb.write(out);
                    } catch (Exception e) {
                        alertError(e);
                    }

                }

            } catch (Exception e) {
                alertError(e);
            } finally {
                try {
                    wb.close();
                } catch (IOException e) {
                    alertError(e);
                }
            }
        }
    }
}
