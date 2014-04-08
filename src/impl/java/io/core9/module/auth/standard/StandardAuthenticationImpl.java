package io.core9.module.auth.standard;

import io.core9.module.auth.AuthenticationConnector;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;

@PluginImplementation
public class StandardAuthenticationImpl extends DefaultSecurityManager implements AuthenticationConnector {
	
	@InjectPlugin
	private RepositoryFactory factory;
	
	@Override
	public SecurityManager getSecurityManager() {
		try {
			return new DefaultSecurityManager(
					new StandardRealm(
							factory.getRepository(UserEntity.class), 
							factory.getRepository(RoleEntity.class)));
		} catch (NoCollectionNamePresentException e) {
			e.printStackTrace();
			return new DefaultSecurityManager();
		}
	}

}
