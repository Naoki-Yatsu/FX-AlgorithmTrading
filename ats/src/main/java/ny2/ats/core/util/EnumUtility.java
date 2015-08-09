package ny2.ats.core.util;


public class EnumUtility {

    /**
     * 存在すればnameを返します。
     * @param enumObj
     * @return
     */
    public static String nameIfPresent(Enum<?> enumObj){
        if (enumObj != null) {
            return enumObj.name();
        } else {
            return null;
        }
    }
}
