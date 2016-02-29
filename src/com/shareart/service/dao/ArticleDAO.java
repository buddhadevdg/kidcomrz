package com.shareart.service.dao;

import static com.shareart.service.response.ArticleResponseHandler.SUCCESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareart.service.domain.Article;
import com.shareart.service.domain.Category;
import com.shareart.service.domain.Document;
import com.shareart.service.domain.User;
import com.shareart.service.response.ArticleResponse;
import com.shareart.service.response.ArticleResponseHandler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;

public class ArticleDAO extends ServiceDAO {
	private static Logger log = Logger.getLogger(ArticleDAO.class);

	private String QUERY_CREATE_ARTICLE = "INSERT INTO article (USER_OID, ARTICLE_NAME, CATEGORY_OID, DESCRIPTION, PRICE, STATE, ACTIVE, REF) VALUES (?, ?, ?, ?, ?, '1', '0', ?)";
	private String QUERY_GET_ARTICLE_OID = "SELECT ARTICLE_OID FROM article WHERE REF = ? ";
	private String QUERY_ACTIVATE_ARTICLE = "UPDATE article SET ACTIVE= 1 WHERE ARTICLE_OID = ? ";
	private String QUERY_FETCH_ARTICLE_USER = "SELECT artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG FROM category ctgry, user user, article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID WHERE artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.USER_OID = user.USER_OID  AND artcl.USER_OID = ? order by doc.DOC_OID";
	private String QUERY_FETCH_ARTICLE = "SELECT artcl.*,ctgry.*, user.USER_OID,user.FIRST_NAME,user.LAST_NAME,user.EMAIL,user.DOB, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG FROM category ctgry, user user, article artcl LEFT OUTER JOIN document doc ON artcl.ARTICLE_OID = doc.ARTICLE_OID AND doc.DEFAULT_FLAG = 1 WHERE artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND  artcl.USER_OID = user.USER_OID AND artcl.ARTICLE_OID = ?";
	private String QUERY_FETCH_ARTICLE_DOCUMENT = "SELECT doc.* FROM document doc WHERE doc.ARTICLE_OID = ?";

	private String QUERY_CREATE_DOCUMENT = "INSERT INTO document (ARTICLE_OID, DOC_NAME, DEFAULT_FLAG, PHYSICAL_PATH, DOC_URL, ACTIVE, REF) VALUES (?, ?, ?, ?, ?, '1', ?)";
	private String QUERY_GET_DOCUMENT_OID = "SELECT DOC_OID FROM document WHERE REF = ? ";
	
	private Gson gson = new GsonBuilder().setDateFormat("dd/MMM/yyyy HH:mm:ss zzz").create();

