package test.com.shareart.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareart.service.domain.Address;
import com.shareart.service.domain.Article;
import com.shareart.service.domain.Document;
import com.shareart.service.domain.User;

import io.vertx.core.json.Json;

public class TestDomain {
	public static void main(String[] args) {
		User user = new User();
		user.setUserId("bd");
		user.setFirstName("FN");
		user.setLastName("LN");
		user.setActivationDate(new Date());
		//user.setDob(new Date());
		user.setEmail("bd@in.com");
		Address addr = new Address();
		addr.setCity("city");
		addr.setLine1("line1");
		addr.setLine2("line2");
		addr.setState("state");
		addr.setZipCode("123444");
		user.setAddress(addr);
		user.setChildrens(new ArrayList<>());
		User chield1 = new User();
		chield1.setUserId("chiled1");
		chield1.setFirstName("chield1 FN");
		chield1.setLastName("chield1 LN");
		chield1.setActivationDate(new Date());
		//chield1.setDob((new Date());
		chield1.setEmail("bd_ch1@in.com");
		chield1.setParentOid(123);;
		
		user.getChildrens().add(chield1);
		
		User chield2 = new User();
		chield2.setUserId("chiled2");
		chield2.setFirstName("chield2 FN");
		chield2.setLastName("chield2 LN");
		chield2.setActivationDate(new Date());
		//chield2.setDob(new Date());
		chield2.setEmail("bd_ch2@in.com");
		
		user.getChildrens().add(chield2);
		Gson gson = new GsonBuilder().setDateFormat("dd/MMM/yyyy HH:mm:ss zzz").create();//new Gson();
		//gson.toJson(user)
		System.out.println(Json.encode(user));
		System.out.println(gson.toJson(user));
		System.out.println(gson.toJson(chield1));
		
		/*Response resp = new Response();
		ChildResponse cResp = new ChildResponse();
		cResp.setChieldOid(124);
		cResp.setUserOid(123);
		cResp.setUserId("bd");
		
		resp.setOperationStatus(1);
		resp.setMessage("child added successfully");
		resp.setBody(cResp);*/
		
		//System.out.println(gson.toJson(resp));
		
		Article article = new Article();
		article.setArticleName("article1");
		article.setCategoryOid(1);
		article.setDescription("article desc");
		article.setUserOid("123");
		
		Document doc = new Document();
		doc.setDocName("image1");
		doc.setDefaultFlag(1);
		
		Document doc1 = new Document();
		doc1.setDocName("image2");
		
		List<Document> docs = new ArrayList<>();
		docs.add(doc);
		docs.add(doc1);
		article.setDocuments(docs);
		
		System.out.println(gson.toJson(article));
	}
}
