package nl.dtls.fairdatapoint.service.seeder;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.dtls.fairdatapoint.config.properties.InstanceProperties;
import nl.dtls.fairdatapoint.service.seeder.entity.EntitiesContainer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntitySeeder {
    private static final String DSO_SCHEMA_VERSION = "1.0.0";

    private final InstanceProperties instanceProperties;

    public EntitiesContainer loadLocal() {
        final EntitiesContainer container = new EntitiesContainer();

        // TODO: list YAML/JSON files in directory
        // TODO: load files one by one and add elements

        return container;
    }
}
