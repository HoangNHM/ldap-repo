package ldap.sample.repo;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.LdapName;

import ldap.sample.constant.ConstantLdap;
import ldap.sample.domain.AdditionalInfo;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Permission;
import ldap.sample.domain.Profile;
import ldap.sample.domain.Role;
import ldap.sample.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapOperationsCallback;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.stereotype.Service;

import com.unboundid.asn1.ASN1OctetString;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.EntrySorter;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl;
import com.unboundid.util.LDAPTestUtils;

@Service
public final class LdapHelper {

	@Autowired
	ContextSource contextSource;
	@Autowired
	LdapTemplate ldapTemplate;

	@Autowired
	Util util;
	
	public void sample() throws Exception {
		LDAPConnection connection = new LDAPConnection("localhost", 10389);
		// Perform a search to retrieve all users in the server, but only retrieving
		 // ten at a time.
		 int numSearches = 0;
		 int totalEntriesReturned = 0;
		 int count = 0;
		 SearchRequest searchRequest = new SearchRequest("dc=ipos,dc=com",
		      SearchScope.SUB, Filter.createEqualityFilter("objectClass", "user"));
		 ASN1OctetString resumeCookie = null;
		 while (true)
		 {
		   searchRequest.setControls(
		        new SimplePagedResultsControl(10, resumeCookie));
		   SearchResult searchResult = connection.search(searchRequest);
		   numSearches++;
		   totalEntriesReturned += searchResult.getEntryCount();
		   for (SearchResultEntry e : searchResult.getSearchEntries())
		   {
		     // Do something with each entry...
			   System.out.println(count++);
			   System.out.println(e.toLDIFString());
		   }

		   LDAPTestUtils.assertHasControl(searchResult,
		        SimplePagedResultsControl.PAGED_RESULTS_OID);
		   SimplePagedResultsControl responseControl =
		        SimplePagedResultsControl.get(searchResult);
		   if (responseControl.moreResultsToReturn())
		   {
		     // The resume cookie can be included in the simple paged results
		     // control included in the next search to get the next page of results.
		     resumeCookie = responseControl.getCookie();
		   }
		   else
		   {
		     break;
		   }
		 }
	}

	public void sortedSearch(String baseDN, String filter,
			LDAPConnection connection, String[] attributes)
			throws LDAPException {
		SearchRequest searchRequest = new SearchRequest(baseDN,
				SearchScope.SUB, filter, attributes);
		SearchResult searchResult = connection.search(searchRequest);
		EntrySorter entrySorter = new EntrySorter();
		SortedSet<Entry> sortedEntries = entrySorter.sort(searchResult
				.getSearchEntries());
		for (Iterator<Entry> iterator = sortedEntries.iterator(); iterator
				.hasNext();) {
			Entry entry = iterator.next();
			System.out.println(entry.toLDIFString());
		}

	}
	
	public void search(LDAPConnection connection, String path, String filter, String[] attributes, int size) throws LDAPException, FileNotFoundException{
		List<Entry> entries = new ArrayList<>();
		
		// search
		int numSearches = 0;
		int totalEntriesReturned = 0;
		int count = 0;
		SearchRequest searchRequest = new SearchRequest(path, SearchScope.SUB, filter, attributes);
		ASN1OctetString resumeCookie = null;
		 while (true)
		 {
		   searchRequest.setControls(
		        new SimplePagedResultsControl(size, resumeCookie));
		   SearchResult searchResult = connection.search(searchRequest);
		   entries.addAll(searchResult.getSearchEntries());
		   numSearches++;
		   totalEntriesReturned += searchResult.getEntryCount();
		   System.out.println("numSearches: " + numSearches + ", totalEntriesReturned: " + totalEntriesReturned);
		   for (SearchResultEntry e : searchResult.getSearchEntries())
		   {
			   System.out.println(count++);
			   System.out.println(e.toLDIFString());
		   }
		   LDAPTestUtils.assertHasControl(searchResult,
		        SimplePagedResultsControl.PAGED_RESULTS_OID);
		   SimplePagedResultsControl responseControl =
		        SimplePagedResultsControl.get(searchResult);
		   if (responseControl.moreResultsToReturn())
		   {
		     // The resume cookie can be included in the simple paged results
		     // control included in the next search to get the next page of results.
		     resumeCookie = responseControl.getCookie();
		   }
		   else
		   {
		     break;
		   }
		 }
	    
	    // sort
	    EntrySorter entrySorter = new EntrySorter();
	    SortedSet<Entry> sortedEntries = entrySorter.sort(entries);
	    
	    // write
	    
	    try(  PrintWriter out = new PrintWriter( "data.ldif" )  ){
	        
	       
		    for (Iterator<Entry> iterator = sortedEntries.iterator(); iterator
					.hasNext();) {
				Entry entry = iterator.next();
//				System.out.println(entry.toLDIFString());
				out.println(entry.toLDIFString());
			} 
	    }
	}

