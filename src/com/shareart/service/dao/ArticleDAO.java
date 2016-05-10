package com.shareart.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.shareart.service.domain.Address;
import com.shareart.service.domain.Article;
import com.shareart.service.domain.Category;
import com.shareart.service.domain.Document;
import com.shareart.service.domain.User;
import com.shareart.service.response.ArticleResponse;
import com.shareart.service.response.ArticleResponseHandler;
import com.shareart.service.util.EmailHelper;
import com.shareart.service.util.FileHelper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;

public class ArticleDAO extends ServiceDAO {
	private static Logger log = Logger.getLogger(ArticleDAO.class);

	private final String QUERY_CREATE_ARTICLE = "INSERT INTO article (USER_OID, ARTICLE_NAME, CATEGORY_OID, DESCRIPTION, PRICE, STATE, ACTIVE, REF) VALUES (?, ?, ?, ?, ?, '1', '0', ?)";
	private final String QUERY_GET_ARTICLE_OID = "SELECT ARTICLE_OID FROM article WHERE REF = ? ";
	private final String QUERY_ACTIVATE_ARTICLE = "UPDATE article SET ACTIVE= 1 WHERE ARTICLE_OID = ? ";
	private final String QUERY_FETCH_ARTICLE_USER = "SELECT artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG, puser.FIRST_NAME PFIRST_NAME, puser.LAST_NAME PLAST_NAME, puser.EMAIL PEMAIL, puser.PHONE PPHONE FROM category ctgry, user user, article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID LEFT OUTER JOIN user puser on artcl.PURCHASED_BY = puser.USER_OID WHERE artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.USER_OID = user.USER_OID  AND artcl.USER_OID = ? order by doc.DOC_OID";
	private final String QUERY_FETCH_ARTICLE_USER_PURCHASED = "SELECT artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG, user.FIRST_NAME, user.LAST_NAME, user.EMAIL FROM category ctgry, user user, article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID WHERE artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.user_oid = user.user_oid AND artcl.PURCHASED_BY = ? order by doc.DOC_OID";
	private final String QUERY_FETCH_ARTICLE_CATEGORY = "SELECT artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG, user.FIRST_NAME, user.LAST_NAME, user.EMAIL FROM category ctgry, user user, address addr, (SELECT zipcode,( 6371 * acos(cos(radians(a.lat))*cos(radians(latitude))*cos(radians(longitude)-radians(a.lng))+sin(radians(a.lat))*sin(radians(latitude)))) AS distance FROM zipcodes, (SELECT latitude as lat, longitude as lng FROM zipcodes WHERE zipcode in (SELECT addr.zip_code FROM user usr, address addr WHERE usr.address_oid = addr.address_oid AND usr.user_oid = ?)  ) a HAVING distance <= ?) zipc, article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID and doc.DEFAULT_FLAG = 1 LEFT OUTER JOIN user puser on artcl.PURCHASED_BY = puser.USER_OID WHERE	artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.USER_OID = user.USER_OID and user.address_oid = addr.address_oid and addr.zip_code =  zipc.zipcode and user.user_oid <>? and (user.parent_oid is NULL OR user.parent_oid <> ?) AND artcl.state = 1 and artcl.active = 1 ";
	private final String QUERY_FETCH_ARTICLE = "SELECT artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG, user.FIRST_NAME, user.LAST_NAME, user.EMAIL, puser.FIRST_NAME PFIRST_NAME, puser.LAST_NAME PLAST_NAME, puser.EMAIL PEMAIL, puser.PHONE PPHONE FROM category ctgry, user user, article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID and doc.DEFAULT_FLAG = 1 LEFT OUTER JOIN user puser on artcl.PURCHASED_BY = puser.USER_OID WHERE artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.USER_OID = user.USER_OID AND artcl.ARTICLE_OID = ? ";
	private final String QUERY_FETCH_ARTICLE_STATE = "SELECT STATE FROM article WHERE article_oid = ?";
	private final String QUERY_FETCH_ARTICLE_PURCHASED = "SELECT artcl.*,ctgry.*,user.FIRST_NAME, user.LAST_NAME, user.EMAIL, puser.FIRST_NAME PFIRST_NAME, puser.LAST_NAME PLAST_NAME, puser.EMAIL PEMAIL, puser.PHONE PPHONE, adr.* FROM article artcl, category ctgry, user user, user puser, address adr WHERE artcl.category_oid = ctgry.category_oid AND artcl.user_oid = user.user_oid AND artcl.purchased_by=puser.user_oid AND puser.address_oid = adr.address_oid AND artcl.article_oid =?";
	private final String QUERY_FETCH_ARTICLE_DOCUMENT = "SELECT doc.* FROM document doc WHERE doc.ARTICLE_OID = ?";
	private final String QUERY_FETCH_DOCUMENT = "SELECT doc.* FROM document doc WHERE doc.DOC_OID = ?";
	private final String QUERY_MARK_ALL_DOCUMENT_NONDEFAULT = "UPDATE document d JOIN document d1 ON d.article_oid = d1.article_oid SET d1.DEFAULT_FLAG = 0 WHERE d.doc_oid = ?";
	private final String QUERY_MARK_DOCUMENT_DEFAULT = "UPDATE document SET DEFAULT_FLAG=1 WHERE DOC_OID = ?";
	private final String QUERY_PURCHASE_ARTICLE = "UPDATE article set PURCHASED_BY= ?, STATE=3 WHERE ARTICLE_OID= ?";
	private final String QUERY_CANCEL_PURCHASE = "UPDATE article set PURCHASED_BY= null, STATE=1 WHERE ARTICLE_OID= ?";
	private final String QUERY_ARTICLE_AVAILABLE = "UPDATE article set PURCHASED_BY= null, STATE=1 WHERE ARTICLE_OID= ?";
	private final String QUERY_ARTICLE_SOLD = "UPDATE article set STATE=2 WHERE ARTICLE_OID= ?";
	private final String QUERY_DELETE_ARTICLE = "DELETE FROM article WHERE ARTICLE_OID = ?";
	private final String QUERY_UPDATE_ARTICLE = "UPDATE article SET USER_OID= ?, ARTICLE_NAME=?, CATEGORY_OID=?, DESCRIPTION=?, PRICE=? WHERE ARTICLE_OID = ? ";
	
	
	private final String QUERY_CREATE_DOCUMENT = "INSERT INTO document (ARTICLE_OID, DOC_NAME, DEFAULT_FLAG, PHYSICAL_PATH, DOC_URL, ACTIVE, REF) VALUES (?, ?, ?, ?, ?, '1', ?)";
	private final String QUERY_GET_DOCUMENT_OID = "SELECT DOC_OID FROM document WHERE REF = ? ";
	
