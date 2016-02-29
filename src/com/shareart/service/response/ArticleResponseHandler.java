package com.shareart.service.response;

import io.vertx.core.http.HttpServerResponse;

public class ArticleResponseHandler extends ResponseHandler{
	
	public static int SUCCESS= 1;
	public static int DUPLICATE_USER_ID = 2;
	public static int USER_INACTIVE = 3;
	public static int USER_BLOCKED = 4;
	public static int USER_DELETED = 5;
	public static int AUTH_FAILED = 6;
	
	public static int APP_ERROR = 10;
	
	public ArticleResponseHandler(HttpServerResponse response, String baseUrl) {
		super(response,baseUrl);
	}

	public void process(ArticleResponse articleResponse, int OperationStatus, String OperationMessage){
		Response resp = new Response();
		resp.setOperationStatus(OperationStatus);
		resp.setMessage(OperationMessage);
		resp.setBody(articleResponse);
		response.putHeader("content-type", "application/json").end(gson.toJson(resp));
	}	
}