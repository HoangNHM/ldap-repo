package ldap.sample.repo;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import ldap.sample.constant.ConstantLdap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

@Component
public class Util {

	@Autowired
	LdapTemplate ldapTemplate;
	
	/**
	 * Check if DN is exist
	 * @param dnName
	 * @return
	 */
	boolean isExist(LdapName dnName) {
		String dn = dnName.toString();
		int split = dn.indexOf(',');
		String baseDn = dn.substring(split + 1); // base part
		String cn = dn.substring(0, split); // leaf part of DN
		@SuppressWarnings("rawtypes")
		AttributesMapper attributesMapper = new AttributesMapper() {

			@Override
			public Object mapFromAttributes(Attributes attrs)
					throws NamingException {
				return attrs.get("cn").get();
			}
		};
		@SuppressWarnings("unchecked")
		// Assume baseDn already exist
		List<Object> list = ldapTemplate.search(baseDn, cn, attributesMapper);
		if (list.isEmpty()) {
			return false;
		} else {
			return true;
		}
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

	LdapName buildAdditionalInfoDn(String userName, String profileId,
			String insurerProfileId, String additionalInfoId) {
		return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
				.add("cn=" + userName).add("cn=" + profileId)
				.add("cn=" + insurerProfileId).add("cn=" + additionalInfoId)
				.build();
	}

	LdapName buildRoleDn(String roleCode) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).build();
	}

	LdapName buildPermissionDn(String roleCode, String permissionId) {
		return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
				.add("cn=" + roleCode).add("cn=" + permissionId).build();
	}

}
