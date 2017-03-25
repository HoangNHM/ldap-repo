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
@JsonIgnoreProperties({ "id", "profileCn", "userCn" })
@Entry(objectClasses = { "insurerProfile", "top" }, base = ConstantLdap.USERS_DN_BASE)
public class InsurerProfile {

	@Id
	private Name id;

	@Attribute(name = "cn")
	@DnAttribute(value = "cn", index = 5)
	private String insurerProfileId;

	@DnAttribute(value = "cn", index = 4)
	@Transient
	private String profileCn;

	@DnAttribute(value = "cn", index = 3)
	@Transient
	private String userCn;

	@Attribute(name = "insurerId")
	private String insurerId;

	@Attribute(name = "agentCode")
	private String agentCode;

}
