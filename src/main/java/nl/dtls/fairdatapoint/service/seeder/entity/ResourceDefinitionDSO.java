package nl.dtls.fairdatapoint.service.seeder.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import nl.dtls.fairdatapoint.api.dto.resource.ResourceDefinitionChildDTO;
import nl.dtls.fairdatapoint.api.dto.resource.ResourceDefinitionLinkDTO;

import java.util.List;
import java.util.UUID;

@Data
public class ResourceDefinitionDSO {

    @NotBlank
    private UUID uuid;

    @NotBlank
    private String name;

    @NotNull
    private String urlPrefix;

    @NotNull
    private List<UUID> metadataSchemaUuids;

    @NotNull
    private List<String> targetClassUris;

    @NotNull
    @Valid
    private List<ResourceDefinitionChildDTO> children;

    @NotNull
    @Valid
    private List<ResourceDefinitionLinkDTO> externalLinks;
}
