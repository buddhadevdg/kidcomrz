package com.shareart.service.response;

import java.util.List;

import com.shareart.service.domain.Country;

import io.vertx.core.http.HttpServerResponse;

public class CountryStateResponseHandler extends ResponseHandler{
	
	public CountryStateResponseHandler(HttpServerResponse response) {
		super(response);
	}

	public void process(List<Country> countryData) {
		response.putHeader("content-type", "application/json").end(gson.toJson(countryData));
	}
}
