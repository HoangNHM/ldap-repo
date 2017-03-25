package ldap.sample.domain;

import javax.naming.Name;

import ldap.sample.constant.ConstantLdap;
import lombok.Data;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties({ "id", "roleCn" })
@Entry(objectClasses = { "permission", "top" }, base = ConstantLdap.ROLES_DN_BASE)
public class Permission {

	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 4)
	private String permissionId;

	@Transient
	@DnAttribute(value = "cn", index = 3)
	private String roleCn;
}
