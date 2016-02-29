package com.shareart.service.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class User implements Serializable {

	private static final long serialVersionUID = 6688867343780746679L;

	// USER_OID
	private Integer userOid;
	// USER_ID
	private String userId;
	// PASSWORD
	private String password;
	// FIRST_NAME
	private String firstName;
	// LAST_NAME
	private String lastName;
	// EMAIL
	private String email;
	// PHONE
	private String phone;
	// DOB
	private String dob;
	private Date birthDate;
	// ADDRESS_OID
	private Address address;
	// PARENT_OID
	private User parent;
	private Integer parentOid;
	private List<User> childrens;
	// ACTIVATION_DATE
	private Date activationDate;
	// ACTIVE
	private Integer active ;
	
	public Integer getUserOid() {
		return userOid;
	}

	public void setUserOid(Integer userOid) {
		this.userOid = userOid;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public User getParent() {
		return parent;
	}

	public void setParent(User parent) {
		this.parent = parent;
	}

	public Integer getParentOid() {
		return parentOid;
	}

	public void setParentOid(Integer parentOid) {
		this.parentOid = parentOid;
	}

	public List<User> getChildrens() {
		return childrens;
	}

	public void setChildrens(List<User> childrens) {
		this.childrens = childrens;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}
}
