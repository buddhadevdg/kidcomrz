package com.shareart.service.response;

import java.io.Serializable;

public class Response implements Serializable {

	private static final long serialVersionUID = -1005604145841582810L;

	private Integer operationStatus;
	private Integer errorCode;
	private String message;
	private Object body;

	public Integer getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(Integer operationStatus) {
		this.operationStatus = operationStatus;
	}

	public Integer getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
