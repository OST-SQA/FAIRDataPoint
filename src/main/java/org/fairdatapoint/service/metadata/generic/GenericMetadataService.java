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
package org.fairdatapoint.service.metadata.generic;

import org.fairdatapoint.entity.exception.ForbiddenException;
import org.fairdatapoint.entity.exception.ValidationException;
import org.fairdatapoint.entity.metadata.Metadata;
import org.fairdatapoint.entity.resource.ResourceDefinition;
import org.fairdatapoint.entity.user.UserRole;
import org.fairdatapoint.service.metadata.common.AbstractMetadataService;
import org.fairdatapoint.service.metadata.exception.MetadataServiceException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.fairdatapoint.entity.metadata.MetadataGetter.getParent;

@Service("genericMetadataService")
public class GenericMetadataService extends AbstractMetadataService {

    @Override
    public Model store(Model metadata, IRI uri, ResourceDefinition rd) throws MetadataServiceException {
        if (!rd.isRoot()) {
            // 1. Check permissions
            final String parentId = Optional.ofNullable(getParent(metadata))
                    .orElseThrow(() -> new ValidationException("Metadata has no parent")).stringValue();
            if (!(getMemberService().checkPermission(parentId, Metadata.class, BasePermission.CREATE)
                    || getMemberService().checkRole(UserRole.ADMIN))) {
                throw new ForbiddenException("You are not allow to add new entry");
            }
        }

        // 2. Store
        return super.store(metadata, uri, rd);
    }

}
