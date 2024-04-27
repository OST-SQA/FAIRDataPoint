/**
 * The MIT License
 * Copyright © 2017 DTL
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
package org.fairdatapoint.service.dashboard;

import org.fairdatapoint.api.dto.dashboard.DashboardItemDTO;
import org.fairdatapoint.api.dto.member.MemberDTO;
import org.fairdatapoint.api.dto.membership.MembershipDTO;
import org.fairdatapoint.entity.metadata.Metadata;
import org.fairdatapoint.entity.metadata.MetadataState;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.entity.resource.ResourceDefinitionChild;
import org.fairdatapoint.service.member.MemberService;
import org.fairdatapoint.service.metadata.common.MetadataService;
import org.fairdatapoint.service.metadata.exception.MetadataServiceException;
import org.fairdatapoint.service.metadata.state.MetadataStateService;
import org.fairdatapoint.service.resource.ResourceDefinitionCache;
import org.fairdatapoint.service.resource.ResourceDefinitionService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.fairdatapoint.entity.metadata.MetadataGetter.getTitle;
import static org.fairdatapoint.util.RdfUtil.getObjectsBy;
import static org.fairdatapoint.util.ValueFactoryHelper.i;

@Service
public class DashboardService {

    @Autowired
    @Qualifier("genericMetadataService")
    private MetadataService metadataService;

    @Autowired
    private MetadataStateService metadataStateService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ResourceDefinitionService resourceDefinitionService;

    @Autowired
    private ResourceDefinitionCache resourceDefinitionCache;

    public List<DashboardItemDTO> getDashboard(IRI repositoryUri) throws MetadataServiceException {
        final ResourceDefinition resourceDefinition = resourceDefinitionService.getByUrlPrefix("");
        final Model repository = metadataService.retrieve(repositoryUri);
        return getDashboardItem(repositoryUri, repository, resourceDefinition).getChildren();
    }

    private DashboardItemDTO getDashboardItem(
            IRI metadataUri, Model model, ResourceDefinition resourceDefinition
    ) throws MetadataServiceException {
        final List<DashboardItemDTO> children = new ArrayList<>();
        for (ResourceDefinitionChild rdChild : resourceDefinition.getChildren()) {
            final IRI relationUri = i(rdChild.getRelationUri());
            for (Value childUri : getObjectsBy(model, metadataUri, relationUri)) {
                final IRI childIri = i(childUri.stringValue());
                final DashboardItemDTO child = getDashboardItem(
                        childIri,
                        metadataService.retrieve(childIri),
                        resourceDefinitionCache.getByUuid(rdChild.getTarget().getUuid())
                );
                children.add(child);
            }
        }

        final Optional<MemberDTO> member =
                memberService.getMemberForCurrentUser(metadataUri.stringValue(), Metadata.class);
        final Optional<MembershipDTO> membership = member.map(MemberDTO::getMembership);
        final MetadataState state = metadataStateService.getState(metadataUri);
        return new DashboardItemDTO(
                metadataUri.toString(),
                getTitle(model).getLabel(),
                children
                        .stream()
                        .filter(this::childOnDashboard)
                        .toList(),
                membership,
                state
        );
    }

    private boolean childOnDashboard(DashboardItemDTO item) {
        return item.getMembership().isPresent() || !item.getChildren().isEmpty();
    }
}
