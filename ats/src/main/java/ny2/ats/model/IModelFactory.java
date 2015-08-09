package ny2.ats.model;

import ny2.ats.core.common.Symbol;
import ny2.ats.model.algo.IndicatorTradeModel;


/**
 * モデル作成のインターフェースです
 */
public interface IModelFactory {

    /**
     * 任意のモデルを配備します<br>
     * Versionは各モデルでparseするため、Stringでnameを指定します
     *
     * @param modelType
     * @param modelVersionStr
     * @param symbol
     * @return 配備したmodel
     */
    public IModel deployModel(ModelType modelType, String modelVersionStr, Symbol symbol);

    /**
     * JMX用のモデル配備メソッドです
     *
     * @param modelTypeStr
     * @param modelVersionStr
     * @param symbol
     * @return 実行メッセージ
     */
    public String deployModelJMX(String modelTypeStr, String modelVersionStr, String symbolStr);

    /**
     * IndicatorTradeModel系列のモデルを配備します<br>
     * モデルのクラス名はStringで指定します<br>
     * VersionNameは各モデルで必要な値を設定してください
     *
     * @param modelClassName
     * @param versionName
     * @param symbol
     * @return 配備したmodel
     */
    public IndicatorTradeModel deployIndicatorTradeModel(String modelClassName, String versionName, Symbol symbol);


    /**
     * JMX用のIndicatorTradeModel系列のモデル配備メソッドです
     * @param modelClassName
     * @param versionName
     * @param symbol
     * @return
     */
    public String deployIndicatorTradeModelJMX(String modelClassName, String versionName, String symbolStr);

}
