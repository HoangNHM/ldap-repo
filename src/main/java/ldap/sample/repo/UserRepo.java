package ldap.sample.repo;

import ldap.sample.domain.User;

import org.springframework.ldap.repository.LdapRepository;

public interface UserRepo extends LdapRepository<User>{
	
}
