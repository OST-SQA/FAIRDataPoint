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
package nl.dtls.fairdatapoint.acceptance.search.query.saved;

import nl.dtls.fairdatapoint.WebIntegrationTest;
import nl.dtls.fairdatapoint.api.dto.search.SearchQueryVariablesDTO;
import nl.dtls.fairdatapoint.api.dto.search.SearchSavedQueryChangeDTO;
import nl.dtls.fairdatapoint.api.dto.search.SearchSavedQueryDTO;
import nl.dtls.fairdatapoint.database.mongo.migration.development.search.SearchSavedQueryFixtures;
import nl.dtls.fairdatapoint.database.mongo.repository.SearchSavedQueryRepository;
import nl.dtls.fairdatapoint.entity.search.SearchSavedQuery;
import nl.dtls.fairdatapoint.entity.search.SearchSavedQueryType;
import nl.dtls.fairdatapoint.util.KnownUUIDs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("DELETE /search/query/saved/:uuid")
public class Detail_DELETE extends WebIntegrationTest {

    private URI url(String uuid) {
        return URI.create("/search/query/saved/" + uuid);
    }

    @Autowired
    private SearchSavedQueryRepository searchSavedQueryRepository;

    @Autowired
    private SearchSavedQueryFixtures searchSavedQueryFixtures;

    @Test
    @DisplayName("HTTP 403: anonymous user")
    public void res403_anonymousUser() {
        // GIVEN: prepare data
        searchSavedQueryRepository.deleteAll();
        SearchSavedQuery query = searchSavedQueryRepository.save(searchSavedQueryFixtures.savedQueryPublic01());

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .delete(url(query.getUuid()))
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
        assertThat(searchSavedQueryRepository.count(), is(equalTo(1L)));
    }

    @Test
    @DisplayName("HTTP 403: non-owner user")
    public void res403_nonOwnerUser() {
        // GIVEN: prepare data
        searchSavedQueryRepository.deleteAll();
        SearchSavedQuery query = searchSavedQueryRepository.save(searchSavedQueryFixtures.savedQueryPublic01());

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .delete(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, NIKOLA_TOKEN)
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
        assertThat(searchSavedQueryRepository.count(), is(equalTo(1L)));
    }

    @Test
    @DisplayName("HTTP 200: owner")
    public void res200_owner() {
        // GIVEN: prepare data
        searchSavedQueryRepository.deleteAll();
        SearchSavedQuery query = searchSavedQueryRepository.save(searchSavedQueryFixtures.savedQueryPublic01());

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .delete(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat(searchSavedQueryRepository.count(), is(equalTo(0L)));
    }

    @Test
    @DisplayName("HTTP 200: admin")
    public void res200_admin() {
        // GIVEN: prepare data
        searchSavedQueryRepository.deleteAll();
        SearchSavedQuery query = searchSavedQueryRepository.save(searchSavedQueryFixtures.savedQueryPublic01());

        // AND: prepare request
        RequestEntity<Void> request = RequestEntity
                .delete(url(query.getUuid()))
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .build();
        ParameterizedTypeReference<?> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<?> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
        assertThat(searchSavedQueryRepository.count(), is(equalTo(0L)));
    }
}
