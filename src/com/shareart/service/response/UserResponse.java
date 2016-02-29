package com.shareart.service.response;

import java.io.Serializable;

import com.shareart.service.domain.User;

public class UserResponse implements Serializable {

	private static final long serialVersionUID = -5655112256821650225L;

	private Integer userOid;
	private String userId;
	private Integer addressOid;
	private User user;

	public Integer getUserOid() {
		return userOid;
	}

	public void setUserOid(Integer userOid) {
		this.userOid = userOid;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Integer getAddressOid() {
		return addressOid;
	}

	public void setAddressOid(Integer addressOid) {
		this.addressOid = addressOid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}