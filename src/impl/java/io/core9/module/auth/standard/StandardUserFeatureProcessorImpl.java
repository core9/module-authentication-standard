package io.core9.module.auth.standard;

import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.DataUtils;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;

@PluginImplementation
public class StandardUserFeatureProcessorImpl implements StandardUserFeatureProcessor {
	
	private CrudRepository<UserEntity> userRepository;
	
	@PluginLoaded
	public void onRepositoryFactoryLoaded(RepositoryFactory factory) throws NoCollectionNamePresentException {
		userRepository = factory.getRepository(UserEntity.class);
	}

	@Override
	public String getFeatureNamespace() {
		return "user";
	}

	@Override
	public String getProcessorAdminTemplateName() {
		return "users/processor.tpl.html";
	}

	@Override
	public boolean updateFeatureContent(VirtualHost vhost, File repository,	Map<String, Object> feature) {
		UserEntity user = userRepository.read(vhost, (String) feature.get("id"));
		Map<String,Object> entry = DataUtils.toMap(user);
		if(entry != null) {
			feature.put("entry", entry);
			return true;
		}
		return false;
	}

	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> feature) {
		@SuppressWarnings("unchecked")
		UserEntity user = DataUtils.toObject((Map<String,Object>) feature.get("entry"), UserEntity.class);
		userRepository.create(vhost, user);
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		userRepository.delete(vhost, (String) item.get("id"));
	}

}
