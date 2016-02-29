package com.shareart.service.domain;

import java.io.Serializable;

public class Document implements Serializable {

	private static final long serialVersionUID = -2471750858984170622L;
	// DOC_OID
	private Integer docOid;
	// ARTICLE_OID
	private Integer articleOid;
	// DOC_NAME
	private String docName;
	// DEFAULT_FLAGs
	private Integer defaultFlag;
	// PHYSICAL_PATH
	private String physicalPath;
	// DOC_URL
	private String docUrl;
	// ACTIVE
	private Integer active;
	
	private Article article;

	public Integer getDocOid() {
		return docOid;
	}

	public void setDocOid(Integer docOid) {
		this.docOid = docOid;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public Integer getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Integer defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public String getPhysicalPath() {
		return physicalPath;
	}

	public void setPhysicalPath(String physicalPath) {
		this.physicalPath = physicalPath;
	}

	public String getDocUrl() {
		return docUrl;
	}

	public void setDocUrl(String docUrl) {
		this.docUrl = docUrl;
	}

	public Integer getActive() {
		return active;
	}

	public void setActive(Integer active) {
		this.active = active;
	}

	public Integer getArticleOid() {
		return articleOid;
	}

	public void setArticleOid(Integer articleOid) {
		this.articleOid = articleOid;
	}
}