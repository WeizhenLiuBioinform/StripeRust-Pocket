package util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import adapter.UserInfo;
import enity.Pic;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class Excel {
    public static WritableFont arial14font = null;
    public static WritableCellFormat arial14format = null;
    public static WritableFont arial10font = null;
    public static WritableCellFormat arial10format = null;
    public static WritableFont arial12font = null;
    public static WritableCellFormat arial12format = null;
    public final static String UTF8_ENCODING = "UTF-8";
    public final static String GBK_ENCODING = "GBK";
    public static void format() {
        try {
            arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
            arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
            arial14format = new WritableCellFormat(arial14font);
            arial14format.setAlignment(jxl.format.Alignment.CENTRE);
            arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
            arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            arial10format = new WritableCellFormat(arial10font);
            arial10format.setAlignment(jxl.format.Alignment.CENTRE);
            arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
            arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
            arial12font = new WritableFont(WritableFont.ARIAL, 12);
            arial12format = new WritableCellFormat(arial12font);
            arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
        }
        catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     * @param fileName 文件名称
     * @param colName 需要导出的列名
     */
    public static void initExcel(String fileName, String[] colName) {
        format();
        WritableWorkbook workbook = null;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.delete();
                file.createNewFile();
                workbook = Workbook.createWorkbook(file);
            } else {
                workbook = Workbook.createWorkbook(file);
            }
            WritableSheet sheet = workbook.createSheet("Name", 0);
            sheet.addCell((WritableCell) new Label(0, 0, fileName, arial14format));
            for (int col = 0; col < colName.length; col++) {
                sheet.addCell(new Label(col, 0, colName[col], arial10format));
            }
            workbook.write();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (workbook != null) {
                try {
                    workbook.close();
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 写出excel文件
     * @param objList 数据源
     * @param fileName 文件名称
     * @param <T> 数据类型
     * @return 写出的Excel文件
     */
    @SuppressWarnings("unchecked")
    public static <T> File writeObjListToExcel(List<Pic> objList, String fileName) {
        File file = null;
        if (objList != null && objList.size() > 0) {
            WritableWorkbook writebook = null;
            InputStream in = null;
            file = new File(fileName);
            if (!file.exists()) {
                try {
                    file.delete();
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                WorkbookSettings setEncode = new WorkbookSettings();
                setEncode.setEncoding(UTF8_ENCODING);
                in = new FileInputStream(file);
                Workbook workbook = Workbook.getWorkbook(in);
                writebook = Workbook.createWorkbook(new File(fileName), workbook);
                WritableSheet sheet = null;
                try {
                    sheet = writebook.getSheet(0);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < objList.size(); j++) {
                    Pic list= objList.get(j);
                    String name = null;
                    String date = null;
                    float percent = 0;
                    name = list.name;
                    date = list.date;
                    percent = list.severity;
                    ArrayList<String> list_string = new ArrayList<>();
                    list_string.add(name);
                    list_string.add(String.valueOf(percent));
                    list_string.add(date);
                    for (int i = 0; i < list_string.size(); i++) {
                        sheet.addCell(new Label(i, j + 1, list_string.get(i), arial12format));
                        if (list_string.get(i).length() <= 4) {
                            //设置列宽
                            sheet.setColumnView(i, list_string.get(i).length() + 8);
                        } else {
                            //设置列宽
                            sheet.setColumnView(i, list_string.get(i).length() + 5);
                        }

                        //设置行高
                        sheet.setRowView(j + 1, 350);
                    }

                }
                writebook.write();
                //Toast.makeText(c, "保存成功", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (writebook != null) {
                    try {
                        writebook.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return file;
    }
    public static Object getValueByRef(Class cls, String fieldName) {
        Object value = null;
        fieldName = fieldName.replaceFirst(fieldName.substring(0, 1), fieldName.substring(0, 1).toUpperCase());
        String getMethodName = "get" + fieldName;
        try {
            Method method = cls.getMethod(getMethodName);
            value = method.invoke(cls);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
