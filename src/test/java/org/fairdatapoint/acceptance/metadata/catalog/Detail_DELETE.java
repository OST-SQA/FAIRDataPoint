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
package org.fairdatapoint.acceptance.metadata.catalog;

import org.fairdatapoint.WebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static java.lang.String.format;
import static org.fairdatapoint.acceptance.common.NotFoundTest.createAdminNotFoundTestDelete;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("DELETE /catalog/:catalogId")
public class Detail_DELETE extends WebIntegrationTest {

    private URI url(String id) {
        return URI.create(format("/catalog/%s", id));
    }

    @Test
    @DisplayName("HTTP 204")
    public void res200() {
        // GIVEN:
        RequestEntity<Void> request = RequestEntity
                .delete(url("catalog-1"))
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .header(HttpHeaders.ACCEPT, "text/turtle")
                .build();
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<String> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.NO_CONTENT)));
    }

    @Test
    @DisplayName("HTTP 403")
    public void res403() {
        // GIVEN:
        RequestEntity<Void> request = RequestEntity
                .delete(url("catalog-1"))
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, "text/turtle")
                .header(HttpHeaders.ACCEPT, "text/turtle")
                .build();
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

    @Test
    @DisplayName("HTTP 404")
    public void res404() {
        createAdminNotFoundTestDelete(client, url("nonExisting"));
    }

}
