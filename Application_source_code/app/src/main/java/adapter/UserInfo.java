package adapter;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name = "Result Table")
public class UserInfo {
    public UserInfo(String name, float severity,  String date) {
        this.name = name;
        this.severity = severity;
        this.date = date;
    }


    /**
     *  name：版块名称，count：目标值，restaurant：餐饮数量，
     *  ka：KA数量，wholesale：流通批发数量，industry：工业加工数量，
     *  other：其它数量
     * */
    @SmartColumn(id = 0, name = "ID", autoMerge = true)
    public String name;
    @SmartColumn(id = 1, name = "Severity")
    public float severity;
    @SmartColumn(id = 2, name = "Date")
    public String date;

}