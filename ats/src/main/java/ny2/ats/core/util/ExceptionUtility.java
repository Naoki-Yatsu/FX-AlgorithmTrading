package ny2.ats.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtility {

    /**
     * StackTraceをStringに変換します。
     * @param e
     * @return
     */
    public static String getStackTraceString(Exception e) {
        // エラーのスタックトレースを表示
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
