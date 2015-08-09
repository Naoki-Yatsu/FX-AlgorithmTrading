package ny2.ats.information.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import ny2.ats.core.event.SystemInformationEvent;
import ny2.ats.core.router.IEventRouter;
import ny2.ats.information.IInformationManager;

@Service
@ManagedResource(objectName = "InformationService:name=InformationManager")
public class InformationManagerImpl implements IInformationManager {

    // //////////////////////////////////////
    // Field
    // //////////////////////////////////////

    // Logger
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private IEventRouter eventRouter;

    @Autowired
    private ApplicationContext applicationContext;

    // バージョン情報(MINIFESTから取得)
    private static final String APP_TITLE = "algorithm-trading-system";
    private String version;
    private String buildTime;

    // //////////////////////////////////////
    // Constructor
    // //////////////////////////////////////

    public InformationManagerImpl() {
        readManifest();
    }

    // //////////////////////////////////////
    // Method
    // //////////////////////////////////////

    @Override
    public void sendEvent(SystemInformationEvent informationEvent) {
        logger.info("Send Information : {}", informationEvent.getContent().toStringSummary());
        eventRouter.addEvent(informationEvent);
    }

    /**
     * MINIFEST.MF からバージョン情報を読み取ります
     */
    private void readManifest() {
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                Attributes attributes = manifest.getMainAttributes();
                String title = attributes.getValue("Implementation-Title");
                if (APP_TITLE.equals(title)) {
                    version = attributes.getValue("Implementation-Version");
                    buildTime = attributes.getValue("Build-Time");
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    // //////////////////////////////////////
    // Method - JMX
    // //////////////////////////////////////

    /**
     * BeanDefinitionNamesを表示します
     *
     * @return
     */
    @ManagedOperation
    public String showBeanDefinition() {
        StringBuilder sb = new StringBuilder("[BeanDefinition]\n");
        for (String name : applicationContext.getBeanDefinitionNames()) {
            sb.append(name).append("\n");
        }
        return sb.toString();
    }

    /**
     * BeanDefinitionNamesを表示します
     *
     * @return
     */
    @ManagedOperation
    public String showPropertySources() {
        StringBuilder sb = new StringBuilder("[PropertySources]\n");
        MutablePropertySources propertySources = (MutablePropertySources) applicationContext.getBean(PropertySourcesPlaceholderConfigurer.class).getAppliedPropertySources();
        Iterator<PropertySource<?>> iterator = propertySources.iterator();
        while(iterator.hasNext()) {
            PropertySource<?> source = iterator.next();
            if (source instanceof PropertiesPropertySource) {
                // PropertiesPropertySource
                // sort for display
                Map<String, Object> propertyMap = new TreeMap<>();
                for (Entry<String, Object> entry : ((PropertiesPropertySource) source).getSource().entrySet()) {
                    propertyMap.put(entry.getKey(), entry.getValue());
                }
                for (Entry<String, Object> entry : propertyMap.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
                }
            } else {
                // environmentProperties
                sb.append(source.getName()).append("=").append(source.getSource().toString()).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * アプリケーションのバージョンを表示します
     *
     * @return
     */
    @ManagedOperation
    public String showAppVersion() {
        StringBuilder sb = new StringBuilder("[AppVersion]\n");
        sb.append("title : ").append(APP_TITLE).append("\n");
        sb.append("version : ").append(version).append("\n");
        sb.append("build-time : ").append(buildTime).append("\n");
        return sb.toString();
    }

}
