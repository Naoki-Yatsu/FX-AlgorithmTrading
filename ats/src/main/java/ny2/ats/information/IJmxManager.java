package ny2.ats.information;

/**
 * JMXでのアクセスを管理するクラスです。
 */
public interface IJmxManager {

    /**
     * MBeanを登録します。
     * @param mbean
     * @param domainType
     * @param name
     */
    public void registerMBean(Object mbean, JmxDomainType domainType, String name);

    /**
     * MBeanの登録を解除します。
     * @param domainType
     * @param name
     */
    public void unregisterMBean(JmxDomainType domainType, String name);

}
