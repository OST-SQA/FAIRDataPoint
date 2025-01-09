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
package nl.dtls.fairdatapoint.entity.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder(toBuilder = true)
public class ResourceDefinition {

    private static final String CATALOG_PREFIX = "catalog";

    @Id
    @JsonIgnore
    private ObjectId id;

    private String uuid;

    private String name;

    private String urlPrefix;

    private List<String> metadataSchemaUuids = new ArrayList<>();

    private List<ResourceDefinitionChild> children = new ArrayList<>();

    private List<ResourceDefinitionLink> externalLinks = new ArrayList<>();

    public ResourceDefinition(
            String uuid, String name, String urlPrefix, List<String> metadataSchemaUuids,
            List<ResourceDefinitionChild> children, List<ResourceDefinitionLink> externalLinks
    ) {
        this.uuid = uuid;
        this.name = name;
        this.urlPrefix = urlPrefix;
        this.metadataSchemaUuids = metadataSchemaUuids;
        this.children = children;
        this.externalLinks = externalLinks;
    }

    public boolean isRoot() {
        return urlPrefix.isEmpty();
    }

    public boolean isCatalog() {
        return urlPrefix.equals(CATALOG_PREFIX);
    }
}
