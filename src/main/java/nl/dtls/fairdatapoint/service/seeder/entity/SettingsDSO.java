package nl.dtls.fairdatapoint.service.seeder.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import nl.dtls.fairdatapoint.api.dto.settings.*;

import java.util.List;

@Data
public class SettingsDSO {

    private String clientUrl;

    private String persistentUrl;

    @JsonInclude
    private String appTitle;

    @JsonInclude
    private String appSubtitle;

    private String appTitleFromConfig;

    private String appSubtitleFromConfig;

    private List<SettingsMetricDTO> metadataMetrics;

    private SettingsPingDTO ping;

    private SettingsRepositoryDTO mainRepository;

    private SettingsRepositoryDTO draftsRepository;

    private SettingsSearchDTO search;

    private SettingsFormsDTO forms;
}
