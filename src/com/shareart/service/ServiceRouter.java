package com.shareart.service;

import static com.shareart.service.ServiceConstants.CLIENT_AUTH_FAILED;
import static com.shareart.service.ServiceConstants.CLIENT_AUTH_MISSING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareart.service.dao.ArticleDAO;
import com.shareart.service.dao.ServiceDAO;
import com.shareart.service.dao.UserDAO;
import com.shareart.service.domain.Article;
import com.shareart.service.domain.Document;
import com.shareart.service.domain.User;
import com.shareart.service.response.ArticleResponseHandler;
import com.shareart.service.response.CategoryResponseHandler;
import com.shareart.service.response.CountryStateResponseHandler;
import com.shareart.service.response.UserResponseHandler;
import com.shareart.service.util.EmailHelper;
import com.shareart.vertx.ext.CustBodyHandlerImpl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class ServiceRouter extends AbstractVerticle {
	private static Logger log = Logger.getLogger(ServiceRouter.class);
	Gson gson = new GsonBuilder().setDateFormat("dd/MMM/yyyy HH:mm:ss zzz").create();
	private UserDAO userDao = new UserDAO();
	private ServiceDAO  serviceDao = new ServiceDAO();
	private ArticleDAO articleDao = new ArticleDAO();

	private Properties prop = null;

	public static void main(String[] args) {
		String verticleID = ServiceRouter.class.getName();
		log.info("attempting to start ServiceRouter with verticle " + verticleID + "...............");
		Consumer<Vertx> runner = vertx -> {
			try {
				vertx.deployVerticle(verticleID);
			} catch (Throwable t) {
				log.error("Exception while deploying verticle: " + verticleID, t);
			}
		};
		Vertx vertx = Vertx.vertx(new VertxOptions());
		runner.accept(vertx);
	}

	@Override
	public void start() {
		Router router = Router.router(vertx);
		this.laodProperties();
		this.init();
		router.route().handler(new CustBodyHandlerImpl().setUploadsDirectory(prop.getProperty("upload.folder.path")));
		
		router.post("/service/user/add").handler(this::handleAddUser);
		router.post("/service/user/child/add").handler(this::handleAddChild);
		router.post("/service/user/login").handler(this::handleLoginUser);
		router.get("/service/user/activate/:userOid").handler(this::activateUser);
		
		//router.get("/service/state/get/:countryCode").handler(this::getStatesByCountry);
		router.get("/service/country/all").handler(this::getAllCountry);
		
		
		router.post("/service/article/add").handler(this::handleAddArticle);
		router.post("/service/article/document/add").handler(this::handleAddDocument);
		router.put("/service/article/done/:articleOid").handler(this::handleArticleCommit);
		router.get("/service/article/fetch/user/:userOid").handler(this::handleArticleFetchByUser);
		router.get("/service/article/fetch/:articleOid").handler(this::handleArticleFetch);
		router.get("/service/article/document/fetch/:articleOid").handler(this::handleArticleDocumentFetch);
		
		router.get("/service/category/all").handler(this::getAllCategory);
		
		router.get("/service/img/*").handler(this::getImage);
		
		//router.get("/service/*").handler(this::getImage);
		
		
		vertx.createHttpServer().requestHandler(router::accept).listen(Integer.valueOf(prop.getProperty("service.port")));

		log.info("Service started in port :: "+prop.getProperty("service.port"));
	}
	
	public void handleAddUser(RoutingContext routingContext){
		JsonObject userData = routingContext.getBodyAsJson();
		User user = gson.fromJson(userData.encode(), User.class);
		UserResponseHandler userRespHandler = new UserResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		System.out.println(user.getChildrens());
		userDao.addUser(user,userRespHandler);
	}

	public void handleAddChild(RoutingContext routingContext){
		JsonObject userData = routingContext.getBodyAsJson();
		User user = gson.fromJson(userData.encode(), User.class);
		UserResponseHandler userRespHandler = new UserResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		userDao.addChild(user,userRespHandler);
	}
	
	public void handleLoginUser(RoutingContext routingContext){
		JsonObject userData = routingContext.getBodyAsJson();
		User user = gson.fromJson(userData.encode(), User.class);
		UserResponseHandler userRespHandler = new UserResponseHandler(routingContext.response(), prop.getProperty("base.url"));
		userDao.login(user.getUserId(), user.getPassword(),userRespHandler);
	}
	
	public void activateUser(RoutingContext routingContext){
		UserResponseHandler userRespHandler = new UserResponseHandler(routingContext.response(), prop.getProperty("base.url"));
		int userOid;
		try {
			userOid = Integer.valueOf(routingContext.request().getParam("userOid"));
			userDao.acitvateUser(userOid, userRespHandler);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			userRespHandler.activationError();
		}
	}
	
	/*public void getStatesByCountry(RoutingContext routingContext){
		CountryStateResponseHandler utilResponseHandler = new CountryStateResponseHandler(routingContext.response());
		String countryCode;
		countryCode = routingContext.request().getParam("countryCode");
		log.debug("countryCode :: "+countryCode);
		serviceDAO.getStates(countryCode, utilResponseHandler);
	}*/
	
	public void getAllCountry(RoutingContext routingContext){
		CountryStateResponseHandler countryStateResponseHandler = new CountryStateResponseHandler(routingContext.response());
		serviceDao.getAllCountry(countryStateResponseHandler);
	}
	
	public void handleAddArticle(RoutingContext routingContext){
		JsonObject articleData = routingContext.getBodyAsJson();
		Article article = gson.fromJson(articleData.encode(), Article.class);
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		System.out.println(article.getDocuments());
		articleDao.addArticle(article,articleRespHandler);
	}
	
	public void handleAddDocument(RoutingContext routingContext){
		log.info("inside handleAddDocument ");
		Document document = null;
		try {
			JsonObject docData = routingContext.getBodyAsJson();		
			document = gson.fromJson(docData.encode(), Document.class);
			log.info("docuemnt data via json :: "+document.getDocName());
		} catch (Exception e) {
			//log.error(e);
		}
		try {
			log.info(routingContext.fileUploads().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (FileUpload file : routingContext.fileUploads()) {
			// in-case data not fetched via json
			if(document == null){
				document = new Document();
				document.setArticleOid(new Integer(routingContext.request().getParam("articleOid")));
				if(routingContext.request().getParam("defaultFlag") != null && routingContext.request().getParam("defaultFlag").equalsIgnoreCase("1")){
					document.setDefaultFlag(1);
				}else{
					document.setDefaultFlag(0);
				}
				document.setDocName(routingContext.request().getParam("docName"));
				log.info("docuemnt data via form param :: "+document.getDocName());
			}
			log.info("file name :: "+file.fileName());
			log.info("uploadedFileName :: "+file.uploadedFileName());
			log.info("File.separator :: "+File.separator);
			document.setDocUrl(prop.getProperty("base.url")+"img/"+file.uploadedFileName().substring(file.uploadedFileName().lastIndexOf(File.separator)+1));
			log.info("DocUrl ::"+document.getDocUrl());
			document.setPhysicalPath(file.uploadedFileName());
		}
		
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		articleDao.addDocument(document,articleRespHandler);
	}
	
	public void handleArticleCommit(RoutingContext routingContext){
		int articleOid = Integer.valueOf(routingContext.request().getParam("articleOid"));
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		articleDao.acitvateArticle(articleOid, articleRespHandler);
	}
	
	public void handleArticleFetch(RoutingContext routingContext){
		int articleOid = Integer.valueOf(routingContext.request().getParam("articleOid"));
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		articleDao.fetchArticle(articleOid, articleRespHandler);
	}
	
	public void handleArticleFetchByUser(RoutingContext routingContext){
		int userOid = Integer.valueOf(routingContext.request().getParam("userOid"));
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		articleDao.fetchArticleByUser(userOid, articleRespHandler);
	}
	
	public void handleArticleDocumentFetch(RoutingContext routingContext){
		int articleOid = Integer.valueOf(routingContext.request().getParam("articleOid"));
		ArticleResponseHandler articleRespHandler = new ArticleResponseHandler(routingContext.response(),prop.getProperty("base.url"));
		articleDao.fetchArticleDocument(articleOid, articleRespHandler);
	}
	
	public void getAllCategory(RoutingContext routingContext){
		CategoryResponseHandler categoryResponseHandler = new CategoryResponseHandler(routingContext.response());
		serviceDao.getAllCategory(categoryResponseHandler);
	}
	
	public void getImage(RoutingContext routingContext){
		String path = routingContext.request().uri();
		log.info("trying to fetch image from path :: "+path);
		log.info(prop.getProperty("upload.folder.path")+File.separator+path.substring(path.lastIndexOf("/")+1));
		routingContext.response().sendFile(prop.getProperty("upload.folder.path")+path.substring(path.lastIndexOf("/")),  arg0-> {
				if(arg0.failed()){
					log.error(arg0.cause());
					routingContext.response().setStatusCode(404).setStatusMessage(arg0.cause().getMessage()).end();
				}
			}
		);
	}

	/**
	 * do initial setup.
	 * 
	 * create a DataSource and set it to dao to be used to get data from
	 * database
	 */
	private void init() {
		String url = prop.getProperty("jdbc.url");
		String userName = prop.getProperty("jdbc.user.name");
		String pwd = prop.getProperty("jdbc.user.password");
		String driverClass = prop.getProperty("jdbc.driver.class");
		int minPoolSize = Integer.valueOf(prop.getProperty("jdbc.min.pool.size"));
		int maxPoolSize = Integer.valueOf(prop.getProperty("jdbc.max.pool.size"));
		int initialPoolSize = Integer.valueOf(prop.getProperty("jdbc.initial.pool.size"));

		// Create a JDBC client with a test database
		JDBCClient client = JDBCClient.createShared(vertx,
				new JsonObject().put("url", url).put("user", userName).put("password", pwd)
						.put("driver_class", driverClass).put("max_pool_size", maxPoolSize)
						.put("min_pool_size", minPoolSize).put("initial_pool_size", initialPoolSize));
		userDao.setJdbcClient(client);
		serviceDao.setJdbcClient(client);
		articleDao.setJdbcClient(client);
		log.info("datasource created ...");
		
		EmailHelper.init(vertx);
	}

	/**
	 * load claimService properties files
	 */
	private void laodProperties() {
		try {
			InputStream is = null;
			prop = new Properties();
			is = this.getClass().getResourceAsStream("/app.properties");
			prop.load(is);
		} catch (IOException e) {
			log.error("Exception while loading claimService.properties", e);
		}
	}

	/**
	 * this method <code>checkAuth<code> check is client has provided basic user
	 * credential. if not provided return false with HTTP code 401 to allow user
	 * to enter credentials. in case user credential are provided, validate if
	 * it matched with the valid claimService credentials if not return false
	 * with HTTP code 403.
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private boolean checkAuth(HttpServerRequest request, HttpServerResponse response) {
		String user;
		String pass;
		String[] credentials;
		String[] parts;
		String authStr = request.getHeader("authorization");
		if (authStr == null) {
			response.setStatusCode(CLIENT_AUTH_MISSING).headers().add("WWW-Authenticate", "Basic");
			response.end();
			return false;
		}

		try {
			parts = authStr.split(" ");
			credentials = new String(DatatypeConverter.parseBase64Binary(parts[1])).split(":");
			if (credentials == null || credentials.length == 0) {
				response.setStatusCode(CLIENT_AUTH_MISSING).headers().add("WWW-Authenticate", "Basic");
				response.end();
				return false;
			}
			user = credentials[0];
			// when the header is: "user:"
			pass = credentials.length > 1 ? credentials[1] : null;
			// check if user is valid, by checking user credential with
			// claimService supported credentials
			if (user != null && user.equals(prop.getProperty("user.name")) && pass != null
					&& pass.equals(prop.getProperty("user.password"))) {
				return true;
			}
		} catch (Exception e) {
			log.error("Exception while checking user authentication");
		}
		response.setStatusCode(CLIENT_AUTH_FAILED).end();
		return false;
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
	}
}