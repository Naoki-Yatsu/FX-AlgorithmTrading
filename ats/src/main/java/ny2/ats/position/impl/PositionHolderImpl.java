package ny2.ats.position.impl;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ny2.ats.core.common.Symbol;
import ny2.ats.core.data.MarketData;
import ny2.ats.core.data.Order;
import ny2.ats.core.data.Position;
import ny2.ats.position.IPositionHolder;

/**
 * 全てのポジションを保持するクラスです。
 */
@Component
public class PositionHolderImpl implements IPositionHolder {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /** 通貨ペアごとのPosition */
    private final EnumMap<Symbol, Position> symbolPositionMap = new EnumMap<>(Symbol.class);

    /** 通貨ペアごとのPL */
    private final Map<Symbol, Integer> plJpyMap = new EnumMap<>(Symbol.class);

    /** 通貨ペアごとのPL(Pips) */
    private final Map<Symbol, Double> plPipsMap = new EnumMap<>(Symbol.class);

    /** 通貨ペアごとのNetOpenAmount */
    private final Map<Symbol, Integer> amountJpyMap = new EnumMap<>(Symbol.class);

    /** TotalのPL金額*/
    private int totalPl;

    /** TotalのPips単位のPL*/
    private double totalPlPips;

    /** 最終約定Market日時 */
    private LocalDateTime lastExecuteDateTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public PositionHolderImpl() {
        lastExecuteDateTime = LocalDateTime.now();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public Position updatePosition(Order filledOrder, MarketData marketData, MarketData ccy2JpyMarketData) {
        Position position = symbolPositionMap.get(filledOrder.getSymbol());
        if (position == null) {
            position = createOpenPosition(filledOrder.getSymbol());
        }
        position.updatePosition(filledOrder, marketData, ccy2JpyMarketData);
        lastExecuteDateTime = position.getExecuteDateTime();
        return position;
    }

    /**
     * ポジションマップに通貨ペアを追加します
     *
     * @param symbol
     * @return position
     */
    private synchronized Position createOpenPosition(Symbol symbol) {
        logger.debug("Create new PositionMap with Symbol = {}", symbol.name());
        // Check position is exist or not again.
        Position position = symbolPositionMap.get(symbol);
        if (position != null) {
            return position;
        }
        position = new Position(symbol);
        symbolPositionMap.put(symbol, position);
        return position;
    }

    @Override
    public Map<Symbol, Position> getAllPosition() {
        return symbolPositionMap;
    }

    @Override
    public Position getPosition(Symbol symbol) {
        return symbolPositionMap.get(symbol);
    }

    @Override
    public Map<Symbol, Integer> getNetAmountAll() {
        return amountJpyMap;
    }

    @Override
    public int getNetAmount(Symbol symbol) {
        Position position = symbolPositionMap.get(symbol);
        if (position == null) {
            return 0;
        }
        return position.getNetOpenAmount();
    }

    @Override
    public Map<Symbol, Integer> getAllPlJpy() {
        return plJpyMap;
    }

    @Override
    public int getPlJpy(Symbol symbol) {
        // 対象通貨ペアがなければゼロを返す
        Integer plJpy = plJpyMap.get(symbol);
        if (plJpy != null) {
            return plJpy;
        } else {
            return 0;
        }
    }

    @Override
    public void updatePl(Map<Symbol, MarketData> marketDataMap) {
        MarketData usdjpyMarketData = marketDataMap.get(Symbol.USDJPY);
        // ポジションの通貨ペアごとにループ
        for (Entry<Symbol, Position> entry : symbolPositionMap.entrySet()) {
            Symbol symbol = entry.getKey();
            // とりあえずUSDJPYを入れておく
            MarketData ccy2JpyMarketData = usdjpyMarketData;
            if (!symbol.isContainJpy() && !symbol.isCcy2Usd()) {
                Symbol ccy2Jpy = symbol.getCcy2JpySymbol();
                ccy2JpyMarketData = marketDataMap.get(ccy2Jpy);
                // ccy2jpyが存在しない場合は、とりあえずUSDJPYで計算する
                if (ccy2JpyMarketData == null) {
                    logger.error("MarketData : {} is NOT exist.", ccy2Jpy.name());
                    ccy2JpyMarketData = usdjpyMarketData;
                }
            }
            Integer pl = entry.getValue().calcTotalPlJpy(marketDataMap.get(symbol), ccy2JpyMarketData);
            plJpyMap.put(symbol, pl);

            // PipsのPL
            Double pipsPl = entry.getValue().getTotalPlPips(marketDataMap.get(symbol));
            plPipsMap.put(symbol, pipsPl);
        }

        // Net Amountも更新する
        symbolPositionMap.values().stream().forEach(position -> amountJpyMap.put(position.getSymbol(), position.getNetOpenAmount()));

        // 全通貨ペアTotalのPL
        Optional<Integer> tempTotal = plJpyMap.values().stream().reduce((sum, pl) -> sum + pl);
        totalPl = tempTotal.orElse(0);

        // 全通貨ペアTotalのPL-Pips
        Optional<Double> tempTotalPips = plPipsMap.values().stream().reduce((sum, plPips) -> sum + plPips);
        totalPlPips = tempTotalPips.orElse(0.0);
    }

    @Override
    public int getTotalPlJpy() {
        return totalPl;
    }

    @Override
    public double getTotalPlPips() {
        return totalPlPips;
    }

    @Override
    public LocalDateTime getLastExecuteDateTime() {
        return lastExecuteDateTime;
    }
}
