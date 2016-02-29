package com.shareart.service.domain;

import java.io.Serializable;

public class Address implements Serializable {

	private static final long serialVersionUID = 2904637760359215123L;

	// ADDRESS_OID
	private Integer addressOid;
	// LINE1
	private String line1;
	// LINE2
	private String line2;
	// CITY
	private String city;
	// STATE_PROVINCE
	private String state;
	// country
	private String country;
	// ZIP_CODE
	private String zipCode;

	public Integer getAddressOid() {
		return addressOid;
	}

	public void setAddressOid(Integer addressOid) {
		this.addressOid = addressOid;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
}