package com.shareart.service.response;

import io.vertx.core.http.HttpServerResponse;

public class UserResponseHandler extends ResponseHandler{
	
	public static int SUCCESS= 1;
	public static int DUPLICATE_USER_ID = 2;
	public static int USER_INACTIVE = 3;
	public static int USER_BLOCKED = 4;
	public static int USER_DELETED = 5;
	public static int AUTH_FAILED = 6;
	
	public static int APP_ERROR = 10;
	
	public UserResponseHandler(HttpServerResponse response, String baseUrl) {
		super(response,baseUrl);
	}

	public void process(UserResponse userResponse, int OperationStatus, String OperationMessage){
		Response resp = new Response();
		resp.setOperationStatus(OperationStatus);
		resp.setMessage(OperationMessage);
		resp.setBody(userResponse);
		response.putHeader("content-type", "application/json").end(gson.toJson(resp));
	}
	
	public void activationSuccess() {
		response.putHeader("content-type", "text/html");
		String activationSucceshtml = "<HTML><HEAD><TITLE>User Activation</TITLE></HEAD><BODY>Thanks you ! Your Account Has Been <b>Activated</b>.</BODY></HTML>";
		response.end(activationSucceshtml);
	}
	
	public void activationNotNeeded() {
		response.putHeader("content-type", "text/html");
		String activationSucceshtml = "<HTML><HEAD><TITLE>User Activation</TITLE></HEAD><BODY>Thanks you ! user not found or user is already Active</BODY></HTML>";
		response.end(activationSucceshtml);
	}
	
	public void activationError() {
		response.putHeader("content-type", "text/html");
		String activationSucceshtml = "<HTML><HEAD><TITLE>User Activation</TITLE></HEAD><BODY>System Error. Please try again later.</BODY></HTML>";
		response.end(activationSucceshtml);
	}
}
