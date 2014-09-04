package io.core9.module.auth.standard;

import io.core9.plugin.database.repository.AbstractCrudEntity;
import io.core9.plugin.database.repository.Collection;
import io.core9.plugin.database.repository.CrudEntity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Set;

import org.apache.shiro.crypto.hash.Sha256Hash;

@Collection("core.users")
public class UserEntity extends AbstractCrudEntity implements CrudEntity {
	
	private String username;
	private String password;
	private String salt;
	private Set<String> roles;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
	public void hashPassword(String source) {
		if(source == null) {
			SecureRandom random = new SecureRandom();
			source = new BigInteger(130, random).toString(32);
		}
		this.password = new Sha256Hash(source, salt).toString();
	}

}
