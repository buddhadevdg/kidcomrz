package com.shareart.service.response;

import java.util.List;

import com.shareart.service.domain.Category;

import io.vertx.core.http.HttpServerResponse;

public class CategoryResponseHandler extends ResponseHandler{
	
	public CategoryResponseHandler(HttpServerResponse response) {
		super(response);
	}

	public void process(List<Category> categoryData) {
		response.putHeader("content-type", "application/json").end(gson.toJson(categoryData));
	}
}
