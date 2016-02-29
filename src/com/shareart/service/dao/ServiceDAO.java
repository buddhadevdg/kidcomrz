package com.shareart.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shareart.service.domain.Category;
import com.shareart.service.domain.Country;
import com.shareart.service.domain.State;
import com.shareart.service.response.CategoryResponseHandler;
import com.shareart.service.response.CountryStateResponseHandler;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class ServiceDAO {
	private static final String QUERY_ALL_COUNTRY = "SELECT ISO_COUNTRY_CODE, country.COUNTRY_CODE, COUNTRY_NAME, STATE_NAME, STATE_CODE, STATE_NAME FROM country, state where state.COUNTRY_CODE = country.COUNTRY_CODE;";
	
	private static final String QUERY_ALL_CATEGORY = "SELECT * FROM category";
	public static int APP_ERROR = 10;
	private JDBCClient jdbcClient;

	public JDBCClient getJdbcClient() {
		return jdbcClient;
	}

	public void setJdbcClient(JDBCClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	protected void closeConnection(SQLConnection conn) {
		// and close the connection
		conn.close(done -> {
			if (done.failed()) {
				throw new RuntimeException(done.cause());
			}
		});
	}
	
	public void getAllCountry(CountryStateResponseHandler countryStateResponseHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				countryStateResponseHandler.sendError(APP_ERROR);
				return;
			}
			SQLConnection connection = conn.result();

			connection.query(QUERY_ALL_COUNTRY, countryRs -> {
				if (countryRs.succeeded()) {
					countryStateResponseHandler.process(getCountry(countryRs.result().getRows()));
					// and close the connection
					this.closeConnection(connection);
				} else {
					countryStateResponseHandler.sendError(APP_ERROR,countryRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	private List<Country> getCountry(List<JsonObject> countryJsonData){
		List<Country> countryList = new ArrayList<>();
		Map<String, Country> countryMap = new HashMap<>();
		Country country;
		State state;
		String countryCode;
		for (JsonObject countryData : countryJsonData) {
			countryCode = countryData.getString("ISO_COUNTRY_CODE");
			country = countryMap.get(countryCode);
			if(country == null){
				country = new Country();
				country.setCountryCode(countryData.getString("COUNTRY_CODE"));
				country.setIsoCountryCode(countryData.getString("ISO_COUNTRY_CODE"));
				country.setCountryName(countryData.getString("COUNTRY_NAME"));
				
				state = new State();
				state.setStateCode(countryData.getString("STATE_CODE"));
				state.setStateName(countryData.getString("STATE_NAME"));
				List<State> states = new ArrayList<>();
				states.add(state);
				
				country.setStates(states);
				
				countryList.add(country);
				countryMap.put(country.getIsoCountryCode(), country);
			}else {
				state = new State();
				state.setStateCode(countryData.getString("STATE_CODE"));
				state.setStateName(countryData.getString("STATE_NAME"));
				List<State> states = country.getStates();
				states.add(state);
			}			
		}
		return countryList;
	}
	
	public void getAllCategory(CategoryResponseHandler categoryResponseHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				categoryResponseHandler.sendError(APP_ERROR);
				return;
			}
			SQLConnection connection = conn.result();

			connection.query(QUERY_ALL_CATEGORY, categoryRs -> {
				if (categoryRs.succeeded()) {
					categoryResponseHandler.process(getCategory(categoryRs.result().getRows()));
					// and close the connection
					this.closeConnection(connection);
				} else {
					categoryResponseHandler.sendError(APP_ERROR,categoryRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	private List<Category> getCategory(List<JsonObject> categoryJsonData){
		List<Category> categoryList = new ArrayList<>();
		Map<Integer, Category> categoryMap = new HashMap<>();
		Category category;
		Category subCategory;
		Integer parentCategoryOid;
		for (JsonObject countryData : categoryJsonData) {
			parentCategoryOid = countryData.getInteger("PARENT_CATEGORY_OID");
			category = categoryMap.get(parentCategoryOid);
			if(category == null){
				category = new Category();
				category.setCategoryName(countryData.getString("CATEGORY_NAME"));
				category.setCategoryOid(countryData.getInteger("CATEGORY_OID"));
				
				categoryList.add(category);
				categoryMap.put(category.getCategoryOid(), category);
			}else {
				subCategory = new Category();
				//subCategory.setCategoryName(countryData.getString("CATEGORY_NAME"));
				subCategory.setCategoryName(countryData.getString("SUB_CATEGORY_NAME"));
				//subCategory.setSubCategoryName(countryData.getString("SUB_CATEGORY_NAME"));
				subCategory.setCategoryOid(countryData.getInteger("CATEGORY_OID"));
				//subCategory.setParentCategoryOid(countryData.getInteger("PARENT_CATEGORY_OID"));
				List<Category> subCategorie = category.getSubCategorie();
				if(subCategorie==null){
					subCategorie = new ArrayList<>();
					category.setSubCategorie(subCategorie);
				}
				subCategorie.add(subCategory);
			}			
		}
		return categoryList;
	}
}