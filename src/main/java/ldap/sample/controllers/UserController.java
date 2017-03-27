package ldap.sample.controllers;

import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;
import ldap.sample.repo.UserLdapRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	@Autowired
	UserLdapRepository iposRepository;

	@Autowired
	LdapTemplate ldapTemplate;

	@RequestMapping("/about")
	public String about() {
		return "Microservice group integration";
	}

	// User
	@RequestMapping(method = RequestMethod.POST, value = "/users")
	public ResponseEntity<?> createUser(@RequestBody User user) {

		iposRepository.createUser(user);

		return new ResponseEntity<>(user, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/users/{userId:.+}")
	public ResponseEntity<?> findUser(@PathVariable String userId) {
		User user = iposRepository.findUser(userId);
		return new ResponseEntity<>(user, HttpStatus.FOUND);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/users/{userId:.+}")
	public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody User user) {
		iposRepository.updateUser(user);
		return new ResponseEntity<>(user, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId:.+}")
	public ResponseEntity<?> deleteUser(@PathVariable String userId) {
		iposRepository.deleteUser(userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	// Profile
	@RequestMapping(method = RequestMethod.POST, value = "/profiles")
	public ResponseEntity<?> createProfile(String userName, @RequestBody Profile profile) {

		iposRepository.createProfile(userName, profile);

		return new ResponseEntity<>(profile, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/users/{profileId}")
	public ResponseEntity<?> findProfile(String userName, @PathVariable String profileId) {
		Profile profile = iposRepository.findProfile(userName, profileId);
		return new ResponseEntity<>(profile, HttpStatus.FOUND);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/users/{userId:.+}")
	public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Profile profile) {
		iposRepository.updateUser(profile);
		return new ResponseEntity<>(profile, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId:.+}")
	public ResponseEntity<?> deleteUser(@PathVariable String userId) {
		iposRepository.deleteUser(userId);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	// Role
	@RequestMapping(method = RequestMethod.POST, value = "/roles")
	public ResponseEntity<?> createRole(@RequestBody Role role) {
		iposRepository.createRole(role);
		return new ResponseEntity<>(role, HttpStatus.CREATED);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/roles/{roleCode}")
	public ResponseEntity<?> updateRole(@PathVariable String roleCode, @RequestBody Role role) {
		iposRepository.updateRole(role);
		return new ResponseEntity<>(HttpStatus.OK);
	}	

	@RequestMapping(method = RequestMethod.GET, value = "/roles/{roleCode}")
	public ResponseEntity<?> findRole(@PathVariable String roleCode) {
		Role role = iposRepository.findRole(roleCode);
		return new ResponseEntity<>(role, HttpStatus.FOUND);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/roles/{roleCode}")
	public ResponseEntity<?> deleteRole(@PathVariable String roleCode) {
		iposRepository.deleteRole(roleCode);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	private Role newRole(String roleCode) {
		Role role = new Role();
		role.setRoleCode(roleCode);
		role.setRoleName("RN" + roleCode);
		Permission permission1 = new Permission();
		permission1.setPermissionId("PM1" + roleCode);
		role.addPermission(permission1);
		Permission permission2 = new Permission();
		permission2.setPermissionId("PM2" + roleCode);
		role.addPermission(permission2);
		return role;
	}

	private User newUser(String name) {
		User user = new User();
		user.setDayOfBirth("12/12/2012");
		user.setEmail(name);
		user.setFullName("Hoang Nguyen");
		user.setIdNumber("12345678");
		user.setIsActive("Y");
		user.addPhoneNumber("+84 7236487263");
		user.addPhoneNumber("+08 7236487263");
		user.addAddress("Etown 3");
		user.addAddress("Etown 5");
		user.addApproverId("agt2@ipos.com");
		user.addApproverId("agt3@ipos.com");
		user.setUserName(name);
		user.setUserPassword("P@ssword123");
		
		Profile profile1 = new Profile();
		profile1.setProfileId("PF000001");
		user.addProfile(profile1);
		Profile profile2 = new Profile();
		profile2.setProfileId("PF000002");
		user.addProfile(profile2);

		InsurerProfile iProfile = new InsurerProfile();
		iProfile.setAgentCode(name);
		iProfile.setInsurerId(name);
		iProfile.setInsurerProfileId(name);
		profile1.addInsurerProfile(iProfile);

		return user;
	}

}
