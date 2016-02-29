package com.shareart.service.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Article implements Serializable {

	private static final long serialVersionUID = 1237828927538548726L;

	// ARTICLE_OID
	private Integer articleOid;
	// USER_OID
	private User user;
	private String userOid;
	// ARTICLE_NAME
	private String articleName;
	// CATEGORY_OID
	private Integer categoryOid;
	// DESCRIPTION
	private String description;
	//PRICE
	private Double price;
	// STATE
	private Integer state;
	// PURCHASED_BY
	private String purchasedBy;
	// ACTIVE
	private Integer active;
	
	private List<Document> documents;
	
	private Category category;

	public Integer getArticleOid() {
		return articleOid;
	}

	public void setArticleOid(Integer articleOid) {
		this.articleOid = articleOid;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getUserOid() {
		return userOid;
	}

	public void setUserOid(String userOid) {
		this.userOid = userOid;
	}

	public String getArticleName() {
		return articleName;
	}

	public void setArticleName(String articleName) {
		this.articleName = articleName;
	}

	public Integer getCategoryOid() {
		return categoryOid;
	}

	public void setCategoryOid(Integer categoryOid) {
		this.categoryOid = categoryOid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getPurchasedBy() {
		return purchasedBy;
	}

	public void setPurchasedBy(String purchasedBy) {
		this.purchasedBy = purchasedBy;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
	public void addDocument(List<Document> docs){
		if(this.documents == null ){
			this.documents = new ArrayList<>();
		}
		this.documents.addAll(docs);
	}
}