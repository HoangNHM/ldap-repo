package ldap.sample.repo;

import ldap.sample.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Repository;

import javax.naming.ldap.LdapName;
import java.util.List;
import java.util.Set;

@Repository
public class UserLdapRepository implements IUserLdapRepository {

    @Autowired
    LdapTemplate ldapTemplate;

    @Autowired
    LdapHelper ldapHelper;

    // Users
    @Override
    public void createUser(User user) {
        ldapHelper.createUser(user);
    }

    @Override
    public void updateUser(User user) {
        ldapHelper.updateUser(user);
    }

    @Override
    public User findUser(String userName) {
        return ldapHelper.findUser(userName);
    }

    @Override
    public void deleteUser(String userName) {
        ldapTemplate.unbind(ldapHelper.buildUserDn(userName), true);
    }

    @Override
    public void createProfile(String userName, Profile profile) {
        ldapHelper.createProfile(userName, profile);
    }

    @Override
    public void updateProfile(String userName, Profile profile) {
        ldapHelper.updateProfile(userName, profile);
    }

    @Override
    public Profile findProfile(String userName, String profileId) {
        return ldapHelper.findProfile(userName, profileId);
    }

    @Override
    public void deleteProfile(String userName, String profileId) {
        LdapName profileDn = ldapHelper.buildProfileDn(userName, profileId);
        ldapTemplate.unbind(profileDn, true);
    }

    @Override
    public void createInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile) {
        ldapHelper.createInsurerProfile(userName, profileId, insurerProfile);
    }

    @Override
    public void updateInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile) {
        ldapHelper.updateInsurerProfile(userName, profileId, insurerProfile);
    }

    @Override
    public InsurerProfile findInsurerProfile(String userName, String profileId, String insurerProfileId) {
        return ldapHelper.findInsurerProfile(userName, profileId, insurerProfileId);
    }

    @Override
    public void deleteInsurerProfile(String userName, String profileId, String insurerProfileId) {
        LdapName insurerProfileDn = ldapHelper.buildInsurerProfileDn(userName, profileId, insurerProfileId);
        ldapTemplate.unbind(insurerProfileDn);
    }

    // Role
    @Override
    public void createRole(Role role) {
        ldapHelper.createRole(role);
    }

    @Override
    public void updateRole(Role role) {
        ldapHelper.updateRole(role);
    }

    @Override
    public Role findRole(String roleCode) {
        return ldapHelper.findRole(roleCode);
    }

    @Override
    public void deleteRole(String roleCode) {
        LdapName roleDn = ldapHelper.buildRoleDn(roleCode);
        ldapTemplate.unbind(roleDn, true);
    }

    @Override
    public void createPermission(String roleCode, Permission permission) {
        ldapHelper.createPermission(roleCode, permission);
    }

    @Override
    public void updatePermission(String roleCode, Permission permission) {
        ldapHelper.updatePermission(roleCode, permission);
    }

    @Override
    public Permission findPermission(String roleCode, String permissionId) {
        return ldapHelper.findPermission(roleCode, permissionId);
    }

    @Override
    public void deletePermission(String roleCode, String permissionId) {
        LdapName permissionDn = ldapHelper.buildPermissionDn(roleCode, permissionId);
        ldapTemplate.unbind(permissionDn);
    }


}
