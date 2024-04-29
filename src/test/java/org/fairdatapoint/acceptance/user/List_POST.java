/**
 * The MIT License
 * Copyright © 2024 FAIR Data Team
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
package org.fairdatapoint.acceptance.user;

import org.fairdatapoint.WebIntegrationTest;
import org.fairdatapoint.api.dto.error.ErrorDTO;
import org.fairdatapoint.api.dto.user.UserCreateDTO;
import org.fairdatapoint.api.dto.user.UserDTO;
import org.fairdatapoint.database.db.repository.UserAccountRepository;
import org.fairdatapoint.entity.user.UserAccount;
import org.fairdatapoint.util.KnownUUIDs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.net.URI;

import static java.lang.String.format;
import static org.fairdatapoint.acceptance.common.ForbiddenTest.createNoUserForbiddenTestPost;
import static org.fairdatapoint.acceptance.common.ForbiddenTest.createUserForbiddenTestPost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("POST /users")
public class List_POST extends WebIntegrationTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    private URI url() {
        return URI.create("/users");
    }

    private UserCreateDTO reqDto() {
        UserAccount user = userAccountRepository.findByUuid(KnownUUIDs.USER_ISAAC_UUID).get();
        return UserCreateDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email("isaac2@example.com")
                .password("password")
                .role(user.getRole())
                .build();
    }

    @Test
    @DisplayName("HTTP 200")
    public void res200() {
        // GIVEN:
        final UserCreateDTO reqDto = reqDto();
        RequestEntity<UserCreateDTO> request = RequestEntity
                .post(url())
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<UserDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<UserDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.OK)));
        Common.compare(reqDto, result.getBody());
    }

    @Test
    @DisplayName("HTTP 400: Email already exists")
    public void res400_emailAlreadyExists() {
        // GIVEN:
        UserAccount user = userAccountRepository.findByUuid(KnownUUIDs.USER_ALBERT_UUID).get();
        UserCreateDTO reqDto = UserCreateDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .password("password")
                .role(user.getRole())
                .build();
        RequestEntity<UserCreateDTO> request = RequestEntity
                .post(url())
                .header(HttpHeaders.AUTHORIZATION, ADMIN_TOKEN)
                .accept(MediaType.APPLICATION_JSON)
                .body(reqDto);
        ParameterizedTypeReference<ErrorDTO> responseType = new ParameterizedTypeReference<>() {
        };

        // WHEN:
        ResponseEntity<ErrorDTO> result = client.exchange(request, responseType);

        // THEN:
        assertThat(result.getStatusCode(), is(equalTo(HttpStatus.BAD_REQUEST)));
        assertThat(result.getBody().getMessage(), is(format("Email '%s' is already taken", reqDto.getEmail())));
    }

    @Test
    @DisplayName("HTTP 403: User is not authenticated")
    public void res403_notAuthenticated() {
        createNoUserForbiddenTestPost(client, url(), reqDto());
    }

    @Test
    @DisplayName("HTTP 403: User is not an admin")
    public void res403_user() {
        createUserForbiddenTestPost(client, url(), reqDto());
    }

}
