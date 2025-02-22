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
package org.fairdatapoint.acceptance.metadata.distribution.meta;

import org.fairdatapoint.WebIntegrationTest;
import org.fairdatapoint.api.dto.member.MemberDTO;
import org.fairdatapoint.api.dto.metadata.MetaDTO;
import org.fairdatapoint.api.dto.metadata.MetaStateDTO;
import org.fairdatapoint.database.db.repository.MembershipRepository;
import org.fairdatapoint.database.db.repository.UserAccountRepository;
import org.fairdatapoint.entity.membership.Membership;
import org.fairdatapoint.entity.metadata.MetadataState;
import org.fairdatapoint.entity.user.UserAccount;
import org.fairdatapoint.service.member.MemberMapper;
import org.fairdatapoint.util.KnownUUIDs;
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

@DisplayName("GET /distribution/:distributionId/meta")
public class List_GET extends WebIntegrationTest {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private URI url(String id) {
        return URI.create(format("/distribution/%s/meta", id));
    }

    @Test
    @DisplayName("HTTP 200")
    public void res200() {
        // GIVEN:
        final UserAccount albert = userAccountRepository.findByUuid(KnownUUIDs.USER_ALBERT_UUID).get();
        final Membership owner = membershipRepository.findByUuid(KnownUUIDs.MEMBERSHIP_OWNER_UUID).get();
        RequestEntity<Void> request = RequestEntity
                .get(url("distribution-1"))
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .header(HttpHeaders.ACCEPT, "application/json")
                .build();
        ParameterizedTypeReference<MetaDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // AND: prepare expectation
        MemberDTO expMember = memberMapper.toDTO(albert, owner);

        // WHEN:
        ResponseEntity<MetaDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(result.getBody().getMember(), is(equalTo(expMember)));
        assertThat(result.getBody().getState(), is(equalTo(new MetaStateDTO(
                MetadataState.PUBLISHED,
                Map.of()
        ))));
    }

    @Test
    @DisplayName("HTTP 200: No user")
    public void res200_no_user() {
        // GIVEN:
        RequestEntity<Void> request = RequestEntity
                .get(url("distribution-1"))
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
