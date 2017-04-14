package ldap.sample.repo;

import javax.naming.ldap.LdapName;

import ldap.sample.domain.AdditionalInfo;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Repository;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

@Repository
public class UserLdapRepository implements IUserLdapRepository {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LdapHelper ldapHelper;

	@Autowired
	Util util;

	public void export() throws LDAPException{
		LDAPConnection lDAPConnection = new LDAPConnection("localhost", 10389);
		ldapHelper.sortedSearch("dc=ipos,dc=com", "(&(objectclass=*))", lDAPConnection, null);
	}

	public void search(int size) throws Exception{
		LDAPConnection lDAPConnection = new LDAPConnection("localhost", 10389);
		ldapHelper.search(lDAPConnection, "dc=ipos,dc=com", "(&(objectclass=*))", null, size);
	}
	
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
        ldapTemplate.unbind(util.buildUserDn(userName), true);
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
        LdapName userDn = util.buildUserDn(userName);
        if (util.isExist(userDn)) {
            return ldapHelper.findProfile(userName, profileId);
        }
        return null;
    }

    @Override
    public void deleteProfile(String userName, String profileId) {
        LdapName profileDn = util.buildProfileDn(userName, profileId);
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
        LdapName userDn = util.buildUserDn(userName);
        if (util.isExist(userDn)) {
            LdapName profileDn = util.buildProfileDn(userName, profileId);
            if (util.isExist(profileDn)) {
                return ldapHelper.findInsurerProfile(userName, profileId, insurerProfileId);
            }
        }
        return null;
    }

    @Override
    public void deleteInsurerProfile(String userName, String profileId, String insurerProfileId) {
        LdapName insurerProfileDn = util.buildInsurerProfileDn(userName, profileId, insurerProfileId);
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
        LdapName roleDn = util.buildRoleDn(roleCode);
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
        LdapName roleDn = util.buildRoleDn(roleCode);
        if (util.isExist(roleDn)) {
            return ldapHelper.findPermission(roleCode, permissionId);
        }
        return null;
    }

    @Override
    public void deletePermission(String roleCode, String permissionId) {
        LdapName permissionDn = util.buildPermissionDn(roleCode, permissionId);
        ldapTemplate.unbind(permissionDn);
    }

	@Override
	public void createAdi(String userName, String profileId,
			String insurerProfileId, AdditionalInfo adi) {
		ldapHelper.createAdditionalInfo(userName, profileId, insurerProfileId, adi);
		
	}

	@Override
	public void updateAdi(String userName, String profileId,
			String insurerProfileId, AdditionalInfo adi) {
		ldapHelper.updateAdditionalInfo(userName, profileId, insurerProfileId, adi);
		
	}

	@Override
	public AdditionalInfo findAdi(String userName, String profileId,
			String insurerProfileId, String adiId) {
		return ldapHelper.findAdditionalInfo(userName, profileId, insurerProfileId, adiId);
	}

	@Override
	public void deleteAdi(String userName, String profileId,
			String insurerProfileId, String adiId) {
		LdapName adiDn = util.buildAdditionalInfoDn(userName, profileId, insurerProfileId, adiId);
		ldapTemplate.unbind(adiDn);
	}

}
