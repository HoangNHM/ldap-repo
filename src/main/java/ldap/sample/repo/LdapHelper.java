package ldap.sample.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import ldap.sample.constant.ConstantLdap;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

/**
 * Created by vantuegia on 3/26/2017.
 */
@Service
final class LdapHelper {

	@Autowired
	LdapTemplate ldapTemplate;

	boolean isExist(LdapName dnName) {
		String dn = dnName.toString();
		int split = dn.indexOf(",");
		String baseDn = dn.substring(split + 1);
		String cn = dn.substring(0, split);
		AttributesMapper attributesMapper = new AttributesMapper() {

			@Override
			public Object mapFromAttributes(Attributes attrs)
					throws NamingException {
				return attrs.get("cn").get();
			}
		};
		List<Object> list = ldapTemplate.search(baseDn, cn, attributesMapper);
		if (list.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	LdapName buildUserDn(String userName) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).build();
	}

	LdapName buildProfileDn(String userName, String profileId) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).add("cn=" + profileId).build();
	}

	LdapName buildInsurerProfileDn(String userName, String profileId,
			String insurerProfileId) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).add("cn=" + profileId)
				.add("cn=" + insurerProfileId).build();
	}

	LdapName buildRoleDn(String roleCode) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).build();
	}

	LdapName buildPermissionDn(String roleCode, String permissionId) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).add("cn=" + permissionId).build();
	}

	List<String> removePrefixCn(List<String> stringList) {
		List<String> result = new ArrayList<String>();
		for (String str : stringList) {
			if (str.startsWith("cn=")) {
				result.add(str.substring(3));
			}
		}
		return result;
	}

	void createInsurerProfile(String userName, String profileId,
			InsurerProfile insurerProfile) {
		insurerProfile.setUserCn(userName);
		insurerProfile.setProfileCn(profileId);
		ldapTemplate.create(insurerProfile);
	}

	void createProfile(String userName, Profile profile) {
		profile.setUserCn(userName);
		ldapTemplate.create(profile);

		Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
		for (InsurerProfile insurerProfile : insurerProfiles) {
			createInsurerProfile(userName, profile.getProfileId(),
					insurerProfile);
		}
	}

	void createUser(User user) {
		ldapTemplate.create(user);
		Set<Profile> userProfiles = user.getProfiles();
		for (Profile profile : userProfiles) {
			createProfile(user.getUserName(), profile);
		}
	}

	void updateInsurerProfile(String userName, String profileId,
			InsurerProfile insurerProfile) {
		insurerProfile.setUserCn(userName);
		insurerProfile.setProfileCn(profileId);
		LdapName insurerProfileDn = buildInsurerProfileDn(userName, profileId,
				insurerProfile.getInsurerId());
		if (isExist(insurerProfileDn)) {
			ldapTemplate.update(insurerProfile);
		} else {
			ldapTemplate.create(insurerProfile);
		}
	}

	void updateProfile(String userName, Profile profile) {
		profile.setUserCn(userName);
		LdapName profileDn = buildProfileDn(userName, profile.getProfileId());
		if (ldapTemplate.findByDn(profileDn, Profile.class) != null) {
			List<String> oldInsurerProfilesCn = removePrefixCn(ldapTemplate
					.list(profileDn));
			ldapTemplate.update(profile);

			Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
			for (InsurerProfile insurerProfile : insurerProfiles) {
				oldInsurerProfilesCn.remove(insurerProfile
						.getInsurerId());
				updateInsurerProfile(userName, profile.getProfileId(),
						insurerProfile);
			}

			for (String unUsedInsurerProfileId : oldInsurerProfilesCn) {
				LdapName unUsedInsurerProfileDn = buildInsurerProfileDn(
						userName, profile.getProfileId(),
						unUsedInsurerProfileId);
				ldapTemplate.unbind(unUsedInsurerProfileDn);
			}
		} else {
			createProfile(userName, profile);
		}

	}

	void updateUser(User user) {
		ldapTemplate.update(user);

		LdapName userDn = buildUserDn(user.getUserName());
		// Get list old profile base on userDn
		List<String> oldProfilesId = removePrefixCn(ldapTemplate.list(userDn));

		// Update current user profile
		Set<Profile> profiles = user.getProfiles();
		for (Profile profile : profiles) {
			oldProfilesId.remove(profile.getProfileId());
			updateProfile(user.getUserName(), profile);
		}

		// Delete unused (oldProfiles - curProfiles)user profile
		for (String unUsedProfileId : oldProfilesId) {
			LdapName unUsedProfileDn = buildProfileDn(user.getUserName(),
					unUsedProfileId);
			ldapTemplate.unbind(unUsedProfileDn);
		}
	}

	InsurerProfile findInsurerProfile(String userName, String profileId,
			String insurerProfileId) {
		LdapName insurerProfileDn = buildInsurerProfileDn(userName, profileId,
				insurerProfileId);
		if (isExist(insurerProfileDn)) {
			return ldapTemplate
					.findByDn(insurerProfileDn, InsurerProfile.class);
		} else {
			return null;
		}
	}

	Profile findProfile(String userName, String profileId) {
		LdapName profileDn = buildProfileDn(userName, profileId);
		if (isExist(profileDn)) {
			Profile profile = ldapTemplate.findByDn(profileDn, Profile.class);

			List<String> insurerProfilesId = removePrefixCn(ldapTemplate
					.list(profileDn));
			for (String insurerProfileId : insurerProfilesId) {
				profile.addInsurerProfile(findInsurerProfile(userName,
						profileId, insurerProfileId));
			}
			return profile;
		} else {
			return null;
		}
	}

	User findUser(String userName) {
		LdapName userDn = buildUserDn(userName);
		if (isExist(userDn)) {
			User user = ldapTemplate.findByDn(userDn, User.class);

			List<String> profilesId = removePrefixCn(ldapTemplate.list(userDn));
			for (String profileId : profilesId) {
				user.addProfile(findProfile(userName, profileId));
			}
			return user;
		} else {
			return null;
		}
	}

	void createPermission(String roleCode, Permission permission) {
		permission.setRoleCn(roleCode);
		ldapTemplate.create(permission);
	}

	void createRole(Role role) {
		ldapTemplate.create(role);
		Set<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			createPermission(role.getRoleCode(), permission);
		}
	}

	void updatePermission(String roleCode, Permission permission) {
		permission.setRoleCn(roleCode);
		LdapName permissionDn = buildPermissionDn(roleCode,
				permission.getPermissionId());
		if (isExist(permissionDn)) {
			ldapTemplate.update(permission);
		} else {
			ldapTemplate.create(permission);
		}
	}

	void updateRole(Role role) {
		ldapTemplate.update(role);

		LdapName roleDn = buildRoleDn(role.getRoleCode());
		// Get list old permission base on roleDn
		List<String> oldPermissionsId = removePrefixCn(ldapTemplate
				.list(roleDn));

		// Update current user Permissions
		Set<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			oldPermissionsId.remove(permission.getPermissionId());
			updatePermission(role.getRoleCode(), permission);
		}

		// Delete unused (oldPermissions - curPermissions) Role Permissions
		for (String unUsePermissionId : oldPermissionsId) {
			LdapName unUsedPermissionDn = buildPermissionDn(role.getRoleCode(),
					unUsePermissionId);
			ldapTemplate.unbind(unUsedPermissionDn);
		}
	}

	Permission findPermission(String roleCode, String permissionId) {
		LdapName permissionDn = buildPermissionDn(roleCode, permissionId);
		if (isExist(permissionDn)) {
			return ldapTemplate.findByDn(permissionDn, Permission.class);
		} else {
			return null;
		}
	}

	Role findRole(String roleCode) {
		LdapName roleDn = buildRoleDn(roleCode);
		if (isExist(roleDn)) {
			Role role = ldapTemplate.findByDn(roleDn, Role.class);
			List<String> permissionsId = removePrefixCn(ldapTemplate
					.list(roleDn));
			for (String permissionId : permissionsId) {
				role.addPermission(findPermission(roleCode, permissionId));
			}
			return role;
		} else {
			return null;
		}
	}

}
