package com.shareart.service.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable {
	private static final long serialVersionUID = -1297615617937370592L;
	private Integer categoryOid;
	private String categoryName;
	private String subCategoryName;
	private Integer parentCategoryOid;

	private List<Category> subCategorie;

	public Integer getCategoryOid() {
		return categoryOid;
	}

	public void setCategoryOid(Integer categoryOid) {
		this.categoryOid = categoryOid;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getSubCategoryName() {
		return subCategoryName;
	}

	public void setSubCategoryName(String subCategoryName) {
		this.subCategoryName = subCategoryName;
	}

	public Integer getParentCategoryOid() {
		return parentCategoryOid;
	}

	public void setParentCategoryOid(Integer parentCategoryOid) {
		this.parentCategoryOid = parentCategoryOid;
	}

	public List<Category> getSubCategorie() {
		return subCategorie;
	}

	public void setSubCategorie(List<Category> subCategorie) {
		this.subCategorie = subCategorie;
	}

}
