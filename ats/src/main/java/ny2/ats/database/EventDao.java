package ny2.ats.database;

import java.util.List;

import ny2.ats.core.data.IData;
import ny2.ats.core.data.IndicatorInformation;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.ModelInformation;
import ny2.ats.core.data.OptimizedExecution;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.PLInformation;
import ny2.ats.core.data.Position;
import ny2.ats.core.data.SystemInformation;
import ny2.ats.core.data.TimerInformation;

public interface EventDao {

    /**
     * データを登録します。
     */
    public void insert(MarketData data);

    /**
     * データを登録します。
     */
    public void insert(Order data);

    /**
     * データを登録します。
     */
    public void insert(Position data);

    /**
     * データを登録します。
     */
    public void insert(IndicatorInformation data);

    /**
     * データを登録します。
     */
    public void insert(ModelInformation data);

    /**
     * データを登録します。
     */
    public void insert(PLInformation data);

    /**
     * データを登録します。
     */
    public void insert(OptimizedExecution data);

    /**
     * データを登録します。
     */
    public void insert(TimerInformation data);

    /**
     * データを登録します。
     */
    public void insert(SystemInformation data);

    /**
     * 複数データを登録します
     * @param <T>
     * @param dataList
     */
    public <T extends IData> void insert(Class<T> clazz, List<T> dataList);
}
