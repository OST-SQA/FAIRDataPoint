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
package nl.dtls.fairdatapoint.acceptance.metadata.repository;

import nl.dtls.fairdatapoint.WebIntegrationTest;
import nl.dtls.fairdatapoint.service.metadata.common.MetadataServiceException;
import nl.dtls.fairdatapoint.utils.MetadataFixtureLoader;
import org.junit.jupiter.api.BeforeEach;
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

@DisplayName("PUT /:repositoryId (RDF)")
public class Detail_PATCH_RDF extends WebIntegrationTest {

    @Autowired
    private MetadataFixtureLoader metadataFixtureLoader;

    private URI url() {
        return URI.create("/");
    }

    private String reqDto() {
        return "<> <http://purl.org/dc/terms/title> \"Test update\" .";
    }

    @BeforeEach
    public void setupExampleMetadata() throws MetadataServiceException {
        metadataFixtureLoader.storeExampleMetadata();
    }

    @Test
    @DisplayName("HTTP 204")
    public void res204() {
        // GIVEN:
        RequestEntity<String> request = RequestEntity
                .patch(url())
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, "text/turtle")
                .header(HttpHeaders.ACCEPT, "text/turtle")
                .body(reqDto());
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
    }

    @Test
    @DisplayName("HTTP 403")
    public void res403() {
        // GIVEN:
        RequestEntity<String> request = RequestEntity
                .patch(url())
                .header(HttpHeaders.AUTHORIZATION, ALBERT_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, "text/turtle")
                .header(HttpHeaders.ACCEPT, "text/turtle")
                .body(reqDto());
        ParameterizedTypeReference<Void> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<Void> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

}
