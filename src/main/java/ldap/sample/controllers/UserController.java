package ldap.sample.controllers;

import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Profile;
import ldap.sample.domain.User;
import ldap.sample.repo.UserLdapRepository;
import ldap.sample.repo.ProfileRepo;
import ldap.sample.repo.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserLdapRepository iposRepository;

	@Autowired
	UserRepo userRepo;

	@Autowired
	ProfileRepo profileRepo;

	@Autowired
	LdapTemplate ldapTemplate;

	@RequestMapping("/about")
	public String about() {
		return "Microservice group integration";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/createUser")
	public ResponseEntity<?> createUser(@RequestBody User user) {

		// User user = newUser("agent1@ipos.com");
		iposRepository.createUser(user);

		return new ResponseEntity<User>(user, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/findUser")
	public ResponseEntity<?> findUser(String userId) {

		User user = iposRepository.findUser(userId);

		return new ResponseEntity<User>(user, HttpStatus.FOUND);
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
