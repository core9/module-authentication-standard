package io.core9.module.auth.standard;

import io.core9.module.auth.wrappers.ServerRequestToken;
import io.core9.module.auth.wrappers.UsernameVirtualHostPrincipal;
import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.server.VirtualHost;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

public class StandardRealm extends AuthorizingRealm {
	
	private MongoDatabase database;
	
	public StandardRealm(MongoDatabase database) {
		this.database = database;
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//null usernames are invalid
        if (principals == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        // Retrieve the user object
        UsernameVirtualHostPrincipal principal = (UsernameVirtualHostPrincipal) getAvailablePrincipal(principals);
        VirtualHost vhost = principal.getVirtualHost();
        Map<String,Object> user = retrieveUserObject(vhost, principal.getUsername());
        
        //Retrieve roles for user
        @SuppressWarnings("unchecked")
		Set<String> roleNames = new HashSet<String>((List<String>) user.get("roles"));
        
        //Retrieve permissions for roles
        Set<String> permissions = retrievePermissionsForRoles(vhost, roleNames);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roleNames);
        info.setStringPermissions(permissions);
		return info;
	}

	/**
	 * Retrieve the role permissions
	 * @param roleNames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Set<String> retrievePermissionsForRoles(VirtualHost vhost, Set<String> roleNames) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("configtype", "userrole");
		Map<String,Object> list = new HashMap<String,Object>();
		list.put("$in", roleNames);
		query.put("role", list);
		List<Map<String,Object>> roles = database.getMultipleResults(
				(String) vhost.getContext("database"), vhost.getContext("prefix") + "configuration", query);
		Set<String> permissions = new HashSet<String>();
		for(Map<String,Object> role : roles) {
			permissions.addAll((List<String>) role.get("permissions"));
		}
		return permissions;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		ServerRequestToken reqToken = (ServerRequestToken) token;
		VirtualHost vhost = reqToken.getRequest().getVirtualHost();
		
		// Retrieve the user from the database
		Map<String,Object> user = retrieveUserObject(vhost, reqToken.getUsername());
        SimpleAuthenticationInfo result;
        
        //Parse the password tot he authenticationinfo object
        String password = (String) user.get("password");
        UsernameVirtualHostPrincipal principal = new UsernameVirtualHostPrincipal(vhost, reqToken.getUsername());
    	result = new SimpleAuthenticationInfo(principal, password.toCharArray(), getName());
    	String salt = (String) user.get("salt"); 
    	if(salt != null) {
    		result.setCredentialsSalt(ByteSource.Util.bytes(salt));
    	}
        return result;
	}
	
	/**
	 * Retrieve the user from the database (specified by the request)
	 * @param request
	 * @param username
	 * @return
	 * @throws AuthenticationException
	 */
	private Map<String,Object> retrieveUserObject(VirtualHost vhost, String username) throws AuthenticationException {
		
		// Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }
        
    	Map<String,Object> query = new HashMap<String, Object>();
        query.put("username", username);
                
        List<Map<String,Object>> users = database.getMultipleResults(
        	(String) vhost.getContext("database"), vhost.getContext("prefix") + "users", query);
        
        switch(users.size()) {
        case 0:
        	throw new AuthenticationException("No user was found, please register a user first.");
        case 1:
        	return users.get(0);
        default:
        	throw new AuthenticationException("Multiple users with the same name were found, please use unique usernames.");
        }
	}
}
