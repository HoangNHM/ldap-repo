package ldap.sample.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ldap.sample.constant.ConstantLdap;
import lombok.Data;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties({"id"})
@Entry(objectClasses = { "user", "top" }, base = ConstantLdap.USERS_DN_BASE)
public class User {
	
	@Id
    private Name id;

	@Attribute(name = "userName")
    @DnAttribute(value="cn", index=3)
    private String userName;
    
    @Transient
    private Set<Profile> profiles = new HashSet<Profile>();
    
    @Attribute(name = "address")
    private Set<String> addresses = new HashSet<String>();
    
    @Attribute(name = "phoneNumber")
    private Set<String> phoneNumbers = new HashSet<String>();
    
    @Attribute(name = "approverId")
    private Set<String> approverIds = new HashSet<String>();
    
    @Attribute(name = "idType")
    private String idType;
    
    @Attribute(name = "fullName")
    private String fullName;
    
    @Attribute(name = "idNumber")
    private String idNumber;
    
    @Attribute(name = "email")
    private String email;
    
    @Attribute(name = "dayOfBirth")
    private String dayOfBirth;
    
    @Attribute(name = "isActive")
    private String isActive;
    
    @Attribute(name = "occupation")
    private String occupation;
    
    @Attribute(name = "marialStatus")
    private String marialStatus;
    
    @Attribute(name = "nationality")
    private String nationality;
    
    @Attribute(name = "gender")
    private String gender;
    
    @Attribute(name = "userPassword")
    private String userPassword;
    
    public void addProfile(Profile profile) {
    	profiles.add(profile);
    }
    
    public void addPhoneNumber(String phoneNumber) {
    	phoneNumbers.add(phoneNumber);
    }
    
    public void addAddress(String address) {
    	addresses.add(address);
    }
    
    public void addApproverId(String approverId) {
    	approverIds.add(approverId);
    }

}
