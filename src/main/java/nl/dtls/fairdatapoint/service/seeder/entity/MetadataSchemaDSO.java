package nl.dtls.fairdatapoint.service.seeder.entity;

import lombok.Data;
import nl.dtls.fairdatapoint.entity.schema.MetadataSchemaState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class MetadataSchemaDSO {

    private UUID uuid = UUID.randomUUID();

    private UUID versionUuid = UUID.randomUUID();

    private String name;

    private String version = "1.0.0";

    private String description = "";

    private Boolean abstractSchema = false;

    private MetadataSchemaState state = MetadataSchemaState.LATEST;

    private String definition;

    private String suggestedResourceName = null;

    private String suggestedUrlPrefix = null;

    private List<UUID> extendSchemaUuids = new ArrayList<>();
}
