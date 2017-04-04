package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;

@Data
@JsonIgnoreProperties({ "id", "profileCn", "userCn", "insurerCn" })
@Entry(objectClasses = { "additionalInfo", "top" }, base = ConstantLdap.USERS_DN_BASE)
public class AdditionalInfo {

	@Id
	private Name id;

	@Attribute(name = "additionalInfoId")
	@DnAttribute(value = "cn", index = 6)
	private String additionalInfoId;

	@DnAttribute(value = "cn", index = 5)
	@Transient
	private String insurerCn;

	@DnAttribute(value = "cn", index = 4)
	@Transient
	private String profileCn;

	@DnAttribute(value = "cn", index = 3)
	@Transient
	private String userCn;

}
