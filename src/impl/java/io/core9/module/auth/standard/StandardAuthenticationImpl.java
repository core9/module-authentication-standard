package io.core9.module.auth.standard;

import io.core9.module.auth.AuthenticationConnector;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.DefaultSecurityManager;

@PluginImplementation
public class StandardAuthenticationImpl implements AuthenticationConnector {
	
	@InjectPlugin
	private RepositoryFactory factory;
	
	@Override
	public DefaultSecurityManager getSecurityManager() {
		try {
			HashedCredentialsMatcher matcher = new HashedCredentialsMatcher("SHA-256");
			StandardRealm realm = new StandardRealm(factory.getRepository(UserEntity.class), factory.getRepository(RoleEntity.class));
			realm.setCredentialsMatcher(matcher);
			return new DefaultSecurityManager(realm);
		} catch (NoCollectionNamePresentException e) {
			e.printStackTrace();
			return new DefaultSecurityManager();
		}
	}

}
