package ldap.sample.repo;

import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

public interface IUserLdapRepository {
	
	void createUser(User user);
	void updateUser(User user);
	User findUser(String userName);
	void deleteUser(String userName);
	
	void createUserProfile(String userName, Profile profile);
	void updateUserProfile(String userName, Profile profile);
	Profile findUserProfile(String userName, String profileId);
	void deleteUserProfile(String userName, String profileId);
	
	void createUserProfileInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile);
	void updateUserProfileInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile);
	InsurerProfile findUserProfileInsurerProfile(String userName, String profileId, String insurerProfileId);
	void deleteUserProfileInsurerProfile(String userName, String profileId, String insurerProfileId);
	
	void createRole(Role role);
	void updateRole(Role role);
	Role findRole(String roleCode);
	void deleteRole(String roleCode);
	
	void createRolePermission(String roleCode, Permission permission);
	void updateRolePermission(String roleCode, Permission permission);
	Permission findRolePermission(String roleCode, String permissionId);
	void deleteRolePermission(String roleCode, String permissionId);

}
