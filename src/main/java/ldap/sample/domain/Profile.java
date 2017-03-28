package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties({ "id", "userCn" })
@Entry(objectClasses = { "profile", "top" }, base = ConstantLdap.USERS_DN_BASE)
public class Profile {

	@Id
	private Name id;

	@Attribute(name = "profileId")
	@DnAttribute(value = "cn", index = 4)
	private String profileId;

	@DnAttribute(value = "cn", index = 3)
	@Transient
	private String userCn;

	@Transient
	private Set<InsurerProfile> insurerProfiles = new HashSet<InsurerProfile>();

	@Attribute(name = "role")
	private Set<String> role = new HashSet<String>();

	public void addInsurerProfile(InsurerProfile insurerProfile) {
		insurerProfiles.add(insurerProfile);
	}
}
