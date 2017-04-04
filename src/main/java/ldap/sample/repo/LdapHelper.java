package ldap.sample.repo;

import java.util.List;
import java.util.Set;

import javax.naming.ldap.LdapName;

import ldap.sample.domain.AdditionalInfo;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

@Service
final class LdapHelper {

	@Autowired
	LdapTemplate ldapTemplate;
	
	@Autowired
	Util util;

	void createAdditionalInfo(String userName, String profileId,
			String insurerProfileId, AdditionalInfo adi) {
		adi.setUserCn(userName);
		adi.setProfileCn(profileId);
		adi.setInsurerCn(insurerProfileId);
		ldapTemplate.create(adi);
	}

	void createInsurerProfile(String userName, String profileId,
			InsurerProfile insurerProfile) {
		insurerProfile.setUserCn(userName);
		insurerProfile.setProfileCn(profileId);
		ldapTemplate.create(insurerProfile);

		AdditionalInfo adi = insurerProfile.getAdditionalInfo();
		if (adi != null) {
			createAdditionalInfo(userName, profileId,
					insurerProfile.getInsurerId(), adi);
		}
	}

	void createProfile(String userName, Profile profile) {
		profile.setUserCn(userName);
		ldapTemplate.create(profile);

		Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
		assert !insurerProfiles.isEmpty() : "User " + userName + ", profile "
				+ profile + ", Must have InsurerProfile";
		for (InsurerProfile insurerProfile : insurerProfiles) {
			createInsurerProfile(userName, profile.getProfileId(),
					insurerProfile);
		}
	}

	void createUser(User user) {
		ldapTemplate.create(user);
		Profile userProfile = user.getProfile();
		if (userProfile != null) {
			createProfile(user.getUserName(), userProfile);
		}
	}

	void updateAdditionalInfo(String userName, String profileId,
			String insurerProfileId, AdditionalInfo adi) {
		adi.setUserCn(userName);
		adi.setProfileCn(profileId);
		adi.setInsurerCn(insurerProfileId);
		LdapName adiDn = util.buildAdditionalInfoDn(userName, profileId,
				insurerProfileId, adi.getAdditionalInfoId());
		if (util.isExist(adiDn)) {
			ldapTemplate.update(adi);
		} else {
			ldapTemplate.create(adi);
		}
	}

	void updateInsurerProfile(String userName, String profileId,
			InsurerProfile insurerProfile) {
		insurerProfile.setUserCn(userName);
		insurerProfile.setProfileCn(profileId);
		LdapName insurerProfileDn = util.buildInsurerProfileDn(userName, profileId,
				insurerProfile.getInsurerId());
		if (util.isExist(insurerProfileDn)) {
			List<String> oldAdiCn = util.removePrefixCn(ldapTemplate
					.list(insurerProfileDn));

			ldapTemplate.update(insurerProfile);
			AdditionalInfo adi = insurerProfile.getAdditionalInfo();
			if (adi != null) {
				oldAdiCn.remove(adi.getAdditionalInfoId());
				updateAdditionalInfo(userName, profileId,
						insurerProfile.getInsurerId(), adi);
			}

			for (String unUsedAdiId : oldAdiCn) {
				LdapName unUsedAdiDn = util.buildAdditionalInfoDn(userName,
						profileId, insurerProfile.getInsurerId(), unUsedAdiId);
				ldapTemplate.unbind(unUsedAdiDn);
			}

		} else {
			createInsurerProfile(userName, profileId, insurerProfile);
		}
	}

