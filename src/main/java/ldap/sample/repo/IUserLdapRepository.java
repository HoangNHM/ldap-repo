package ldap.sample.repo;

import ldap.sample.domain.*;

public interface IUserLdapRepository {
	
	void createUser(User user);
	void updateUser(User user);
	User findUser(String userName);
	void deleteUser(String userName);
	
	void createProfile(String userName, Profile profile);
	void updateProfile(String userName, Profile profile);
	Profile findProfile(String userName, String profileId);
	void deleteProfile(String userName, String profileId);
	
	void createInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile);
	void updateInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile);
	InsurerProfile findInsurerProfile(String userName, String profileId, String insurerProfileId);
	void deleteInsurerProfile(String userName, String profileId, String insurerProfileId);
	
	void createAdi(String userName, String profileId, String insurerProfileId, AdditionalInfo adi);
	void updateAdi(String userName, String profileId, String insurerProfileId, AdditionalInfo adi);
	AdditionalInfo findAdi(String userName, String profileId, String insurerProfileId, String adiId);
	void deleteAdi(String userName, String profileId, String insurerProfileId, String adiId);
	
	void createRole(Role role);
	void updateRole(Role role);
	Role findRole(String roleCode);
	void deleteRole(String roleCode);
	
	void createPermission(String roleCode, Permission permission);
	void updatePermission(String roleCode, Permission permission);
	Permission findPermission(String roleCode, String permissionId);
	void deletePermission(String roleCode, String permissionId);

}
