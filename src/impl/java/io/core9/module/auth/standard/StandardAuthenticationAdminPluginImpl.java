package io.core9.module.auth.standard;

import io.core9.plugin.admin.AbstractAdminPlugin;
import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.DataUtils;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.server.request.Method;
import io.core9.plugin.server.request.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;

@PluginImplementation
public class StandardAuthenticationAdminPluginImpl extends AbstractAdminPlugin implements StandardAuthenticationAdminPlugin {
	
	private CrudRepository<UserEntity> userRepository;
	private CrudRepository<RoleEntity> roleRepository;
	
	@PluginLoaded
	public void repositoryFactoryLoaded(RepositoryFactory factory) throws NoCollectionNamePresentException {
		userRepository = factory.getRepository(UserEntity.class);
		roleRepository = factory.getRepository(RoleEntity.class);
	}

	@Override
	public String getControllerName() {
		return "authentication";
	}

	@Override
	protected void process(Request request) {
		request.getResponse().setStatusCode(404);
		request.getResponse().end("Not found");
	}

	@Override
	protected void process(Request request, String type) {
		try {
			switch(type) {
			case "user":
				if(request.getMethod() == Method.POST) {
					request.getResponse().end(createUser(request.getVirtualHost(), request.getBodyAsMap().toBlocking().last()));
				} else {
					request.getResponse().sendJsonArray(getUserList(request.getVirtualHost()));
				}
				break;
			case "role":
				if(request.getMethod() == Method.POST) {
					addRole(request.getVirtualHost(), request.getBodyAsMap().toBlocking().last());
				} else {
					request.getResponse().sendJsonArray(getRoleList(request.getVirtualHost()));
				}
				break;
			}
		} catch (JsonProcessingException e) {
			request.getResponse().setStatusCode(500);
			request.getResponse().addValue("error", e.getMessage());
		}
	}

	@Override
	protected void process(Request request, String type, String id) {
		try {
			switch(type) {
			case "user":
				if(request.getMethod() == Method.PUT) {
					request.getResponse().end(updateUser(request.getVirtualHost(), request.getBodyAsMap().toBlocking().last()));
				} else if (request.getMethod() == Method.DELETE) {
					deleteUser(request.getVirtualHost(), id);
					request.getResponse().end();
				} else {
					request.getResponse().end(readUser(request.getVirtualHost(), id));
				}
				break;
			case "role":
				request.getResponse().end(getRole(request.getVirtualHost(), id));
				break;
			}
		} catch (JsonProcessingException e) {
			request.getResponse().setStatusCode(500);
			request.getResponse().addValue("error", e.getMessage());
		}
	}
	
	/**
	 * Create a new user, return the JSON representation of this user
	 * @param vhost
	 * @param bodyAsMap
	 * @return
	 * @throws JsonProcessingException
	 */
	private String createUser(VirtualHost vhost, Map<String, Object> bodyAsMap) throws JsonProcessingException {
		UserEntity user = DataUtils.toObject(bodyAsMap, UserEntity.class);
		user.hashPassword(user.getPassword());
		user = userRepository.create(vhost, user);
		return DataUtils.toJSON(user);
	}

	/**
	 * Find a user by its ID
	 * @param vhost
	 * @param id
	 * @return
	 * @throws JsonProcessingException
	 */
	private String readUser(VirtualHost vhost, String id) throws JsonProcessingException {
		return DataUtils.toJSON(userRepository.read(vhost, id));
	}
	
	/**
	 * Update a user
	 * @param virtualHost
	 * @param bodyAsMap
	 * @return
	 * @throws JsonProcessingException
	 */
	private String updateUser(VirtualHost vhost, Map<String, Object> bodyAsMap) throws JsonProcessingException {
		String newPassword = (String) bodyAsMap.remove("newPassword");
		UserEntity user = DataUtils.toObject(bodyAsMap, UserEntity.class);
		if(newPassword != null) {
			user.hashPassword(newPassword);
		}
		user = userRepository.update(vhost, user.getId(), user);
		return DataUtils.toJSON(user);
	}
	
	/**
	 * Delete a user by its id
	 * @param vhost
	 * @param id
	 */
	private void deleteUser(VirtualHost vhost, String id) {
		userRepository.delete(vhost, id);
	}
	
	/**
	 * Return a list of users
	 * @param vhost
	 * @return
	 */
	private List<UserEntity> getUserList(VirtualHost vhost) {
		return userRepository.getAll(vhost); 
	}
	
	private String getRole(VirtualHost vhost, String id) throws JsonProcessingException {
		return DataUtils.toJSON(roleRepository.read(vhost, id));
	}
	
	private List<RoleEntity> getRoleList(VirtualHost vhost) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("configtype", "userrole");
		return roleRepository.query(vhost, query); 
	}

	private void addRole(VirtualHost virtualHost, Map<String, Object> bodyAsMap) {
		
	}
}
