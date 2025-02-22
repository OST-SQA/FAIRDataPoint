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
package org.fairdatapoint.service.member;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.fairdatapoint.api.dto.member.MemberDTO;
import org.fairdatapoint.database.db.repository.MembershipRepository;
import org.fairdatapoint.database.db.repository.UserAccountRepository;
import org.fairdatapoint.entity.exception.ValidationException;
import org.fairdatapoint.entity.membership.Membership;
import org.fairdatapoint.entity.membership.MembershipPermission;
import org.fairdatapoint.entity.user.UserAccount;
import org.fairdatapoint.entity.user.UserRole;
import org.fairdatapoint.service.membership.PermissionService;
import org.fairdatapoint.service.user.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MutableAclService aclService;

    private final MembershipRepository membershipRepository;

    private final UserAccountRepository userAccountRepository;

    private final CurrentUserService currentUserService;

    private final PermissionService permissionService;

    private final MemberMapper memberMapper;

    private final AclCache aclCache;

    private final EntityManager entityManager;

    @Transactional
    @PreAuthorize("hasPermission(#entityId, #entityType.getName(), 'WRITE') or hasRole('ADMIN')")
    public <T> List<MemberDTO> getMembers(String entityId, Class<T> entityType) {
        final MutableAcl acl = retrieveAcl(entityId, entityType);
        return acl.getEntries()
                .stream()
                .collect(Collectors.groupingBy(AccessControlEntry::getSid,
                        Collectors.mapping(AccessControlEntry::getPermission, Collectors.toList())))
                .entrySet()
                .stream()
                .map(entry -> {
                    final Membership membership = deriveMembership(entry.getValue());
                    final UserAccount user = userAccountRepository.findByUuid(
                            UUID.fromString(((PrincipalSid) entry.getKey()).getPrincipal())
                    ).get();
                    return memberMapper.toDTO(user, membership);
                })
                .toList();
    }

    @Transactional
    public <T> Optional<MemberDTO> getMemberForCurrentUser(String entityId, Class<T> entityType) {
        final MutableAcl acl = retrieveAcl(entityId, entityType);
        final Optional<UserAccount> oUser = currentUserService.getCurrentUser();
        if (oUser.isEmpty()) {
            return Optional.empty();
        }
        final UserAccount user = oUser.get();
        final List<Permission> permissions = acl.getEntries()
                .stream()
                .filter(ace -> ace.getSid().equals(new PrincipalSid(user.getUuid().toString())))
                .map(AccessControlEntry::getPermission)
                .toList();

        if (!permissions.isEmpty()) {
            final Membership membership = deriveMembership(permissions);
            return Optional.of(memberMapper.toDTO(user, membership));
        }
        return Optional.empty();
    }

    @Transactional
    @PreAuthorize("hasPermission(#entityId, #entityType.getName(), 'WRITE') or hasRole('ADMIN')")
    public <T> MemberDTO createOrUpdateMember(String entityId, Class<T> entityType, UUID userUuid,
                                              UUID membershipUuid) {
        // Get membership
        final Optional<Membership> oMembership = membershipRepository.findByUuid(membershipUuid);
        if (oMembership.isEmpty()) {
            throw new ValidationException("Membership doesn't exist");
        }
        final Membership membership = oMembership.get();

        // Get user
        final Optional<UserAccount> oUser = userAccountRepository.findByUuid(userUuid);
        if (oUser.isEmpty()) {
            throw new ValidationException("User doesn't exist");
        }
        final UserAccount user = oUser.get();

        // Get ACL
        final MutableAcl acl = retrieveAcl(entityId, entityType);

        // Remove old user's ace
        deleteMember(entityId, entityType, userUuid);

        // Add new user's ace
        for (MembershipPermission membershipPermission : membership.getPermissions()) {
            final Permission permission = permissionService.getPermission(membershipPermission);
            insertAce(acl, userUuid, permission);
        }

        // Update database
        aclService.updateAcl(acl);

        return memberMapper.toDTO(user, membership);
    }

    @Transactional
    public <T> void createOwner(String entityId, Class<T> entityType, UUID userUuid) {
        createPermission(entityId, entityType, userUuid, BasePermission.WRITE);
        createPermission(entityId, entityType, userUuid, BasePermission.CREATE);
        createPermission(entityId, entityType, userUuid, BasePermission.DELETE);
        createPermission(entityId, entityType, userUuid, BasePermission.ADMINISTRATION);

        entityManager.flush();
    }

    @Transactional
    public <T> void createPermission(
            String entityId, Class<T> entityType, UUID userUuid, Permission permission
    ) {
        final MutableAcl acl = retrieveAcl(entityId, entityType);
        if (acl.getEntries().stream()
                .filter(ace -> ace.getPermission().getMask() == permission.getMask())
                .findAny()
                .isEmpty()) {
            insertAce(acl, userUuid, permission);
            aclService.updateAcl(acl);
        }
    }

    public boolean checkRole(UserRole role) {
        return currentUserService.getCurrentUser()
                .map(userAccount -> userAccount.getRole().equals(role))
                .orElse(false);
    }

    @Transactional
    public <T> boolean checkPermission(
            String entityId, Class<T> entityType, Permission permission
    ) {
        final Optional<UserAccount> oUser = currentUserService.getCurrentUser();
        if (oUser.isEmpty()) {
            return false;
        }
        final UserAccount user = oUser.get();

        final MutableAcl acl = retrieveAcl(entityId, entityType);
        return acl.getEntries()
                .stream()
                .filter(ace -> ((PrincipalSid) ace.getSid()).getPrincipal().equals(user.getUuid().toString()))
                .map(AccessControlEntry::getPermission)
                .anyMatch(permission2 -> permission2.getMask() == permission.getMask());
    }

    @Transactional
    public <T> void deleteMembers(UserAccount user) {
        final Long sid = (Long) entityManager
                .createNativeQuery("SELECT s.id FROM acl_sid s WHERE s.sid = :userUuid")
                .setParameter("userUuid", user.getUuid().toString())
                .getSingleResult();

        entityManager.createNativeQuery("DELETE FROM acl_entry WHERE sid = :sid")
                .setParameter("sid", sid)
                .executeUpdate();

        aclCache.clearCache();
    }

    @Transactional
    @PreAuthorize("hasPermission(#entityId, #entityType.getName(), 'WRITE') or hasRole('ADMIN')")
    public <T> void deleteMember(String entityId, Class<T> entityType, UUID userUuid) {
        // Get ACL
        final MutableAcl acl = retrieveAcl(entityId, entityType);

        for (int i = acl.getEntries().size() - 1; i >= 0; i--) {
            final AccessControlEntry ace = acl.getEntries().get(i);
            if (ace.getSid().equals(new PrincipalSid(userUuid.toString()))) {
                acl.deleteAce(i);
            }
        }
        aclService.updateAcl(acl);
        entityManager.flush();
    }

    @Transactional
    protected Membership deriveMembership(List<Permission> permissions) {
        final List<Membership> memberships = membershipRepository.findAll();
        for (Membership membership : memberships) {
            final List<MembershipPermission> membershipPermissions = membership.getPermissions();
            final List<Integer> mpMasks = membershipPermissions
                    .stream()
                    .map(MembershipPermission::getMask)
                    .sorted()
                    .toList();
            final List<Integer> pMasks = permissions
                    .stream()
                    .map(Permission::getMask)
                    .sorted()
                    .toList();
            if (mpMasks.equals(pMasks)) {
                return membership;
            }
        }
        throw new IllegalArgumentException("Non-existing combination of permissions");
    }

    @Transactional
    protected <T> MutableAcl retrieveAcl(String entityId, Class<T> entityType) {
        final ObjectIdentity identity = new ObjectIdentityImpl(entityType, entityId);
        try {
            return (MutableAcl) aclService.readAclById(identity);
        }
        catch (NotFoundException exception) {
            return aclService.createAcl(identity);
        }
    }

    private void insertAce(MutableAcl acl, UUID userUuid, Permission permission) {
        acl.insertAce(acl.getEntries().size(), permission, new PrincipalSid(userUuid.toString()), true);
    }
}
