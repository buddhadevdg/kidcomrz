package com.shareart.service.domain;

import java.io.Serializable;
import java.util.List;

public class Country implements Serializable {

	private static final long serialVersionUID = -3339830428876621826L;

	private String isoCountryCode;
	private String countryCode;
	private String countryName;
	private List<State> states;

	public String getIsoCountryCode() {
		return isoCountryCode;
	}

	public void setIsoCountryCode(String isoCountryCode) {
		this.isoCountryCode = isoCountryCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public List<State> getStates() {
		return states;
	}

	public void setStates(List<State> states) {
		this.states = states;
	}

}
