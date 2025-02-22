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
package org.fairdatapoint.entity.settings;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.fairdatapoint.entity.base.BaseEntity;

@Entity(name = "SettingsAutocompleteSource")
@Table(name = "settings_autocomplete_source")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class SettingsAutocompleteSource extends BaseEntity {

    @NotNull
    @Column(name = "rdf_type", nullable = false)
    private String rdfType;

    @NotNull
    @Column(name = "sparql_endpoint", nullable = false)
    private String sparqlEndpoint;

    @NotNull
    @Column(name = "sparql_query", nullable = false)
    private String sparqlQuery;

    @NotNull
    @Column(name = "order_priority", nullable = false)
    private Integer orderPriority;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "settings_id", nullable = false)
    private Settings settings;
}
