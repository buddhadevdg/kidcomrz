package com.shareart.service.response;

import java.io.Serializable;
import java.util.List;

import com.shareart.service.domain.Article;
import com.shareart.service.domain.Document;

public class ArticleResponse implements Serializable {

	private static final long serialVersionUID = 9083585173764306950L;

	private Integer articleOid;
	
	private Integer documentOid;
	
	private Article article;

	private List<Article> articles;
	
	private List<Document> dcos;
	
	private String docUrl;

	public Integer getArticleOid() {
		return articleOid;
	}

	public void setArticleOid(Integer articleOid) {
		this.articleOid = articleOid;
	}

	public Article getArticle() {
		return article;
	}
	
	public Integer getDocumentOid() {
		return documentOid;
	}

	public void setDocumentOid(Integer documentOid) {
		this.documentOid = documentOid;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public List<Article> getArticles() {
		return articles;
	}

	public void setArticles(List<Article> articles) {
		this.articles = articles;
	}

	public List<Document> getDcos() {
		return dcos;
	}

	public void setDcos(List<Document> dcos) {
		this.dcos = dcos;
	}

	public String getDocUrl() {
		return docUrl;
	}

	public void setDocUrl(String docUrl) {
		this.docUrl = docUrl;
	}
}