package io.core9.module.auth.standard;

import io.core9.plugin.database.repository.AbstractCrudEntity;
import io.core9.plugin.database.repository.Collection;
import io.core9.plugin.database.repository.CrudEntity;

import java.util.List;

@Collection("configuration")
public class RoleEntity extends AbstractCrudEntity implements CrudEntity {

	private String role;
	private List<String> permissions;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

}
