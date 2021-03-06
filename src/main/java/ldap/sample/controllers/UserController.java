package ldap.sample.controllers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import ldap.sample.domain.AdditionalInfo;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;
import ldap.sample.repo.LdapHelper;
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

import com.unboundid.ldap.sdk.LDAPException;

@RestController
public class UserController {

	@Autowired
	UserLdapRepository iposRepository;

	@Autowired
	LdapTemplate ldapTemplate;
	
	@Autowired
	LdapHelper ldapHelper;

	@RequestMapping("/about")
	public String about() {
		return "Micro service group ldap repo";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/export")
	public ResponseEntity<?> export(int size) throws Exception {
//		iposRepository.search(size);
		ldapHelper.sample();
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/all")
	public ResponseEntity<?> all() {

	    Hashtable<String, Object> env = new Hashtable<String, Object>(11);
	    env
	        .put(Context.INITIAL_CONTEXT_FACTORY,
	            "com.sun.jndi.ldap.LdapCtxFactory");

	    /* Specify host and port to use for directory service */
	    env.put(Context.PROVIDER_URL,
	        "ldap://localhost:10389/ou=users,dc=ipos,dc=com");

	    try {
	      LdapContext ctx = new InitialLdapContext(env, null);

	      // Activate paged results
	      int pageSize = 5;
	      byte[] cookie = null;
	      ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize,
	          Control.NONCRITICAL) });
	      int total;

	      do {
	        /* perform the search */
	        NamingEnumeration results = ctx.search("", "(objectclass=*)",
	            new SearchControls());

	        /* for each entry print out name + all attrs and values */
	        while (results != null && results.hasMore()) {
	          SearchResult entry = (SearchResult) results.next();
	          System.out.println(entry.getName());
	        }

	        // Examine the paged results control response
	        Control[] controls = ctx.getResponseControls();
	        if (controls != null) {
	          for (int i = 0; i < controls.length; i++) {
	            if (controls[i] instanceof PagedResultsResponseControl) {
	              PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
	              total = prrc.getResultSize();
	              if (total != 0) {
	                System.out.println("***************** END-OF-PAGE "
	                    + "(total : " + total + ") *****************\n");
	              } else {
	                System.out.println("***************** END-OF-PAGE "
	                    + "(total: unknown) ***************\n");
	              }
	              cookie = prrc.getCookie();
	            }
	          }
	        } else {
	          System.out.println("No controls were sent from the server");
	        }
	        // Re-activate paged results
	        ctx.setRequestControls(new Control[] { new PagedResultsControl(
	            pageSize, cookie, Control.CRITICAL) });

	      } while (cookie != null);

	      ctx.close();

	    } catch (NamingException e) {
	      System.err.println("PagedSearch failed.");
	      e.printStackTrace();
	    } catch (IOException ie) {
	      System.err.println("PagedSearch failed.");
	      ie.printStackTrace();
	    }
	    return new ResponseEntity<>(HttpStatus.OK);
	  }
	
	@RequestMapping(method = RequestMethod.GET, value = "/users")
	public ResponseEntity<?> getAllUserCns(byte[] cookie) throws NamingException {
		return new ResponseEntity<>(ldapHelper.getAllUserCns(cookie), HttpStatus.FOUND);
	}

	// User
	@RequestMapping(method = RequestMethod.POST, value = "/users")
	public ResponseEntity<?> createUser(@RequestBody User user) {

//		User user = newUser(name);
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
	@RequestMapping(method = RequestMethod.POST, value = "/users/{userId:.+}/profiles")
	public ResponseEntity<?> createProfile(@PathVariable String userId, @RequestBody Profile profile) {
		iposRepository.createProfile(userId, profile);
		return new ResponseEntity<>(profile, HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/users/{userId:.+}/profiles/{profileId}")
	public ResponseEntity<?> findProfile(@PathVariable String userId, @PathVariable String profileId) {
		Profile profile = iposRepository.findProfile(userId, profileId);
		return new ResponseEntity<>(profile, HttpStatus.FOUND);
	}
	
	@RequestMapping(method = RequestMethod.PUT, value = "/users/{userId:.+}/profiles/{profileId}")
	public ResponseEntity<?> updateProfile(@PathVariable String userId, @PathVariable String profileId, @RequestBody Profile profile) {
		iposRepository.updateProfile(userId, profile);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId:.+}/profiles/{profileId}")
	public ResponseEntity<?> deleteProfile(@PathVariable String userId, @PathVariable String profileId) {
		iposRepository.deleteProfile(userId, profileId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// Role
	@RequestMapping(method = RequestMethod.POST, value = "/roles")
	public ResponseEntity<?> createRole(@RequestBody Role role) {
		iposRepository.createRole(role);
		return new ResponseEntity<>(HttpStatus.CREATED);
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

	// Permission
	@RequestMapping(method = RequestMethod.POST, value = "/roles/{roleCode}/permissions")
	public ResponseEntity<?> createPermission(@PathVariable String roleCode, @RequestBody Permission permission) {
		iposRepository.createPermission(roleCode, permission);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/roles/{roleCode}/permissions/{permissionsId}")
	public ResponseEntity<?> updatePermission(@PathVariable String roleCode, @PathVariable String permissionsId, @RequestBody Permission permission) {
		iposRepository.updatePermission(roleCode, permission);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/roles/{roleCode}/permissions/{permissionsId}")
	public ResponseEntity<?> findPermission(@PathVariable String roleCode, @PathVariable String permissionsId) {
		Permission permission = iposRepository.findPermission(roleCode, permissionsId);
		return new ResponseEntity<>(permission, HttpStatus.FOUND);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/roles/{roleCode}/permissions/{permissionsId}")
	public ResponseEntity<?> deletePermission(@PathVariable String roleCode, @PathVariable String permissionsId) {
		iposRepository.deletePermission(roleCode, permissionsId);
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
		
		Profile profile = new Profile();
		profile.setProfileId("PF000001");
		user.setProfile(profile);
		Set<String> role = new HashSet<String>();
		role.add("role1");
		role.add("role2");
		profile.setRole(role);

		InsurerProfile iProfile = new InsurerProfile();
		iProfile.setAgentCode("AC32113");
		iProfile.setInsurerId("InsurerId01");

        AdditionalInfo adi = new AdditionalInfo();
        adi.setAdditionalInfoId("ADI00001");
        adi.setUserCn(name);
        adi.setProfileCn(profile.getProfileId());
        adi.setInsurerCn(iProfile.getInsurerId());
        iProfile.setAdditionalInfo(adi);

        profile.addInsurerProfile(iProfile);

		return user;
	}

}
