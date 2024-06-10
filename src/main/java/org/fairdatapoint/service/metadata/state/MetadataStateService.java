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
package org.fairdatapoint.service.metadata.state;

import lombok.extern.slf4j.Slf4j;
import org.fairdatapoint.api.dto.metadata.MetaStateChangeDTO;
import org.fairdatapoint.api.dto.metadata.MetaStateDTO;
import org.fairdatapoint.database.rdf.repository.RepositoryMode;
import org.fairdatapoint.database.rdf.repository.common.MetadataRepository;
import org.fairdatapoint.database.rdf.repository.exception.MetadataRepositoryException;
import org.fairdatapoint.entity.exception.ValidationException;
import org.fairdatapoint.entity.metadata.MetadataState;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.entity.resource.ResourceDefinitionChild;
import org.fairdatapoint.service.metadata.common.MetadataService;
import org.fairdatapoint.service.metadata.exception.MetadataServiceException;
import org.fairdatapoint.service.user.CurrentUserService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fairdatapoint.util.RdfUtil.getObjectsBy;
import static org.fairdatapoint.util.ValueFactoryHelper.i;

@Slf4j
@Service
public class MetadataStateService {

    @Autowired
    @Qualifier("genericMetadataService")
    private MetadataService metadataService;

    @Autowired
    @Qualifier("genericMetadataRepository")
    private MetadataRepository metadataRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public boolean isDraft(IRI uri) throws MetadataServiceException {
        try {
            return !metadataRepository.find(uri, RepositoryMode.DRAFTS).isEmpty();
        }
        catch (MetadataRepositoryException exc) {
            throw new MetadataServiceException(exc.getMessage());
        }
    }

    public boolean isPublished(IRI uri) throws MetadataServiceException {
        try {
            return !metadataRepository.find(uri, RepositoryMode.MAIN).isEmpty();
        }
        catch (MetadataRepositoryException exc) {
            throw new MetadataServiceException(exc.getMessage());
        }
    }

    public MetadataState getState(IRI entityUri) throws MetadataServiceException {
        if (isDraft(entityUri)) {
            return MetadataState.DRAFT;
        }
        return MetadataState.PUBLISHED;
    }

    public MetaStateDTO getStateDTO(
            IRI entityUri, Model entity, ResourceDefinition definition
    ) throws MetadataServiceException {
        // 1. Return null if user is not logged in
        if (currentUserService.getCurrentUser().isEmpty()) {
            return null;
        }
        final Model model = metadataService.retrieve(entityUri, RepositoryMode.COMBINED);

        // 2. Get metadata info for current
        final MetaStateDTO result = new MetaStateDTO();
        result.setCurrent(getState(entityUri));

        // 3. Get metadata info for children
        // TODO: make querying more efficient (query published and drafts, in 2 queries)
        final List<String> childrenUris = new ArrayList<>();
        for (ResourceDefinitionChild rdChild : definition.getChildren()) {
            final IRI relationUri = i(rdChild.getRelationUri());
            for (org.eclipse.rdf4j.model.Value childUri : getObjectsBy(model, entityUri, relationUri)) {
                childrenUris.add(childUri.stringValue());
            }
        }
        final Map<String, MetadataState> children = new HashMap<>();
        childrenUris.forEach(childUri -> {
            try {
                children.put(childUri, getState(i(childUri)));
            }
            catch (MetadataServiceException exc) {
                log.warn("Failed to metadata check state: {} ({})", childUri, exc.getMessage());
            }
        });
        result.setChildren(children);
        return result;
    }

    public void modifyState(IRI entityUri, MetaStateChangeDTO reqDto)
            throws MetadataServiceException, ValidationException {
        try {
            if (isDraft(entityUri)) {
                if (reqDto.getCurrent().equals(MetadataState.PUBLISHED)) {
                    metadataRepository.moveToMain(entityUri);
                }
                else {
                    throw new ValidationException("You can not change state to DRAFT");
                }
            }
            else {
                if (reqDto.getCurrent().equals(MetadataState.DRAFT)) {
                    metadataRepository.moveToDrafts(entityUri);
                }
                else {
                    throw new ValidationException("Metadata is already published");
                }
            }
        }
        catch (MetadataRepositoryException exc) {
            throw new MetadataServiceException(exc.getMessage());
        }
    }
}
