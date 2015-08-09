package ny2.ats.position;

import java.time.LocalDateTime;
import java.util.Map;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.Position;

public interface IPositionHolder {

    /**
     * Positionを更新します。
     *
     * @param filledOrder
     * @param marketData
     * @param ccy2JpyMarketData
     * @return
     */
    public Position updatePosition(Order filledOrder, MarketData marketData, MarketData ccy2JpyMarketData);

    /**
     * 全てのPositionを取得します
     * @return
     */
    public Map<Symbol, Position> getAllPosition();

    /**
     * Positionを返します。
     *
     * @param symbol
     * @return
     */
    public Position getPosition(Symbol symbol);

    /**
     * NetのAmountを返します。BUYが多いほうがLongでプラスです。
     *
     * @return
     */
    public Map<Symbol, Integer> getNetAmountAll();

    /**
     * NetのAmountを返します。BUYが多いほうがLongでプラスです。
     *
     * @param symbol
     * @return
     */
    public int getNetAmount(Symbol symbol);

    /**
     * 全ての通貨ペアのPLを返します。
     *
     * @return
     */
    public Map<Symbol, Integer> getAllPlJpy();

    /**
     * 対象通貨ペアのPLを返します。
     *
     * @param symbol
     * @return
     */
    public int getPlJpy(Symbol symbol);

    /**
     * 全通貨ペアの合計PLを返します。
     *
     * @return
     */
    public int getTotalPlJpy();

    /**
     * 単純計算したPipsのPLを返します。
     *
     * @return
     */
    public double getTotalPlPips();

    /**
     * 最新のマーケットデータを使用してPLを再計算します。
     * @param marketDataMap
     */
    public void updatePl(Map<Symbol, MarketData> marketDataMap);

    /**
     * Positionの最終約定時刻を取得します。
     * @return
     */
    public LocalDateTime getLastExecuteDateTime();

}