	public void addArticle(Article article, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			long addRef = System.currentTimeMillis();

			JsonArray articleData = new JsonArray();
			articleData.add(article.getUserOid()).add(article.getArticleName()).add(article.getCategoryOid());
			
			if(article.getDescription() != null){
				articleData.add(article.getDescription());
			}
			if(article.getPrice() != null){
				articleData.add(article.getPrice());
			}
			
			articleData.add(addRef);

			// insert article
			connection.updateWithParams(QUERY_CREATE_ARTICLE, articleData, rs -> {
				if (rs.succeeded()) {
					// get articleOid for newly added article
					connection.queryWithParams(QUERY_GET_ARTICLE_OID, new JsonArray().add(addRef), rsArticle -> {
						if (rsArticle.succeeded()) {
							int article_oid = rsArticle.result().getRows().get(0).getInteger("ARTICLE_OID");
							log.info("newly created article with article_oid :: " + article_oid);
							ArticleResponse articleResponse = new ArticleResponse();
							articleResponse.setArticleOid(article_oid);
							articleRespHandler.process(articleResponse, SUCCESS, "Article created successfully");
						} else {
							articleRespHandler.process(null, APP_ERROR, rsArticle.cause().getMessage());
						}
						// and close the connection
						this.closeConnection(connection);
					});
				} else {
					articleRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});

	}

	public void addDocument(Document document, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			long addRef = System.currentTimeMillis();

			JsonArray docData = new JsonArray();
			docData.add(document.getArticleOid()).add(document.getDocName());
			if(document.getDefaultFlag() != null){
				docData.add(document.getDefaultFlag());
			}
			if(document.getPhysicalPath() != null){
				docData.add(document.getPhysicalPath());
			}
			if(document.getDocUrl() != null){
				docData.add(document.getDocUrl());
			}
			docData.add(addRef);

			// insert article
			connection.updateWithParams(QUERY_CREATE_DOCUMENT, docData, rs -> {
				if (rs.succeeded()) {
					// get doc_oid for newly added document
					connection.queryWithParams(QUERY_GET_DOCUMENT_OID, new JsonArray().add(addRef), rsDoc -> {
						if (rsDoc.succeeded()) {
							int docOid = rsDoc.result().getRows().get(0).getInteger("DOC_OID");
							log.info("newly created document with doc_oid :: " + docOid);
							ArticleResponse articleResponse = new ArticleResponse();
							articleResponse.setDocumentOid(docOid);
							articleResponse.setDocUrl(document.getDocUrl());
							articleRespHandler.process(articleResponse, SUCCESS, "Docuemnt uploaded successfully");
						} else {
							articleRespHandler.process(null, APP_ERROR, rsDoc.cause().getMessage());
						}
						// and close the connection
						this.closeConnection(connection);
					});
				} else {
					articleRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}

	public void acitvateArticle(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.sendError(500, "DB connection Error");
				;
				return;
			}
			SQLConnection connection = conn.result();

			connection.updateWithParams(QUERY_ACTIVATE_ARTICLE, new JsonArray().add(articleOid), updateRs -> {
				ArticleResponse articleResponse = new ArticleResponse();
				articleResponse.setArticleOid(articleOid);
				if (updateRs.succeeded()) {
					articleRespHandler.process(articleResponse, SUCCESS, "Article Activated successfully");
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(articleResponse, APP_ERROR, "Failed to Activate Article");
				}
				// and close the connection
				this.closeConnection(connection);
			});
		});
	}

	public void fetchArticle(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}

			SQLConnection connection = conn.result();

			// get user information based on userid/password
			connection.queryWithParams(QUERY_FETCH_ARTICLE, new JsonArray().add(articleOid), articleRs -> {
				if (articleRs.succeeded()) {
					ArticleResponse articleResponse = new ArticleResponse();
					try {
						Article article = null;
						try {
							article = this.getArticle(articleRs.result().getRows().get(0));
							log.info("Article ::" + article.getArticleOid());
						} catch (Exception e) {
							log.error(e);
							e.printStackTrace();
						}
						
						articleResponse.setArticle(article);
						articleRespHandler.process(articleResponse, SUCCESS, (article == null ? "No " : "")+"Article Found");
					} catch (Exception e) {
						log.error(e);
					}
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, articleRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});

		});
	}

	public void fetchArticleByUser(int userOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			// get user information based on userid/password
			connection.queryWithParams(QUERY_FETCH_ARTICLE_USER, new JsonArray().add(userOid), articleRs -> {
				if (articleRs.succeeded()) {
					ArticleResponse articleResponse = new ArticleResponse();
					try {
						Article article = null;
						List<Article> articles = new ArrayList<>();
						Map<Integer, Article> articlesMap = new HashMap<>();
						for (JsonObject data : articleRs.result().getRows()) {
							try {
								article = this.getArticle(data,false);
								if(articlesMap.get(article.getArticleOid()) == null){
									articlesMap.put(article.getArticleOid(), article);
									articles.add(article);
								}else {
									if(article.getDocuments() != null){
										articlesMap.get(article.getArticleOid()).addDocument(article.getDocuments());;
									}
								}
								log.info("Article ::" + article.getArticleOid());								
							} catch (Exception e) {
								log.error(e);
							}
						}		
						articleResponse.setArticles(articles);
						articleRespHandler.process(articleResponse, SUCCESS, articles.size() +" Articles found for User");
					} catch (Exception e) {
						log.error(e);
					}
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, articleRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void fetchArticleDocument(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();

			// get user information based on userid/password
			connection.queryWithParams(QUERY_FETCH_ARTICLE_DOCUMENT, new JsonArray().add(articleOid), docRs -> {
				if (docRs.succeeded()) {
					ArticleResponse articleResponse = new ArticleResponse();
					try {
						Document doc = null;
						List<Document> docs = new ArrayList<>();
						for (JsonObject data : docRs.result().getRows()) {
							try {
								doc = this.getDocument(data);
								docs.add(doc);
								log.info("Document ::" + doc.getDocName());
							} catch (Exception e) {
								log.error(e);
							}
						}		
						articleResponse.setDcos(docs);
						articleRespHandler.process(articleResponse, SUCCESS, docs.size() +" Docuemnt found for Article");
					} catch (Exception e) {
						log.error(e);
					}
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, docRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}

	private Article getArticle(JsonObject jsonObject) {
		return  getArticle(jsonObject, true);
	}
	
	private Article getArticle(JsonObject jsonObject, boolean addUserData) {
		Article article = new Article();
		Category category = new Category();
		User user = new User();
		Document doc = new Document();
		List<Document> docs = new ArrayList<>();
		article.setArticleOid(jsonObject.getInteger("ARTICLE_OID"));
		article.setArticleName(jsonObject.getString("ARTICLE_NAME"));
		article.setDescription(jsonObject.getString("DESCRIPTION"));
		article.setPrice(jsonObject.getDouble("PRICE"));
		article.setState(jsonObject.getInteger("STATE"));
		article.setPurchasedBy(jsonObject.getString("PURCHASED_BY"));
		article.setActive(jsonObject.getInteger("ACTIVE"));
		
		if(addUserData){
			user.setUserOid(jsonObject.getInteger("USER_OID"));
			user.setUserId(jsonObject.getString("USER_ID"));
			user.setDob(jsonObject.getString("DOB"));
			user.setEmail(jsonObject.getString("EMAIL"));
			user.setFirstName(jsonObject.getString("FIRST_NAME"));
			user.setLastName(jsonObject.getString("LAST_NAME"));
			user.setPhone(jsonObject.getString("PHONE"));
			article.setUser(user);
		}

		category.setCategoryOid(jsonObject.getInteger("CATEGORY_OID"));
		//category.setCategoryName(jsonObject.getString("CATEGORY_NAME"));
		//category.setSubCategoryName(jsonObject.getString("SUB_CATEGORY_NAME"));
		if(jsonObject.getInteger("PARENT_CATEGORY_OID") == null){
			category.setCategoryName(jsonObject.getString("CATEGORY_NAME"));
		} else{
			category.setCategoryName(jsonObject.getString("SUB_CATEGORY_NAME"));
		}
		category.setParentCategoryOid(jsonObject.getInteger("PARENT_CATEGORY_OID"));
		
		if(jsonObject.getInteger("DOC_OID") != null){
			doc.setDocOid(jsonObject.getInteger("DOC_OID"));
			doc.setDocName(jsonObject.getString("DOC_NAME"));
			doc.setDocUrl(jsonObject.getString("DOC_URL"));
			doc.setDefaultFlag(jsonObject.getInteger("DEFAULT_FLAG"));
			docs.add(doc);
			article.setDocuments(docs);
		}

		article.setCategory(category);

		return article;
	}
	
	private Document getDocument(JsonObject jsonObject) {
		Document doc = new Document();
		//doc.setArticleOid(jsonObject.getInteger("ARTICLE_OID"));
		doc.setDocOid(jsonObject.getInteger("DOC_OID"));
		doc.setDocName(jsonObject.getString("DOC_NAME"));
		doc.setDocUrl(jsonObject.getString("DOC_URL"));
		doc.setDefaultFlag(jsonObject.getInteger("DEFAULT_FLAG"));
		
		return doc;
	}
}