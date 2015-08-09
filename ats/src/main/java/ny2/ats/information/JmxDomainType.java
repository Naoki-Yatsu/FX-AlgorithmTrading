package ny2.ats.information;

/**
 * JMXのDomainの一覧です
 */
public enum JmxDomainType {

    INFORMATION_SERVICE("InformationService"),
    MARKET_SERVICE("MarketService"),
    MODEL_SERVICE("ModelService"),
    MODEL_SERVICE_MODEL("ModelServiceModel"),
    POSITION_SERVICE("PositionService");

    private String domainName;
    private JmxDomainType(String domainName) {
        this.domainName = domainName;
    }
    public String getDomainName() {
        return domainName;
    }
}
