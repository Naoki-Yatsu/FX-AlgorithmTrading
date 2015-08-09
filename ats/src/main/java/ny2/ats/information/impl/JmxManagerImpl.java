package ny2.ats.information.impl;

import javax.annotation.PostConstruct;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.stereotype.Service;

import com.udojava.jmx.wrapper.JMXBeanWrapper;

import ny2.ats.information.IJmxManager;
import ny2.ats.information.JmxDomainType;

@Service
public class JmxManagerImpl implements IJmxManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    private static final String OBJECT_NAME_KEY = ":name=";

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private MBeanExporter mbeanExporter;

    /** MBeanServer (from MBeanExporter) */
    private MBeanServer mbeanServer;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public JmxManagerImpl() {
    }

    @PostConstruct
    private void init() {
        logger.info("PostConstruct instance.");
        this.mbeanServer = mbeanExporter.getServer();
    }


    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void registerMBean(Object mbean, JmxDomainType domainType, String name) {
        try {
            ObjectName mbeanName = new ObjectName(domainType.getDomainName() + OBJECT_NAME_KEY + name);
            JMXBeanWrapper wrappedBean = new JMXBeanWrapper(mbean);
            mbeanServer.registerMBean(wrappedBean, mbeanName);

        } catch (Exception e) {
            logger.error("Error ing registering MBean.", e);
        }
    }

    @Override
    public void unregisterMBean(JmxDomainType domainType, String name) {
        try {
            ObjectName mbeanName = new ObjectName(domainType.getDomainName() + OBJECT_NAME_KEY + name);
            mbeanServer.unregisterMBean(mbeanName);
        } catch (MBeanRegistrationException | MalformedObjectNameException | InstanceNotFoundException e) {
            logger.error("", e);
        }
    }

}
