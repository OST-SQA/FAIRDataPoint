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
package org.fairdatapoint.acceptance.resource;

import org.fairdatapoint.WebIntegrationTest;
import org.fairdatapoint.api.dto.resource.ResourceDefinitionChangeDTO;
import org.fairdatapoint.api.dto.resource.ResourceDefinitionDTO;
import org.fairdatapoint.database.db.repository.ResourceDefinitionRepository;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.service.resource.ResourceDefinitionMapper;
import org.fairdatapoint.util.KnownUUIDs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.net.URI;
import java.util.UUID;

import static java.lang.String.format;
import static org.fairdatapoint.acceptance.common.ForbiddenTest.createNoUserForbiddenTestPut;
import static org.fairdatapoint.acceptance.common.ForbiddenTest.createUserForbiddenTestPut;
import static org.fairdatapoint.acceptance.common.NotFoundTest.createAdminNotFoundTestPut;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("PUT /resource-definitions/:resourceDefinitionUuid")
public class Detail_PUT extends WebIntegrationTest {

    @Autowired
    private ResourceDefinitionRepository resourceDefinitionRepository;

    @Autowired
    private ResourceDefinitionMapper resourceDefinitionMapper;

    private URI url(UUID uuid) {
        return URI.create(format("/resource-definitions/%s", uuid));
    }

    private ResourceDefinitionChangeDTO reqDto(ResourceDefinition rd) {
        rd.setName(format("EDITED: %s", rd.getName()));
        rd.setUrlPrefix(format("%s-edited", rd.getUrlPrefix()));
        return resourceDefinitionMapper.toChangeDTO(rd);
    }

    @Test
    @DisplayName("HTTP 200")
    public void res200() {
        // GIVEN: Prepare data
        ResourceDefinition resourceDefinition = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        ResourceDefinitionChangeDTO reqDto = reqDto(resourceDefinition);

        // AND: Prepare request
        RequestEntity<ResourceDefinitionChangeDTO> request = RequestEntity
                .put(url(resourceDefinition.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<ResourceDefinitionDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<ResourceDefinitionDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(result.getBody().getName(), is(equalTo(reqDto.getName())));
        assertThat(result.getBody().getUrlPrefix(), is(equalTo(reqDto.getUrlPrefix())));
    }

    @Test
    @DisplayName("HTTP 403: ResourceDefinition is not authenticated")
    public void res403_notAuthenticated() {
        ResourceDefinition resourceDefinition = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        createNoUserForbiddenTestPut(client, url(resourceDefinition.getUuid()), reqDto(resourceDefinition));
    }

    @Test
    @DisplayName("HTTP 403: ResourceDefinition is not an admin")
    public void res403_resourceDefinition() {
        ResourceDefinition resourceDefinition = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        createUserForbiddenTestPut(client, url(resourceDefinition.getUuid()), reqDto(resourceDefinition));
    }

    @Test
    @DisplayName("HTTP 404")
    public void res404() {
        ResourceDefinition resourceDefinition = resourceDefinitionRepository.findByUuid(KnownUUIDs.RD_DISTRIBUTION_UUID).get();
        createAdminNotFoundTestPut(client, url(KnownUUIDs.NULL_UUID), reqDto(resourceDefinition));
    }

}
