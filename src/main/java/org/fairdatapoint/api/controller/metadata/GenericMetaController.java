/**
 * The MIT License
 * Copyright © 2016-2024 FAIR Data Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.fairdatapoint.api.controller.metadata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fairdatapoint.api.dto.member.MemberDTO;
import org.fairdatapoint.api.dto.metadata.MetaDTO;
import org.fairdatapoint.api.dto.metadata.MetaPathDTO;
import org.fairdatapoint.api.dto.metadata.MetaStateChangeDTO;
import org.fairdatapoint.api.dto.metadata.MetaStateDTO;
import org.fairdatapoint.database.rdf.repository.RepositoryMode;
import org.fairdatapoint.entity.metadata.Metadata;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.entity.user.UserAccount;
import org.fairdatapoint.service.member.MemberService;
import org.fairdatapoint.service.metadata.common.MetadataService;
import org.fairdatapoint.service.metadata.exception.MetadataServiceException;
import org.fairdatapoint.service.metadata.factory.MetadataServiceFactory;
import org.fairdatapoint.service.metadata.state.MetadataStateService;
import org.fairdatapoint.service.resource.ResourceDefinitionService;
import org.fairdatapoint.service.user.CurrentUserService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.fairdatapoint.entity.metadata.MetadataGetter.getMetadataIdentifier;
import static org.fairdatapoint.entity.metadata.MetadataGetter.getTitle;
import static org.fairdatapoint.util.HttpUtil.getMetadataIRI;
import static org.fairdatapoint.util.RdfUtil.getStringObjectBy;
import static org.fairdatapoint.util.ValueFactoryHelper.i;

@Tag(name = "Metadata")
@RestController
@RequiredArgsConstructor
public class GenericMetaController {

    private static final int SUPPORTED_URL_FRAGMENTS = 3;

    private final String persistentUrl;

    private final MemberService memberService;

    private final MetadataServiceFactory metadataServiceFactory;

    private final MetadataStateService metadataStateService;

    private final ResourceDefinitionService resourceDefinitionService;

    private final CurrentUserService currentUserService;

    @Operation(hidden = true)
    @GetMapping(path = {"meta", "{oUrlPrefix:[^.]+}/{oRecordId:[^.]+}/meta"})
    public MetaDTO getMeta(
            @PathVariable final Optional<String> oUrlPrefix,
            @PathVariable final Optional<String> oRecordId
    ) throws MetadataServiceException {
        // 1. Init
        String urlPrefix = oUrlPrefix.orElse("");
        final String recordId = oRecordId.orElse("");
        final MetadataService metadataService =
                metadataServiceFactory.getMetadataServiceByUrlPrefix(urlPrefix);

        // 2. Get resource definition
        ResourceDefinition definition = resourceDefinitionService.getByUrlPrefix(urlPrefix);

        // 3. Get and check existence entity
        final Optional<UserAccount> oCurrentUser = currentUserService.getCurrentUser();
        final RepositoryMode mode = oCurrentUser.isEmpty() ? RepositoryMode.MAIN : RepositoryMode.COMBINED;
        IRI entityUri = getMetadataIRI(persistentUrl, urlPrefix, recordId);
        Model entity = metadataService.retrieve(entityUri, mode);

        // 4. Get member
        final String entityId = getMetadataIdentifier(entity).getIdentifier().getLabel();
        final Optional<MemberDTO> oMember =
                memberService.getMemberForCurrentUser(entityId, Metadata.class);
        final MemberDTO member = oMember.orElse(new MemberDTO(null, null));

        // 5. Get state
        final MetaStateDTO state = metadataStateService.getStateDTO(entityUri, entity, definition);

        // 6. Make path map
        final Map<String, MetaPathDTO> pathMap = new HashMap<>();
        while (true) {
            final MetaPathDTO entry = new MetaPathDTO();
            entry.setResourceDefinitionUuid(definition.getUuid().toString());
            entry.setTitle(getTitle(entity).stringValue());
            final IRI parentUri = i(getStringObjectBy(entity, entityUri, DCTERMS.IS_PART_OF));
            Optional.ofNullable(parentUri).map(IRI::toString).ifPresent(entry::setParent);
            pathMap.put(entityUri.toString(), entry);
            if (parentUri == null) {
                break;
            }
            entity = metadataService.retrieve(parentUri);
            entityUri = parentUri;
            urlPrefix = getResourceNameForList(parentUri.toString());
            definition = resourceDefinitionService.getByUrlPrefix(urlPrefix);
        }

        return new MetaDTO(member, state, pathMap);
    }

    @Operation(hidden = true)
    @PutMapping(path = {"meta/state", "{oUrlPrefix:[^.]+}/{oRecordId:[^.]+}/meta/state"})
    public MetaStateChangeDTO putMetaState(
            @PathVariable final Optional<String> oUrlPrefix,
            @PathVariable final Optional<String> oRecordId,
            @RequestBody @Valid MetaStateChangeDTO reqDto
    ) throws MetadataServiceException {
        // 1. Init
        final String urlPrefix = oUrlPrefix.orElse("");
        final String recordId = oRecordId.orElse("");
        final MetadataService metadataService =
                metadataServiceFactory.getMetadataServiceByUrlPrefix(urlPrefix);

        // 2. Get and check existence entity
        final IRI entityUri = getMetadataIRI(persistentUrl, urlPrefix, recordId);
        final Model model = metadataService.retrieve(entityUri);

        // 3. Get state
        metadataStateService.modifyState(entityUri, reqDto);

        return reqDto;
    }

    private String getResourceNameForList(String url) throws MetadataServiceException {
        final String fixedUrl = url
                .replace(persistentUrl, "")
                .replace("/meta", "")
                .replace("/state", "");

        final String[] parts = fixedUrl.split("/");
        if (parts.length == 1) {
            return "";
        }
        if (parts.length != SUPPORTED_URL_FRAGMENTS) {
            throw new MetadataServiceException("Unsupported URL");
        }
        return parts[1];
    }
}
