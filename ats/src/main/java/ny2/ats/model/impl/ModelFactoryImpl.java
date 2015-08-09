package ny2.ats.model.impl;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.exception.ATSRuntimeException;
import ny2.ats.core.exception.ModelInitializeException;
import ny2.ats.core.util.ExceptionUtility;
import ny2.ats.model.IModel;
import ny2.ats.model.IModelFactory;
import ny2.ats.model.IModelManager;
import ny2.ats.model.ModelType;
import ny2.ats.model.algo.IndicatorTradeModel;

/**
 * 各種モデルのインスタンスを作成するクラスです
 */
@Component
@ManagedResource(objectName = "ModelService:name=ModelFactory")
public class ModelFactoryImpl implements IModelFactory {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IModelManager modelManager;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    private ModelFactoryImpl() {
        logger.info("Create instance.");
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public IModel deployModel(ModelType modelType, String modelVersionStr, Symbol symbol) {
        // モデル生成・JMX登録
        IModel model = createModelInstance(modelType, modelVersionStr, symbol);
        if (model == null) {
            throw new ModelInitializeException("Model is null.");
        }
        modelManager.registerModelMBean(model);
        return model;
    }

    @Override
    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "modelTypeStr", description = "value of ModelType.name()"),
            @ManagedOperationParameter(name = "modelVersionStr", description = "value of ModelVersion.name()"),
            @ManagedOperationParameter(name = "symbolStr", description = "value of Symbol.name()") })
    public String deployModelJMX(String modelTypeStr, String modelVersionStr, String symbolStr) {
        try {
            ModelType modelType = ModelType.valueOf(modelTypeStr);
            Symbol symbol = Symbol.valueOf(symbolStr);
            // deploy
            IModel model = deployModel(modelType, modelVersionStr, symbol);
            return String.format("Successfully deploy model : %s", model.getModelInformation());
        } catch (ModelInitializeException e) {
            return String.format("Failed to depoly model : %s, %s, %s \n\n%s", modelTypeStr, modelVersionStr, symbolStr, ExceptionUtility.getStackTraceString(e));
        }
    }

    /**
     * NoiseRangeModelモデルを配備します
     *
     * @return
     */
    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "modelVersionStr", description = "ModelVersion.name()"),
            @ManagedOperationParameter(name = "symbolStr", description = "Symbol.name()") })
    public String deployModelNoiseRangeModel(String modelVersionStr, String symbolStr) {
        return deployModelJMX(ModelType.NOISE_RANGE.name(), modelVersionStr, symbolStr);
    }

    /**
     * モデルのインスタンスを作成します
     *
     * @param modelType
     * @param modelVersionStr
     * @param symbol
     * @return
     */
    private IModel createModelInstance(ModelType modelType, String modelVersionStr, Symbol symbol) {
        switch (modelType) {
            case INDICATOR_TRADE:
                // IndicatorTrade は通常このメソッドを使用しません
                return IndicatorTradeModel.getInstance(modelManager, modelVersionStr, symbol);

            case DUMMY:
            case POSITION:
                // These are NOT trade model.
            default:
                break;
        }
        return null;
    }


    @Override
    public IndicatorTradeModel deployIndicatorTradeModel(String modelClassName, String versionName, Symbol symbol) {
        IndicatorTradeModel model = createIndicatorTradeModelSubModel(modelClassName, versionName, symbol);
        if (model == null) {
            throw new ModelInitializeException("Model is null.");
        }
        modelManager.registerModelMBean(model);
        return model;
    }

    @Override
    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "modelClassName", description = "class name of IndicatorTradeModel-SubModel"),
            @ManagedOperationParameter(name = "versionName", description = "version name"),
            @ManagedOperationParameter(name = "symbolStr", description = "Symbol.name()") })
    public String deployIndicatorTradeModelJMX(String modelClassName, String versionName, String symbolStr) {
        try {
            Symbol symbol = Symbol.valueOf(symbolStr);
            IModel model = deployIndicatorTradeModel(modelClassName, versionName, symbol);
            return String.format("Successfully deploy model : %s", model.getModelInformation());
        } catch (ModelInitializeException e) {
            return String.format("Failed to depoly model : %s, %s, %s \n\n%s", modelClassName, versionName, symbolStr, ExceptionUtility.getStackTraceString(e));
        }
    }

    /**
     * IndicatorTradeModelのsub-classのインスタンスを作成します
     * リフレクションを使用します
     *
     * @param modcelClassName
     * @param versionName
     * @param symbol
     * @return
     */
    public IndicatorTradeModel createIndicatorTradeModelSubModel(String modcelClassName, String versionName, Symbol symbol) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends IndicatorTradeModel> clazz = (Class<? extends IndicatorTradeModel>) Class.forName("ny2.ats.model.algo.indicatortrade." + modcelClassName);
            Constructor<?> constructor = clazz.getConstructor(IModelManager.class, String.class, Symbol.class);
            Object instance = constructor.newInstance(modelManager, versionName, symbol);
            return (IndicatorTradeModel) instance;
        } catch (Exception e) {
            throw new ATSRuntimeException(modcelClassName + " インスタンス作成でエラーが発生しました。", e);
        }
    }

}
