package com.shareart.service.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.http.HttpServerResponse;

public class ResponseHandler {
	private boolean responseCommited;
	protected HttpServerResponse response;
	protected Gson gson = new GsonBuilder().setDateFormat("dd/MMM/yyyy HH:mm:ss zzz").create();
	private String baseUrl;
	
	public ResponseHandler(HttpServerResponse response, String baseUrl) {
		this.response = response;
		this.baseUrl = baseUrl;
	}
	
	public ResponseHandler(HttpServerResponse response) {
		this.response = response;
	}

	/**
	 * write and close response with provided errorCode
	 * 
	 * @param statusCode
	 */
	public void sendError(int statusCode) {
		responseCommited = true;
		response.setStatusCode(statusCode).end();
	}

	/**
	 * write and close response with provided error mesage
	 * 
	 * @param errorMsg
	 */
	public void sendError(String errorMsg) {
		responseCommited = true;
		response.setStatusMessage(errorMsg).end();
	}

	/**
	 * write and close response with provided errorCode and error message
	 * 
	 * @param statusCode
	 * @param errorMsg
	 */
	public void sendError(int statusCode, String errorMsg) {
		responseCommited = true;
		response.setStatusCode(statusCode).setStatusMessage(errorMsg).end();
	}

	public boolean isResponseCommited() {
		return responseCommited;
	}

	public void setResponseCommited(boolean responseCommited) {
		this.responseCommited = responseCommited;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
}