	private final String QUERY_DELETE_DOCUMENT = "DELETE FROM document WHERE DOC_OID= ?";
	
	private final int ARTICLE_BLOCKED = 2;
	
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
	
	public void updateArticle(Article article, int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			JsonArray articleData = new JsonArray();
			articleData.add(article.getUserOid()).add(article.getArticleName()).add(article.getCategoryOid());
			
			if(article.getDescription() != null){
				articleData.add(article.getDescription());
			}
			if(article.getPrice() != null){
				articleData.add(article.getPrice());
			}
			
			SQLConnection connection = conn.result();
			ArticleResponse articleResponse = new ArticleResponse();
			articleResponse.setArticleOid(articleOid);
			connection.updateWithParams(QUERY_UPDATE_ARTICLE, articleData.add(articleOid), updateRs -> {
				if (updateRs.succeeded()) {
					if(updateRs.result().getUpdated()>0){
						articleRespHandler.process(articleResponse,SUCCESS , "article updated successfully");
						// and close the connection
						this.closeConnection(connection);
						//TODO delete physical file if exist
					}else{
						articleRespHandler.process(articleResponse,SUCCESS , "no article found");
						// and close the connection
						this.closeConnection(connection);
					}
				} else {
					articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
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
	
	public void deleteDocument(int docOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			ArticleResponse articleResponse = new ArticleResponse();
			articleResponse.setDocumentOid(docOid);
			// fetch document 
			connection.queryWithParams(QUERY_FETCH_DOCUMENT, new JsonArray().add(docOid), docRs -> {
				if (docRs.succeeded() && docRs.result().getRows().size() > 0) {
					Document doc = getDocument(docRs.result().getRows().get(0), articleRespHandler.getBaseUrl());
					doc.setPhysicalPath(docRs.result().getRows().get(0).getString("PHYSICAL_PATH"));
					connection.updateWithParams(QUERY_DELETE_DOCUMENT, new JsonArray().add(docOid), updateRs -> {
						if (updateRs.succeeded()) {
							articleRespHandler.process(articleResponse,SUCCESS , "document deleted successfully");
							// and close the connection
							this.closeConnection(connection);
							// delete physical file if exist
							FileHelper.delteFile(doc.getPhysicalPath());
						} else {
							articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
							// and close the connection
							this.closeConnection(connection);
						}
					});						
				} else {
					articleRespHandler.process(articleResponse,SUCCESS , "no document found to delete");
					// and close the connection
					this.closeConnection(connection);
				}
			});			
		});
	}
	
	public void markDocAsDefault(int docOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			ArticleResponse articleResponse = new ArticleResponse();
			articleResponse.setDocumentOid(docOid);
			
			connection.setAutoCommit(false, connRs ->{
				if(connRs.succeeded()){
					// #1 mark all documents of an article as default as 0
					connection.updateWithParams(QUERY_MARK_ALL_DOCUMENT_NONDEFAULT, new JsonArray().add(docOid), updateDocRS -> {
						if (updateDocRS.succeeded()) {
							// #2 then mark a particular document as default
							connection.updateWithParams(QUERY_MARK_DOCUMENT_DEFAULT,  new JsonArray().add(docOid), rs -> {
								if (rs.succeeded()) {
									// commit 
									connection.commit(null);
									articleRespHandler.process(articleResponse,SUCCESS , "document marked as default");
									// and close the connection
									this.closeConnection(connection);								
								} else {
									articleRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
									// and close the connection
									this.closeConnection(connection);
								}								
							});									
						} else {
							articleRespHandler.process(null, APP_ERROR, updateDocRS.cause().getMessage());
							// and close the connection
							this.closeConnection(connection);
						}
					});
				} else {
					articleRespHandler.process(null, APP_ERROR, connRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}				
			});
		});
	}

	public void acitvateArticle(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
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
					// and close the connection
					this.closeConnection(connection);
				}
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
							article = this.getArticle(articleRs.result().getRows().get(0),articleRespHandler.getBaseUrl());
							log.info("Article ::" + article.getArticleOid());
						} catch (Exception e) {
							log.error(e);
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
								article = this.getArticle(data,articleRespHandler.getBaseUrl(),false,true);
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
	
	public void fetchArticlePurchasedByUser(int userOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			// get articles purchased by user based on useOid 
			connection.queryWithParams(QUERY_FETCH_ARTICLE_USER_PURCHASED, new JsonArray().add(userOid), articleRs -> {
				if (articleRs.succeeded()) {
					ArticleResponse articleResponse = new ArticleResponse();
					try {
						Article article = null;
						List<Article> articles = new ArrayList<>();
						Map<Integer, Article> articlesMap = new HashMap<>();
						for (JsonObject data : articleRs.result().getRows()) {
							try {
								article = this.getArticle(data,articleRespHandler.getBaseUrl(),true,false);
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
						articleRespHandler.process(articleResponse, SUCCESS, articles.size() +" Articles purchased by User");
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
	/*
	SELECT 
		artcl.*, ctgry.*, doc.DOC_OID, doc.DOC_NAME, doc.DOC_URL, doc.DEFAULT_FLAG, user.FIRST_NAME, user.LAST_NAME, user.EMAIL
	FROM 
		category ctgry, user user, 
		address addr,
		(SELECT 
			zipcode, 
			( 6371 * acos(cos(radians(a.lat))*cos(radians(latitude))*cos(radians(longitude)-radians(a.lng))+sin(radians(a.lat))*sin(radians(latitude)))) AS distance 
		FROM 
			zipcodes,
			(select latitude as lat, longitude as lng from zipcodes where zipcode in (select addr.zip_code from user usr, address addr where usr.address_oid = addr.address_oid and usr.user_oid = 1)  ) a
		HAVING 
			distance <= 10) zipc,
		article artcl LEFT OUTER JOIN document doc on artcl.ARTICLE_OID = doc.ARTICLE_OID and doc.DEFAULT_FLAG = 1 
		LEFT OUTER JOIN user puser on artcl.PURCHASED_BY = puser.USER_OID 
	WHERE 
		artcl.CATEGORY_OID = ctgry.CATEGORY_OID AND artcl.USER_OID = user.USER_OID and user.address_oid = addr.address_oid and addr.zip_code =  zipc.zipcode and user.user_oid <>1
	 */
	
	public void fetchArticleByZipCodeCategory(int userOid,int categoryOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			
			String query = QUERY_FETCH_ARTICLE_CATEGORY;
			JsonArray queryData = new JsonArray().add(userOid).add(10).add(userOid).add(userOid);
			
			if(categoryOid >0){
				query = query + " and artcl.category_Oid =? ";
				queryData.add(categoryOid);
			}
			// get articles based on logged in user zip code and category 
			connection.queryWithParams(query, queryData, articleRs -> {
				if (articleRs.succeeded()) {
					ArticleResponse articleResponse = new ArticleResponse();
					try {
						Article article = null;
						List<Article> articles = new ArrayList<>();
						Map<Integer, Article> articlesMap = new HashMap<>();
						for (JsonObject data : articleRs.result().getRows()) {
							try {
								article = this.getArticle(data,articleRespHandler.getBaseUrl(),true,false);
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
						articleRespHandler.process(articleResponse, SUCCESS, articles.size() +" Articles found");
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
								doc = this.getDocument(data, articleRespHandler.getBaseUrl());
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
	
	public void purchaseArticle(int articleOid, int purchaserUserOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			// get article state to check is this already purchased
			connection.queryWithParams(QUERY_FETCH_ARTICLE_STATE, new JsonArray().add(articleOid), rs -> {
				if (rs.succeeded()) {
					if(rs.result().getRows().size()==0){
						articleRespHandler.process(null, ARTICLE_BLOCKED, "No article found with articleOid :: "+articleOid);
					}else if(rs.result().getRows().get(0).getInteger("STATE") != 1) {
						articleRespHandler.process(null, ARTICLE_BLOCKED, "Articled not available for purchase");
					}else{
						// if not yet purchased then go ahead and mark it blocked
						connection.updateWithParams(QUERY_PURCHASE_ARTICLE, new JsonArray().add(purchaserUserOid).add(articleOid), updateRs -> {
							if (updateRs.succeeded()) {
								articleRespHandler.process(null, SUCCESS, "article purchase request successful");
								// send purchase EMAIL to owner and requester
								getJdbcClient().getConnection(arctConn -> {
									if (arctConn.failed()) {
										log.error("Not abel to send email");
									}
									SQLConnection artclConnection = arctConn.result();
									artclConnection.queryWithParams(QUERY_FETCH_ARTICLE_PURCHASED, new JsonArray().add(articleOid), artclRS -> {
										if (artclRS.succeeded()) {
											JsonObject jsonObject = artclRS.result().getRows().get(0);
											// get article/owner/requestor data
											Article article = new Article();
											User owner = new User();
											User purchansedUser = new User();
											Category category = new Category();
											Address addr = new Address();
											
											article.setArticleOid(jsonObject.getInteger("ARTICLE_OID"));
											article.setArticleName(jsonObject.getString("ARTICLE_NAME"));
											article.setDescription(jsonObject.getString("DESCRIPTION"));
											article.setPrice(jsonObject.getDouble("PRICE"));
											
											//Owner.setUserOid(jsonObject.getInteger("USER_OID"));
											//owner.setUserId(jsonObject.getString("USER_ID"));
											owner.setEmail(jsonObject.getString("EMAIL"));
											owner.setFirstName(jsonObject.getString("FIRST_NAME"));
											owner.setLastName(jsonObject.getString("LAST_NAME"));
											article.setUser(owner);
										
											purchansedUser.setUserOid(new Integer(jsonObject.getString("PURCHASED_BY")));
											purchansedUser.setEmail(jsonObject.getString("PEMAIL"));
											purchansedUser.setPhone(jsonObject.getString("PPHONE"));
											purchansedUser.setFirstName(jsonObject.getString("PFIRST_NAME"));
											purchansedUser.setLastName(jsonObject.getString("PLAST_NAME"));

											category.setCategoryOid(jsonObject.getInteger("CATEGORY_OID"));
											if(jsonObject.getInteger("PARENT_CATEGORY_OID") == null){
												category.setCategoryName(jsonObject.getString("CATEGORY_NAME"));
											} else{
												category.setCategoryName(jsonObject.getString("SUB_CATEGORY_NAME"));
											}
											article.setCategory(category);
											
											addr.setCity(jsonObject.getString("CITY"));
											addr.setCountry(jsonObject.getString("COUNTRY"));
											addr.setLine1(jsonObject.getString("LINE1"));
											addr.setLine2(jsonObject.getString("LINE2"));
											addr.setState(jsonObject.getString("STATE"));
											addr.setZipCode(jsonObject.getString("ZIP_CODE"));
											purchansedUser.setAddress(addr);
											
											// and close the connection
											this.closeConnection(artclConnection);
											// send purchase EMAIL to owner and requester
											EmailHelper.sendPurchaseArticleMail(owner, article, purchansedUser);
										} else {
											// and close the connection
											this.closeConnection(artclConnection);
										}
									});
								});
							} else {
								articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
							}
						});
					}
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});			
		});
	}
	
	public void cancelPurchase(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			connection.updateWithParams(QUERY_CANCEL_PURCHASE, new JsonArray().add(articleOid), updateRs -> {
				if (updateRs.succeeded()) {
					articleRespHandler.process(null, SUCCESS, "Purchase canceled");
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void markArticleAvailable(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			connection.updateWithParams(QUERY_ARTICLE_AVAILABLE, new JsonArray().add(articleOid), updateRs -> {
				if (updateRs.succeeded()) {
					articleRespHandler.process(null, SUCCESS, "article available for Sale");
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void markArticleSold(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			connection.updateWithParams(QUERY_ARTICLE_SOLD, new JsonArray().add(articleOid), updateRs -> {
				if (updateRs.succeeded()) {
					articleRespHandler.process(null, SUCCESS, "article marked as sold");
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void deleteArticle(int articleOid, ArticleResponseHandler articleRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				articleRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			connection.updateWithParams(QUERY_DELETE_ARTICLE, new JsonArray().add(articleOid), updateRs -> {
				if (updateRs.succeeded()) {
					articleRespHandler.process(null, SUCCESS, "article deleted");
					// and close the connection
					this.closeConnection(connection);
				} else {
					articleRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}

	private Article getArticle(JsonObject jsonObject, String baseUrl) {
		return  getArticle(jsonObject,baseUrl, true,true);
	}
	
	private Article getArticle(JsonObject jsonObject, String baseurl, boolean addUserData, boolean addPurchasedUserData) {
		Article article = new Article();
		Category category = new Category();
		User user = new User();
		User purchansedUser = new User();
		Document doc = new Document();
		List<Document> docs = new ArrayList<>();
		article.setArticleOid(jsonObject.getInteger("ARTICLE_OID"));
		article.setArticleName(jsonObject.getString("ARTICLE_NAME"));
		article.setDescription(jsonObject.getString("DESCRIPTION"));
		article.setPrice(jsonObject.getDouble("PRICE"));
		article.setState(jsonObject.getInteger("STATE"));
		//article.setPurchasedBy(jsonObject.getString("PURCHASED_BY"));
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
		
		if(addPurchasedUserData && jsonObject.getString("PURCHASED_BY") !=null && !jsonObject.getString("PURCHASED_BY").isEmpty()){
			purchansedUser.setUserOid(new Integer(jsonObject.getString("PURCHASED_BY")));
			purchansedUser.setEmail(jsonObject.getString("PEMAIL"));
			purchansedUser.setFirstName(jsonObject.getString("PFIRST_NAME"));
			purchansedUser.setLastName(jsonObject.getString("PLAST_NAME"));
			article.setPurchasedUser(purchansedUser);
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
			doc.setDocUrl(baseurl+jsonObject.getString("DOC_URL"));
			doc.setDefaultFlag(jsonObject.getInteger("DEFAULT_FLAG"));
			docs.add(doc);
			article.setDocuments(docs);
		}

		article.setCategory(category);

		return article;
	}
	
	private Document getDocument(JsonObject jsonObject, String baseurl) {
		Document doc = new Document();
		//doc.setArticleOid(jsonObject.getInteger("ARTICLE_OID"));
		doc.setDocOid(jsonObject.getInteger("DOC_OID"));
		doc.setDocName(jsonObject.getString("DOC_NAME"));
		doc.setDocUrl(baseurl+jsonObject.getString("DOC_URL"));
		doc.setDefaultFlag(jsonObject.getInteger("DEFAULT_FLAG"));
		
		return doc;
	}
}