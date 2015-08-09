package ny2.ats.market.connection;

import ny2.ats.core.data.MarketData;

public interface IMessageConverter<T> {

    public MarketData convertMessage(T marketDataMessage);
    
}
