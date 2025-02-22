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
package org.fairdatapoint.acceptance.search.query.saved;

import org.fairdatapoint.WebIntegrationTest;
import org.fairdatapoint.api.dto.search.SearchSavedQueryDTO;
import org.fairdatapoint.database.db.repository.SearchSavedQueryRepository;
import org.fairdatapoint.entity.search.SearchSavedQuery;
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
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("GET /search/query/saved/:uuid")
public class Detail_GET extends WebIntegrationTest {

    private URI url(UUID uuid) {
        return URI.create("/search/query/saved/" + uuid.toString());
    }

    @Autowired
    private SearchSavedQueryRepository searchSavedQueryRepository;

    @Test
    @DisplayName("HTTP 200: anonymous user, public query")
    public void res200_anonymousUserPublic() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PUBLIC_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .build();
        ParameterizedTypeReference<SearchSavedQueryDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<SearchSavedQueryDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(Objects.requireNonNull(result.getBody()).getUuid(), is(equalTo(query.getUuid())));
    }

    @Test
    @DisplayName("HTTP 404: anonymous user, internal query")
    public void res404_anonymousUserInternal() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_INTERNAL_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
    }

    @Test
    @DisplayName("HTTP 404: anonymous user, private query")
    public void res404_anonymousUserPrivate() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PRIVATE_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
    }

    @Test
    @DisplayName("HTTP 200: user, public query")
    public void res200_userPublic() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PUBLIC_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, NIKOLA_TOKEN)
                .build();
        ParameterizedTypeReference<SearchSavedQueryDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<SearchSavedQueryDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(Objects.requireNonNull(result.getBody()).getUuid(), is(equalTo(query.getUuid())));
    }

    @Test
    @DisplayName("HTTP 404: user, internal query")
    public void res404_userInternal() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_INTERNAL_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, NIKOLA_TOKEN)
                .build();
        ParameterizedTypeReference<SearchSavedQueryDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<SearchSavedQueryDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(Objects.requireNonNull(result.getBody()).getUuid(), is(equalTo(query.getUuid())));
    }

    @Test
    @DisplayName("HTTP 200: owner, private query")
    public void res200_ownerPrivate() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PRIVATE_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .build();
        ParameterizedTypeReference<SearchSavedQueryDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<SearchSavedQueryDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(Objects.requireNonNull(result.getBody()).getUuid(), is(equalTo(query.getUuid())));
    }

    @Test
    @DisplayName("HTTP 404: non-owner, private query")
    public void res404_nonOwnerPrivate() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PRIVATE_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, NIKOLA_TOKEN)
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NOT_FOUND)));
    }

    @Test
    @DisplayName("HTTP 200: admin, private query")
    public void res200_adminPrivate() {
        // GIVEN: prepare data
        SearchSavedQuery query = searchSavedQueryRepository.findByUuid(KnownUUIDs.SAVED_QUERY_PRIVATE_UUID).get();

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .get(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .build();
        ParameterizedTypeReference<SearchSavedQueryDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<SearchSavedQueryDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat(Objects.requireNonNull(result.getBody()).getUuid(), is(equalTo(query.getUuid())));
    }

}
