package ldap.sample.repo;

import java.util.List;
import java.util.Set;

import javax.naming.ldap.LdapName;

import ldap.sample.constant.ConstantLdap;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Repository;

@Repository
public class UserLdapRepository {

	@Autowired
	LdapTemplate ldapTemplate;

	/*
	 * @Autowired UserRepo userRepo;
	 * 
	 * @Autowired ProfileRepo profileRepo;
	 * 
	 * @Autowired InsurerProfileRepo insurerProfileRepo;
	 * 
	 * @Autowired RoleRepo roleRepo;
	 * 
	 * @Autowired PermissionRepo permissionRepo;
	 */

	// Users
	public void createUser(User user) {
		ldapTemplate.create(user);

		Set<Profile> userProfiles = user.getProfiles();
		for (Profile profile : userProfiles) {
			profile.setUserCn(user.getUserName());
			ldapTemplate.create(profile);

			Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
			for (InsurerProfile insurerProfile : insurerProfiles) {
				insurerProfile.setUserCn(user.getUserName());
				insurerProfile.setProfileCn(profile.getProfileId());
				ldapTemplate.create(insurerProfile);
			}
		}
	}
	
	public void updateUser(User user) {
		String userName = user.getUserName();
		LdapName userDn = buildUserDn(userName);
		
		List<String> oldProfilesCn = ldapTemplate.list(userDn);
		
		Set<Profile> newUserProfiles = user.getProfiles();
		for (Profile profile : newUserProfiles) {
			profile.setUserCn(userName);

			Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
			for (InsurerProfile insurerProfile : insurerProfiles) {
				insurerProfile.setUserCn(userName);
				insurerProfile.setProfileCn(profile.getProfileId());
				ldapTemplate.update(insurerProfile);
			}
			
//			if (ldapTemplate.findByDn(profileDn, Profile.class) != null) {
//				ldapTemplate.update(profile);
//			}
		}
		
		if (ldapTemplate.findByDn(userDn, User.class) != null) {
			ldapTemplate.update(user);
		}
	}

	public User findUser(String userName) {
		LdapName userDn = buildUserDn(userName);
		User user = ldapTemplate.findByDn(userDn, User.class);

		List<String> profilesCn = ldapTemplate.listBindings(userDn);
		System.out.println(profilesCn);
		for (String profileCn : profilesCn) {
			LdapName profileDn = buildUserProfileDn(userName, profileCn);
			Profile profile = ldapTemplate.findByDn(profileDn, Profile.class);
			user.addProfile(profile);

			List<String> insurerProfiles = ldapTemplate.list(profileDn);
			for (String insurerProfile : insurerProfiles) {
				LdapName insurerProfileDn = buildUserProfileInsurerProfileDn(
						userName, profileCn, insurerProfile);
				profile.addInsurerProfile(ldapTemplate.findByDn(
						insurerProfileDn, InsurerProfile.class));
			}
		}
		return user;
	}

	// Role
	public void createRole(Role role) {
		ldapTemplate.create(role);

		Set<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			permission.setRoleCn(role.getRoleCode());
			ldapTemplate.create(permission);
		}
	}

	public void updateRole(Role role) {
		Set<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			permission.setRoleCn(role.getRoleCode());
			ldapTemplate.update(permission);
		}
		
		ldapTemplate.update(role);
	}

	public void deleteRole(Role role) {
		Set<Permission> permissions = role.getPermissions();
		for (Permission permission : permissions) {
			permission.setRoleCn(role.getRoleCode());
			ldapTemplate.delete(permission);
		}
		
		ldapTemplate.delete(role);
	}

	public Role findRole(String roleCode) {
		LdapName roleDn = buildRoleDn(roleCode);
		Role role = ldapTemplate.findByDn(roleDn, Role.class);

		List<String> permissionsCn = ldapTemplate.list(roleDn);
		for (String permissionCn : permissionsCn) {
			LdapName permissionDn = buildRolePermissionDn(roleCode,
					permissionCn);
			role.addPermission(ldapTemplate.findByDn(permissionDn,
					Permission.class));
		}
		return role;
	}

	private LdapName buildUserDn(String userName) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).build();
	}

	private LdapName buildUserProfileDn(String userName, String profileId) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName)
				.add("cn=" + profileId)
				.build();
	}

	private LdapName buildUserProfileInsurerProfileDn(String userName,
			String profileCn, String insurerProfileCn) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).add(profileCn).add(insurerProfileCn)
				.build();
	}

	private LdapName buildRoleDn(String roleCode) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).build();
	}

	private LdapName buildRolePermissionDn(String roleCode, String permissionCn) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).add(permissionCn).build();
	}

}
