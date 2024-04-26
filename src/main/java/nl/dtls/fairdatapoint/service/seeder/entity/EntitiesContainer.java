package nl.dtls.fairdatapoint.service.seeder.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class EntitiesContainer {

    private final SettingsDSO settings = null;

    private final Map<UUID, UserDSO> users = new HashMap<>();

    private final Map<UUID, MetadataSchemaDSO> schemas = new HashMap<>();

    private final Map<UUID, ResourceDefinitionDSO> resourceDefinitions = new HashMap<>();
}
