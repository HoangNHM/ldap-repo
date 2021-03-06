package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;

@Data
@JsonIgnoreProperties({ "id", "profileCn", "userCn" })
@Entry(objectClasses = { "insurerProfile", "top" }, base = ConstantLdap.USERS_DN_BASE)
public class InsurerProfile {

	@Id
	private Name id;

	@Attribute(name = "insurerId")
	@DnAttribute(value = "cn", index = 5)
	private String insurerId;
	
	@Transient
	private AdditionalInfo additionalInfo;

	@DnAttribute(value = "cn", index = 4)
	@Transient
	private String profileCn;

	@DnAttribute(value = "cn", index = 3)
	@Transient
	private String userCn;

	@Attribute(name = "agentCode")
	private String agentCode;

}