	void updateProfile(String userName, Profile profile) {
		profile.setUserCn(userName);
		LdapName profileDn = util.buildProfileDn(userName, profile.getProfileId());
		if (util.isExist(profileDn)) {
			List<String> oldInsurerProfilesCn = util.removePrefixCn(ldapTemplate
					.list(profileDn));
			ldapTemplate.update(profile);

			Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
			assert !insurerProfiles.isEmpty() : "User " + userName
					+ ", profile " + profile.getProfileId() + ", Must have InsurerProfile";
			for (InsurerProfile insurerProfile : insurerProfiles) {
				oldInsurerProfilesCn.remove(insurerProfile.getInsurerId());
				updateInsurerProfile(userName, profile.getProfileId(),
						insurerProfile);
			}

			for (String unUsedInsurerProfileId : oldInsurerProfilesCn) {
				LdapName unUsedInsurerProfileDn = util.buildInsurerProfileDn(
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

		LdapName userDn = util.buildUserDn(user.getUserName());
		// Get list old profile base on userDn
		List<String> oldProfilesId = util.removePrefixCn(ldapTemplate.list(userDn));

		// Update current user profile
		Profile profile = user.getProfile();
		if (profile != null) {
			oldProfilesId.remove(profile.getProfileId());
			updateProfile(user.getUserName(), profile);
		}

		// Delete unused (oldProfiles - curProfiles)user profile
		for (String unUsedProfileId : oldProfilesId) {
			LdapName unUsedProfileDn = util.buildProfileDn(user.getUserName(),
					unUsedProfileId);
			ldapTemplate.unbind(unUsedProfileDn);
		}
	}

	AdditionalInfo findAdditionalInfo(String userName, String profileId,
			String insurerProfileId, String adiId) {
		LdapName adiDn = util.buildAdditionalInfoDn(userName, profileId,
				insurerProfileId, adiId);
		if (util.isExist(adiDn)) {
			return ldapTemplate.findByDn(adiDn, AdditionalInfo.class);
		} else {
			return null;
		}
	}

	InsurerProfile findInsurerProfile(String userName, String profileId,
			String insurerProfileId) {
		LdapName insurerProfileDn = util.buildInsurerProfileDn(userName, profileId,
				insurerProfileId);
		if (util.isExist(insurerProfileDn)) {
			InsurerProfile insurerProfile = ldapTemplate.findByDn(
					insurerProfileDn, InsurerProfile.class);

			List<String> adisId = util.removePrefixCn(ldapTemplate
					.list(insurerProfileDn));
			
			assert adisId.size() < 2 : "User " + userName + ", profile "
					+ profileId + ", insurerProfile " + insurerProfileId
					+ ", Multiple AdditionalInfo is invalid";
			for (String adiId : adisId) {
				insurerProfile.setAdditionalInfo(findAdditionalInfo(userName,
						profileId, insurerProfileId, adiId));
			}
			return insurerProfile;
		} else {
			return null;
		}
	}

	Profile findProfile(String userName, String profileId) {
		LdapName profileDn = util.buildProfileDn(userName, profileId);
		if (util.isExist(profileDn)) {
			Profile profile = ldapTemplate.findByDn(profileDn, Profile.class);

			List<String> insurerProfilesId = util.removePrefixCn(ldapTemplate
					.list(profileDn));
			assert !insurerProfilesId.isEmpty() : "User " + userName
			+ ", profile " + profileId + ", Must have InsurerProfile";
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
		LdapName userDn = util.buildUserDn(userName);
		if (util.isExist(userDn)) {
			User user = ldapTemplate.findByDn(userDn, User.class);

			List<String> profilesId = util.removePrefixCn(ldapTemplate.list(userDn));
			assert profilesId.size() < 2 : "User " + userName
					+ ", Multiple profile is invalid";
			for (String profileId : profilesId) {
				user.setProfile(findProfile(userName, profileId));
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
		assert !permissions.isEmpty() : "Role " + role.getRoleCode() + ", Must have Permissions";
		for (Permission permission : permissions) {
			createPermission(role.getRoleCode(), permission);
		}
	}

	void updatePermission(String roleCode, Permission permission) {
		permission.setRoleCn(roleCode);
		LdapName permissionDn = util.buildPermissionDn(roleCode,
				permission.getPermissionId());
		if (util.isExist(permissionDn)) {
			ldapTemplate.update(permission);
		} else {
			ldapTemplate.create(permission);
		}
	}

	void updateRole(Role role) {
		ldapTemplate.update(role);

		LdapName roleDn = util.buildRoleDn(role.getRoleCode());
		// Get list old permission base on roleDn
		List<String> oldPermissionsId = util.removePrefixCn(ldapTemplate
				.list(roleDn));

		// Update current user Permissions
		Set<Permission> permissions = role.getPermissions();
		assert !permissions.isEmpty() : "Role " + role.getRoleCode() + ", Must have Permissions";
		for (Permission permission : permissions) {
			oldPermissionsId.remove(permission.getPermissionId());
			updatePermission(role.getRoleCode(), permission);
		}

		// Delete unused (oldPermissions - curPermissions) Role Permissions
		for (String unUsePermissionId : oldPermissionsId) {
			LdapName unUsedPermissionDn = util.buildPermissionDn(role.getRoleCode(),
					unUsePermissionId);
			ldapTemplate.unbind(unUsedPermissionDn);
		}
	}

	Permission findPermission(String roleCode, String permissionId) {
		LdapName permissionDn = util.buildPermissionDn(roleCode, permissionId);
		if (util.isExist(permissionDn)) {
			return ldapTemplate.findByDn(permissionDn, Permission.class);
		} else {
			return null;
		}
	}

	Role findRole(String roleCode) {
		LdapName roleDn = util.buildRoleDn(roleCode);
		if (util.isExist(roleDn)) {
			Role role = ldapTemplate.findByDn(roleDn, Role.class);
			List<String> permissionsId = util.removePrefixCn(ldapTemplate
					.list(roleDn));
			assert !permissionsId.isEmpty() : "Role " + roleCode + ", Must have Permissions";
			for (String permissionId : permissionsId) {
				role.addPermission(findPermission(roleCode, permissionId));
			}
			return role;
		} else {
			return null;
		}
	}

}
