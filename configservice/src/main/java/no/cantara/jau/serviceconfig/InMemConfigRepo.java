package no.cantara.jau.serviceconfig;

import no.cantara.jau.serviceconfig.dto.DownloadItem;
import no.cantara.jau.serviceconfig.dto.MavenMetadata;
import no.cantara.jau.serviceconfig.dto.NexusUrlBuilder;
import no.cantara.jau.serviceconfig.dto.ServiceConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 2015-07-09.
 */
@Service
public class InMemConfigRepo implements ServiceConfigDao {
    private final Map<String, ServiceConfig> configs;
    private final Map<String, String> clientToConfigMapping;

    public InMemConfigRepo() {
        this.configs = new HashMap<>();
        this.clientToConfigMapping = new HashMap<>();
        addTestData();
    }

    @Override
    public ServiceConfig create(ServiceConfig newServiceConfig) {
        newServiceConfig.setId(UUID.randomUUID().toString());
        configs.put(newServiceConfig.getId(), newServiceConfig);
        return newServiceConfig;
    }

    public void update(ServiceConfig newServiceConfig) {
        configs.put(newServiceConfig.getId(), newServiceConfig);
    }

    public void addOrUpdateConfig(String clientId, ServiceConfig serviceConfig) {
        String serviceConfigId = serviceConfig.getId();
        if (serviceConfigId == null) {
            ServiceConfig persistedServiceConfig = create(serviceConfig);
            serviceConfigId = persistedServiceConfig.getId();
        } else {
            update(serviceConfig);
        }

        clientToConfigMapping.put(clientId, serviceConfigId);
    }

    //Should probably be moved to somewhere else.
    public ServiceConfig findConfig(String clientId) {
        String serviceConfigId = clientToConfigMapping.get(clientId);
        if (serviceConfigId == null) {
            return null;
        }
        return configs.get(serviceConfigId);
    }


    private void addTestData() {
        MavenMetadata metadata = new MavenMetadata("net.whydah.identity", "UserAdminService", "2.1-SNAPSHOT");
        String url = new NexusUrlBuilder("http://mvnrepo.cantara.no", "snapshots").build(metadata);
        DownloadItem downloadItem = new DownloadItem(url, null, null, metadata);

        ServiceConfig serviceConfig = new ServiceConfig("Service1-1.23");
        serviceConfig.addDownloadItem(downloadItem);
        serviceConfig.setStartServiceScript("java -DIAM_MODE=DEV -jar " + downloadItem.filename());
        addOrUpdateConfig("clientid1", serviceConfig);
    }
}
