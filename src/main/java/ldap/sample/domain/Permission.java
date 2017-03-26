package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;

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
