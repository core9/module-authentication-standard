package io.core9.module.auth.standard;

import io.core9.module.auth.AuthenticationConnector;
import io.core9.plugin.database.mongodb.MongoDatabase;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;

@PluginImplementation
public class StandardAuthenticationImpl extends DefaultSecurityManager implements AuthenticationConnector {
	
	@InjectPlugin
	private MongoDatabase database;
	
	@Override
	public SecurityManager getSecurityManager() {
		return new DefaultSecurityManager(new StandardRealm(database));
	}

}
