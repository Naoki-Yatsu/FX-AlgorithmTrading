package ny2.ats.model;

/**
 * モデルのバージョンです。
 * 実装クラスでは、enumとして実装します。
 */
public interface ModelVersion {

    /** ダミー用のバージョンです */
    public static final ModelVersion DUMMY_VERSION = new ModelVersion() {
        public String getName() {
            return "DUMMY";
        }
    };

    /**
     * nameを取得します。実装enumクラスではname()に対応します。
     * @return
     */
    public String getName();

    /**
     * NullでなければNameを返します。
     * @param modelVersion
     * @return
     */
    public static String getNameIfPresent(ModelVersion modelVersion) {
        if (modelVersion == null) {
            return "";
        }
        return modelVersion.getName();
    }
}
