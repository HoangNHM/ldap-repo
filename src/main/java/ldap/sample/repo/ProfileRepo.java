package ldap.sample.repo;

import ldap.sample.domain.Profile;

import org.springframework.ldap.repository.LdapRepository;

public interface ProfileRepo extends LdapRepository<Profile>{

}
