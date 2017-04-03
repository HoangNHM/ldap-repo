package ldap.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.repository.config.EnableLdapRepositories;

@SpringBootApplication
@EnableLdapRepositories("ldap")
@EntityScan("ldap")
@ComponentScan("ldap")
public class Application {
	
	@Bean
    ContextSource contextSource() {

        LdapContextSource ldapContextSource = new LdapContextSource();
        /*ldapContextSource.setUrl("ldap://20.203.6.133:389");
        ldapContextSource.setPassword("secret");
        ldapContextSource.setUserDn("uid=admin,ou=system");*/
        ldapContextSource.setUrl("ldap://localhost:10389");
        ldapContextSource.setPassword("secret");
        ldapContextSource.setUserDn("uid=admin,ou=system");
//        ldapContextSource.setBase("dc=example,dc=com");
        return ldapContextSource;
    }

    @Bean
    LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
