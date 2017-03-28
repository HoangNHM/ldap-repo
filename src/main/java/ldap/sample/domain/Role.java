package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties({ "id" })
@Entry(objectClasses = { "role", "top" }, base = ConstantLdap.ROLES_DN_BASE)
public class Role {

	@Id
	private Name id;

	@Attribute(name = "roleCode")
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
