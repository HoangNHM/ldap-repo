package ldap.sample.repo;

import ldap.sample.domain.Permission;

import org.springframework.ldap.repository.LdapRepository;

public interface PermissionRepo extends LdapRepository<Permission>{

}
