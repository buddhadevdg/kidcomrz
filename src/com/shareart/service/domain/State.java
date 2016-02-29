package com.shareart.service.domain;

import java.io.Serializable;

public class State implements Serializable {

	private static final long serialVersionUID = 6870011186458153011L;
	private String stateCode;
	private String stateName;

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}
}