	public byte[] getAllUserCns(byte[] cookie) throws NamingException {
		final int pageSize = 100;
		final SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		final PagedResultsDirContextProcessor processor;
		if (cookie == null) {
			processor = new PagedResultsDirContextProcessor(pageSize);
		} else {
			cookie = new byte[3];
			cookie[0] = 2;
			cookie[1] = 0;
			cookie[2] = 0;
			processor = new PagedResultsDirContextProcessor(pageSize,
					new PagedResultsCookie(cookie));
		}

		SingleContextSource.doWithSingleContext(contextSource,
				new LdapOperationsCallback<List<String>>() {

					@Override
					public List<String> doWithLdapOperations(
							LdapOperations operations) {
						List<String> result = new LinkedList<String>();

						do {
							@SuppressWarnings("unchecked")
							List<String> oneResult = operations.search(
									ConstantLdap.USERS_DN_BASE,
									"(&(objectclass=user))", searchControls,
									new AttributesMapper() {

										@Override
										public Object mapFromAttributes(
												Attributes attrs)
												throws NamingException {
											System.out.println(attrs.get("cn")
													.get());
											return attrs.get("cn").get();
										}
									}, processor);
							result.addAll(oneResult);
							System.out.println(Arrays.toString(processor
									.getCookie().getCookie()));
						} while (processor.hasMore());
						return result;
					}
				});
		byte[] temp = processor.getCookie().getCookie();
		System.out.println(Arrays.toString(temp));
		return temp;
	}

	void createAdditionalInfo(String userName, String profileId,
			String insurerProfileId, AdditionalInfo adi) {
		// Set parent path
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
		LdapName insurerProfileDn = util.buildInsurerProfileDn(userName,
				profileId, insurerProfile.getInsurerId());
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
		LdapName profileDn = util.buildProfileDn(userName,
				profile.getProfileId());
		if (util.isExist(profileDn)) {
			List<String> oldInsurerProfilesCn = util
					.removePrefixCn(ldapTemplate.list(profileDn));
			ldapTemplate.update(profile);

			Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
			assert !insurerProfiles.isEmpty() : "User " + userName
					+ ", profile " + profile.getProfileId()
					+ ", Must have InsurerProfile";
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
		List<String> oldProfilesId = util.removePrefixCn(ldapTemplate
				.list(userDn));

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
		LdapName insurerProfileDn = util.buildInsurerProfileDn(userName,
				profileId, insurerProfileId);
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

			List<String> profilesId = util.removePrefixCn(ldapTemplate
					.list(userDn));
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
		assert !permissions.isEmpty() : "Role " + role.getRoleCode()
				+ ", Must have Permissions";
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
		assert !permissions.isEmpty() : "Role " + role.getRoleCode()
				+ ", Must have Permissions";
		for (Permission permission : permissions) {
			oldPermissionsId.remove(permission.getPermissionId());
			updatePermission(role.getRoleCode(), permission);
		}

		// Delete unused (oldPermissions - curPermissions) Role Permissions
		for (String unUsePermissionId : oldPermissionsId) {
			LdapName unUsedPermissionDn = util.buildPermissionDn(
					role.getRoleCode(), unUsePermissionId);
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
			assert !permissionsId.isEmpty() : "Role " + roleCode
					+ ", Must have Permissions";
			for (String permissionId : permissionsId) {
				role.addPermission(findPermission(roleCode, permissionId));
			}
			return role;
		} else {
			return null;
		}
	}

}
