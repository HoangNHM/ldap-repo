package ldap.sample.repo;

import ldap.sample.constant.ConstantLdap;
import ldap.sample.domain.InsurerProfile;
import ldap.sample.domain.Profile;
import ldap.sample.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.ldap.LdapName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by vantuegia on 3/26/2017.
 */
@Service
public final class LdapHelper {

    @Autowired
    LdapTemplate ldapTemplate;

    LdapName buildUserDn(String userName) {
        return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
                .add("cn=" + userName).build();
    }

    LdapName buildProfileDn(String userName, String profileId) {
        return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
                .add("cn=" + userName)
                .add("cn=" + profileId)
                .build();
    }

    LdapName buildInsurerProfileDn(String userName,
                                   String profileId, String insurerProfileId) {
        return LdapNameBuilder.newInstance(ConstantLdap.USERS_DN_BASE)
                .add("cn=" + userName)
                .add("cn=" + profileId)
                .add("cn=" + insurerProfileId)
                .build();
    }

    LdapName buildRoleDn(String roleCode) {
        return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
                .add("cn=" + roleCode).build();
    }

    LdapName buildRolePermissionDn(String roleCode, String permissionId) {
        return LdapNameBuilder.newInstance(ConstantLdap.ROLES_DN_BASE)
                .add("cn=" + roleCode)
                .add("cn=" + permissionId)
                .build();
    }

    List<String> removePrefixCn(List<String> stringList) {
        List<String> result = new ArrayList<String>();
        for (String str :
                stringList) {
            if (str.startsWith("cn=")) {
                result.add(str.substring(3));
            }
        }
        return result;
    }

    void createInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile) {
        insurerProfile.setUserCn(userName);
        insurerProfile.setProfileCn(profileId);
        ldapTemplate.create(insurerProfile);
    }

    void createProfile(String userName, Profile profile) {
        profile.setUserCn(userName);
        ldapTemplate.create(profile);

        Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
        for (InsurerProfile insurerProfile : insurerProfiles) {
            createInsurerProfile(userName, profile.getProfileId(), insurerProfile);
        }
    }

    void createUser(User user) {
        ldapTemplate.create(user);
        Set<Profile> userProfiles = user.getProfiles();
        for (Profile profile : userProfiles) {
            createProfile(user.getUserName(), profile);
        }
    }

    void updateInsurerProfile(String userName, String profileId, InsurerProfile insurerProfile) {
        insurerProfile.setUserCn(userName);
        insurerProfile.setProfileCn(profileId);
        LdapName insurerProfileDn = buildInsurerProfileDn(userName, profileId, insurerProfile.getInsurerProfileId());
        if (ldapTemplate.findByDn(insurerProfileDn, InsurerProfile.class) != null) {
            ldapTemplate.update(insurerProfile);
        } else {
            ldapTemplate.create(insurerProfile);
        }
    }

    void updateProfile(String userName, Profile profile) {
        profile.setUserCn(userName);
        LdapName profileDn = buildProfileDn(userName, profile.getProfileId());
        if (ldapTemplate.findByDn(profileDn, Profile.class) != null) {
            List<String> oldInsurerProfilesCn = removePrefixCn(ldapTemplate.list(profileDn));
            ldapTemplate.update(profile);

            Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
            for (InsurerProfile insurerProfile : insurerProfiles) {
                oldInsurerProfilesCn.remove(insurerProfile.getInsurerProfileId());
                updateInsurerProfile(userName, profile.getProfileId(), insurerProfile);
            }

            for (String unUsedInsurerProfileId :
                    oldInsurerProfilesCn) {
                LdapName unUsedInsurerProfileDn = buildInsurerProfileDn(userName, profile.getProfileId(), unUsedInsurerProfileId);
                ldapTemplate.unbind(unUsedInsurerProfileDn);
            }
        } else {
            createProfile(userName, profile);
        }

    }

    void updateUser(User user) {
        ldapTemplate.update(user);

        LdapName userDn = buildUserDn(user.getUserName());
        // Get list old profile base on userDn
        List<String> oldProfilesId = removePrefixCn(ldapTemplate.list(userDn));

        // Update current user profile
        Set<Profile> profiles = user.getProfiles();
        for (Profile profile : profiles) {
            oldProfilesId.remove(profile.getProfileId());
            updateProfile(user.getUserName(), profile);
        }

        // Delete unused (oldProfiles - curProfiles)user profile
        for (String unUsedProfileId :
                oldProfilesId) {
            LdapName unUsedProfileDn = buildProfileDn(user.getUserName(), unUsedProfileId);
            ldapTemplate.unbind(unUsedProfileDn);
        }
    }

    InsurerProfile findInsurerProfile(String userName, String profileId, String insurerProfileId) {
        LdapName insurerProfileDn = buildInsurerProfileDn(userName, profileId, insurerProfileId);
        return ldapTemplate.findByDn(insurerProfileDn, InsurerProfile.class);
    }

    Profile findProfile(String userName, String profileId) {
        LdapName profileDn = buildProfileDn(userName, profileId);
        Profile profile = ldapTemplate.findByDn(profileDn, Profile.class);
        Set<InsurerProfile> insurerProfiles = profile.getInsurerProfiles();
        for (InsurerProfile insurerProfile :
                insurerProfiles) {
            profile.addInsurerProfile(findInsurerProfile(userName, profileId, insurerProfile.getInsurerProfileId()));
        }
        return profile;
    }
    
    User findUser(String userName) {
        LdapName userDn = buildUserDn(userName);
        User user = ldapTemplate.findByDn(userDn, User.class);

        List<String> profilesId = removePrefixCn(ldapTemplate.listBindings(userDn));
        for (String profileId : profilesId) {
            user.addProfile(findProfile(userName, profileId));
        }
        return user;
    }

    void deleteUser(String userName) {
        ldapTemplate.unbind(buildUserDn(userName), true);
    }

}