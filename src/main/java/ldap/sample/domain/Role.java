package ldap.sample.domain;

import java.util.HashSet;
import java.util.Set;

import javax.naming.Name;

import ldap.sample.constant.ConstantLdap;
import lombok.Data;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties({ "id" })
@Entry(objectClasses = { "role", "top" }, base = ConstantLdap.ROLES_DN_BASE)
public class Role {

	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 3)
	private String roleCode;

	@Transient
	private Set<Permission> permissions = new HashSet<Permission>();

	@Attribute(name = "roleName")
	private String roleName;
	
	public void addPermission(Permission permission) {
		permissions.add(permission);
	}
}
