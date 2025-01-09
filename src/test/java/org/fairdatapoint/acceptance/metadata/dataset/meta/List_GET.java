/**
 * The MIT License
 * Copyright © 2016-2025 FAIR Data Team
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
package org.fairdatapoint.acceptance.metadata.dataset.meta;

import org.fairdatapoint.WebIntegrationTest;
import org.fairdatapoint.api.dto.member.MemberDTO;
import org.fairdatapoint.api.dto.metadata.MetaDTO;
import org.fairdatapoint.api.dto.metadata.MetaStateDTO;
import org.fairdatapoint.database.mongo.migration.development.membership.data.MembershipFixtures;
import org.fairdatapoint.database.mongo.migration.development.metadata.data.MetadataFixtures;
import org.fairdatapoint.database.mongo.migration.development.user.data.UserFixtures;
import org.fairdatapoint.service.member.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

import static java.lang.String.format;
import static org.fairdatapoint.acceptance.common.NotFoundTest.createUserNotFoundTestGetRDF;
import static org.fairdatapoint.acceptance.metadata.Common.assertEmptyMember;
import static org.fairdatapoint.acceptance.metadata.Common.assertEmptyState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("GET /dataset/:datasetId/meta")
public class List_GET extends WebIntegrationTest {

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private MembershipFixtures membershipFixtures;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MetadataFixtures metadataFixtures;

    private URI url(String id) {
        return URI.create(format("/dataset/%s/meta", id));
    }

    @Test
    @DisplayName("HTTP 200")
    public void res200() {
        // GIVEN:
        RequestEntity<Void> request = RequestEntity
                .get(url("dataset-1"))
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .header(HttpHeaders.ACCEPT, "application/json")
                .build();
        ParameterizedTypeReference<MetaDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // AND: prepare expectation
        MemberDTO expMember = memberMapper.toDTO(userFixtures.albert(), membershipFixtures.owner());

        // WHEN:
        ResponseEntity<MetaDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(result.getBody().getMember(), is(equalTo(expMember)));
        assertThat(result.getBody().getState(), is(equalTo(new MetaStateDTO(
                metadataFixtures.dataset1().getState(),
                Map.of(
                        metadataFixtures.distribution1().getUri(), metadataFixtures.distribution1().getState(),
                        metadataFixtures.distribution2().getUri(), metadataFixtures.distribution2().getState()
                )
        ))));
    }

    @Test
    @DisplayName("HTTP 200: No user")
    public void res200_no_user() {
        // GIVEN:
        RequestEntity<Void> request = RequestEntity
                .get(url("dataset-1"))
                .header(HttpHeaders.ACCEPT, "application/json")
                .build();
        ParameterizedTypeReference<MetaDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<MetaDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertEmptyMember(result.getBody().getMember());
        assertEmptyState(result.getBody().getState());
    }

    @Test
    @DisplayName("HTTP 404")
    public void res404() {
        createUserNotFoundTestGetRDF(client, url("nonExisting"));
    }

}
