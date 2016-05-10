package com.shareart.service.dao;

import static com.shareart.service.response.UserResponseHandler.AUTH_FAILED;
import static com.shareart.service.response.UserResponseHandler.DUPLICATE_USER_ID;
import static com.shareart.service.response.UserResponseHandler.SUCCESS;
import static com.shareart.service.response.UserResponseHandler.USER_BLOCKED;
import static com.shareart.service.response.UserResponseHandler.USER_DELETED;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareart.service.domain.Address;
import com.shareart.service.domain.User;
import com.shareart.service.response.UserResponse;
import com.shareart.service.response.UserResponseHandler;
import com.shareart.service.util.EmailHelper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;

public class UserDAO extends ServiceDAO {
	private String QUERY_CREATE_USER = "INSERT INTO user (USER_ID, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, PHONE, DOB, ADDRESS_OID, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, '1')";
	private String QUERY_UPDATE_USER = "UPDATE user set USER_ID=?, PASSWORD=?, FIRST_NAME=?, LAST_NAME=?, EMAIL=?, PHONE=?, DOB=? WHERE USER_OID=?";
	private String QUERY_CREATE_CHILD = "INSERT INTO user (USER_ID, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, PHONE, DOB, PARENT_OID, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, '1')";
	private String QUERY_CREATE_ADDRESS = "INSERT INTO address (LINE1, LINE2, CITY, COUNTRY, STATE, ZIP_CODE, REF) VALUES(?,?,?,?,?,?,?)";
	private String QUERY_UPDATE_ADDRESS = "UPDATE  address set LINE1=?, LINE2=?, CITY=?, COUNTRY=?, STATE=?, ZIP_CODE=? WHERE ADDRESS_OID=?";
	private String QUERY_GET_USER_ID = "SELECT user_oid FROM user WHERE user_id = ? ";
	private String QUERY_DUPLICATE_USER_ID = "SELECT user_oid FROM user WHERE user_id = ? and USER_OID <> ?";
	private String QUERY_GET_ADDRESS_OID = "SELECT address_oid FROM address WHERE ref = ? ";
	
	private String QUERY_GET_USER = "SELECT * FROM user LEFT JOIN address ON user.address_oid = address.address_oid WHERE user_id = ? AND password = ? ";
	
	private String QUERY_ACTIVATE_USER = "UPDATE user SET ACTIVE= 2 WHERE USER_OID= ? and ACTIVE = 1 ";
	private String QUERY_DEACTIVATE_USER = "UPDATE user SET ACTIVE= 3 WHERE USER_OID= ? ";
	private String QUERY_DELETE_USER = "UPDATE user SET ACTIVE= 4 WHERE USER_OID= ?";
	
	
	
	Gson gson = new GsonBuilder().setDateFormat("dd/MMM/yyyy HH:mm:ss zzz").create();
	
	public void addUser(User user, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			JsonArray addrData = new JsonArray();
			long addRef = System.currentTimeMillis();
			
			Address addr = user.getAddress();
			if(addr.getLine1() != null){
				addrData.add(addr.getLine1());
			} else {
				addrData.add("");
			}
			
			if(addr.getLine2() != null){
				addrData.add(addr.getLine2());
			} else {
				addrData.add("");
			}
			
			if(addr.getCity() != null){
				addrData.add(addr.getCity());
			} else {
				addrData.add("");
			}
			
			if(addr.getCountry() != null){
				addrData.add(addr.getCountry());
			} else {
				addrData.add("");
			}
			
			if(addr.getState() != null){
				addrData.add(addr.getState());
			} else {
				addrData.add("");
			}
			
			if(addr.getZipCode() != null){
				addrData.add(addr.getZipCode());
			} else {
				addrData.add("");
			}
			
			addrData.add(addRef);

			JsonArray userData = new JsonArray();
			userData.add(user.getUserId()).add(user.getPassword());
			
			if(user.getFirstName() != null){
				userData.add(user.getFirstName());
			} else {
				userData.add("");
			}
			
			if(user.getLastName() != null){
				userData.add(user.getLastName());
			} else {
				userData.add("");
			}
			
			userData.add(user.getEmail());
			
			if(user.getPhone() != null){
				userData.add(user.getPhone());
			} else {
				userData.add("");
			}
			
			if(user.getDob() != null){
				userData.add(user.getDob());
			} else {
				userData.add("");
			}
			
			
			// check for duplicate user_ID 
			connection.queryWithParams(QUERY_GET_USER_ID, new JsonArray().add(user.getUserId()), validateUser -> {
				if(validateUser.succeeded()){
					if(validateUser.result().getRows().size()>0){
						userRespHandler.process(null, DUPLICATE_USER_ID, "User ID "+user.getUserId()+" already exist");
						// and close the connection
						this.closeConnection(connection);
					} else {
						connection.setAutoCommit(false, closeCnnRs ->{
							if(closeCnnRs.succeeded()){
								// Insert into address table
								connection.updateWithParams(QUERY_CREATE_ADDRESS, addrData, insertAddr -> {
									if (insertAddr.succeeded()) {
										// get adress_oid
										connection.queryWithParams(QUERY_GET_ADDRESS_OID, new JsonArray().add(addRef), rsAddr -> {
											if(rsAddr.succeeded()){
												int addressOid = rsAddr.result().getRows().get(0).getInteger("address_oid");
												System.out.println("address_oid :: " + addressOid);
												userData.add(addressOid);
												// insert into user table
												connection.updateWithParams(QUERY_CREATE_USER, userData, rs -> {
													if (rs.succeeded()) {
														// get user_oid for newly added user
														connection.queryWithParams(QUERY_GET_USER_ID, new JsonArray().add(user.getUserId()), rsUser -> {
															boolean sendActivateionEmail = false;
															if(rsUser.succeeded()){
																int user_oid = rsUser.result().getRows().get(0).getInteger("user_oid");
																System.out.println("newly created user_oid :: "+user_oid);
																/*if(user.getChildrens()!=null){
																	JsonArray childData = new JsonArray();
																	for (Object object : userData) {
																		
																	}
																}*/
																UserResponse userResponse = new UserResponse();
																//userResponse.setUserId(user.getUserId());
																userResponse.setUserOid(rsUser.result().getRows().get(0).getInteger("user_oid"));
																user.setUserOid(userResponse.getUserOid());
																userResponse.setAddressOid(addressOid);
																userRespHandler.process(userResponse, SUCCESS, "User created successfully");
																sendActivateionEmail = true;
																// commit 
																connection.commit(null);
																// and close the connection
																this.closeConnection(connection);
															}else{
																userRespHandler.process(null, APP_ERROR, rsUser.cause().getMessage());
																// and close the connection
																this.closeConnection(connection);
															}															
															if(sendActivateionEmail){
																EmailHelper.sendRegristrationMail(user.getEmail(), userRespHandler.getBaseUrl()+"user/activate/"+user.getUserOid());
															}
														});
													} else {
														userRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
														// and close the connection
														this.closeConnection(connection);
													}
													
												});
											} else {
												userRespHandler.process(null, APP_ERROR, rsAddr.cause().getMessage());
												// and close the connection
												this.closeConnection(connection);
											}
										});
									} else {
										userRespHandler.process(null, APP_ERROR, insertAddr.cause().getMessage());
										// and close the connection
										this.closeConnection(connection);
									}
								});
							} else {
								userRespHandler.process(null, APP_ERROR, closeCnnRs.cause().getMessage());
								// and close the connection
								this.closeConnection(connection);
							}
							
						});
					}
				}else{
					userRespHandler.process(null, APP_ERROR, validateUser.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void updateUser(User user, int userOid, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			JsonArray addrData = new JsonArray();
			Address addr = user.getAddress();
			
			if(addr.getLine1() != null){
				addrData.add(addr.getLine1());
			} else {
				addrData.add("");
			}
			
			if(addr.getLine2() != null){
				addrData.add(addr.getLine2());
			} else {
				addrData.add("");
			}
			
			if(addr.getCity() != null){
				addrData.add(addr.getCity());
			} else {
				addrData.add("");
			}
			
			if(addr.getCountry() != null){
				addrData.add(addr.getCountry());
			} else {
				addrData.add("");
			}
			
			if(addr.getState() != null){
				addrData.add(addr.getState());
			} else {
				addrData.add("");
			}
			
			if(addr.getZipCode() != null){
				addrData.add(addr.getZipCode());
			} else {
				addrData.add("");
			}

			JsonArray userData = new JsonArray();
			userData.add(user.getUserId()).add(user.getPassword());
			
			if(user.getFirstName() != null){
				userData.add(user.getFirstName());
			} else {
				userData.add("");
			}
			
			if(user.getLastName() != null){
				userData.add(user.getLastName());
			} else {
				userData.add("");
			}
			
			userData.add(user.getEmail());
			
			if(user.getPhone() != null){
				userData.add(user.getPhone());
			} else {
				userData.add("");
			}
			
			if(user.getDob() != null){
				userData.add(user.getDob());
			} else {
				userData.add("");
			}
			
			
			// check for duplicate user_ID 
			connection.queryWithParams(QUERY_DUPLICATE_USER_ID, new JsonArray().add(user.getUserId()).add(userOid), validateUser -> {
				if(validateUser.succeeded()){
					if(validateUser.result().getRows().size()>0){
						userRespHandler.process(null, DUPLICATE_USER_ID, "User ID "+user.getUserId()+" already exist");
						// and close the connection
						this.closeConnection(connection);
					} else {
						connection.setAutoCommit(false, closeCnnRs ->{
							if(closeCnnRs.succeeded()){
								// UPDATE address table
								connection.updateWithParams(QUERY_UPDATE_ADDRESS, addrData.add(user.getAddress().getAddressOid()), updateAddr -> {
									if (updateAddr.succeeded()) {
										// UPDATE user table
										connection.updateWithParams(QUERY_UPDATE_USER, userData.add(userOid), rs -> {
											if (rs.succeeded()) {
												UserResponse userResponse = new UserResponse();
												//userResponse.setUserId(user.getUserId());
												userResponse.setUserOid(user.getUserOid());
												userRespHandler.process(userResponse, SUCCESS, "User Updated successfully");
												// commit 
												connection.commit(null);
												// and close the connection
												this.closeConnection(connection);
											} else {
												userRespHandler.process(null, APP_ERROR, rs.cause().getMessage());
												// and close the connection
												this.closeConnection(connection);
											}
											
										});									
									} else {
										userRespHandler.process(null, APP_ERROR, updateAddr.cause().getMessage());
										// and close the connection
										this.closeConnection(connection);
									}
								});
							} else {
								userRespHandler.process(null, APP_ERROR, closeCnnRs.cause().getMessage());
								// and close the connection
								this.closeConnection(connection);
							}							
						});
					}
				}else{
					userRespHandler.process(null, APP_ERROR, validateUser.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void addChild(User child, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			SQLConnection connection = conn.result();
			JsonArray childData = new JsonArray();
			
			childData.add(child.getUserId()).add(child.getPassword());
			
			if(child.getFirstName() != null){
				childData.add(child.getFirstName());
			}
			if(child.getLastName() != null){
				childData.add(child.getLastName());
			}
			childData.add(child.getEmail());
			if(child.getPhone() != null){
				childData.add(child.getPhone());
			}
			if(child.getDob() != null){
				childData.add(child.getDob());
			}
			childData.add(child.getParentOid());
			
			connection.queryWithParams(QUERY_GET_USER_ID, new JsonArray().add(child.getUserId()), validateUser -> {
				if(validateUser.succeeded()){
					if(validateUser.result().getRows().size()>0){
						userRespHandler.process(null, DUPLICATE_USER_ID, "User ID "+child.getUserId()+" already exist");
						// and close the connection
						this.closeConnection(connection);
					}else {
						connection.setAutoCommit(false, closeCnnRs ->{
							if(closeCnnRs.succeeded()){
								// Insert into address table
								connection.updateWithParams(QUERY_CREATE_CHILD, childData, insertChild -> {
									if (insertChild.succeeded()) {
										// get user_oid for newly added child
										connection.queryWithParams(QUERY_GET_USER_ID, new JsonArray().add(child.getUserId()), rsUser -> {
											boolean sendActivateionEmail = false;
											if(rsUser.succeeded()){
												int user_oid = rsUser.result().getRows().get(0).getInteger("user_oid");
												System.out.println("newly created child with user_oid :: "+user_oid);
												UserResponse userResponse = new UserResponse();
												//userResponse.setUserId(user.getUserId());
												userResponse.setUserOid(rsUser.result().getRows().get(0).getInteger("user_oid"));
												child.setUserOid(userResponse.getUserOid());
												userRespHandler.process(userResponse, SUCCESS, "child created successfully");
												// commit 
												connection.commit(null);
												// and close the connection
												this.closeConnection(connection);
												sendActivateionEmail = true;
											}else{
												userRespHandler.process(null, APP_ERROR, rsUser.cause().getMessage());
												// and close the connection
												this.closeConnection(connection);
											}											
											if(sendActivateionEmail){
												EmailHelper.sendRegristrationMail(child.getEmail(), userRespHandler.getBaseUrl()+"user/activate/"+child.getUserOid());
											}
										});
									} else {
										userRespHandler.process(null, APP_ERROR, insertChild.cause().getMessage());
										// and close the connection
										this.closeConnection(connection);
									}
								});
							}else {
								userRespHandler.process(null, APP_ERROR, closeCnnRs.cause().getMessage());
								// and close the connection
								this.closeConnection(connection);
							}	
						});	
					}
				} else {
					userRespHandler.process(null, APP_ERROR, validateUser.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void login(String userId, String password, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, "error to establish dabase connection");
				return;
			}
			
			SQLConnection connection = conn.result();

			// get user information based on userid/password
			connection.queryWithParams(QUERY_GET_USER, new JsonArray().add(userId).add(password), userRs -> {
				if (userRs.succeeded()) {
					UserResponse userResponse = new UserResponse();
					try {
						if(userRs.result().getRows().size()==0){
							userRespHandler.process(userResponse, AUTH_FAILED, "user not authorized");
						}else{
							User user = null;
							try {
								System.out.println(userRs.result().getRows());
								user = this.getUser(userRs.result().getRows().get(0));
								System.out.println("User_oid ::"+user.getUserOid());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if(user.getActive()==1){
								userRespHandler.process(userResponse,AUTH_FAILED , "user found but not active yet");
							} else if(user.getActive()==3){
								userRespHandler.process(userResponse,USER_BLOCKED , "user is blocked");
							}  else if(user.getActive()==4){
								userRespHandler.process(userResponse,USER_DELETED , " user delted");
							}else{
								userResponse.setUser(user);
								userRespHandler.process(userResponse,SUCCESS , "valid user");
							}		
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					// and close the connection
					this.closeConnection(connection);
				} else {
					userRespHandler.process(null, APP_ERROR, userRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
			
		});
	}
	
	public void acitvateUser(int userOid, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.activationError();
				return;
			}
			SQLConnection connection = conn.result();

			connection.updateWithParams(QUERY_ACTIVATE_USER, new JsonArray().add(userOid), updateRs -> {
				if (updateRs.succeeded()) {
					if(updateRs.result().getUpdated()==0){
						userRespHandler.activationNotNeeded();
					} else{
						userRespHandler.activationSuccess();
					}					
					// and close the connection
					this.closeConnection(connection);
				} else {
					userRespHandler.activationError();
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void deacitvateUser(int userOid, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			UserResponse userResponse = new UserResponse();
			userResponse.setUserOid(userOid);
			connection.updateWithParams(QUERY_DEACTIVATE_USER, new JsonArray().add(userOid), updateRs -> {
				if (updateRs.succeeded()) {
					userRespHandler.process(userResponse,SUCCESS , "Deactivated User");
					// and close the connection
					this.closeConnection(connection);
				} else {
					userRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	public void deleteUser(int userOid, UserResponseHandler userRespHandler) {
		getJdbcClient().getConnection(conn -> {
			if (conn.failed()) {
				userRespHandler.process(null, APP_ERROR, conn.cause().getMessage());
				return;
			}
			SQLConnection connection = conn.result();
			UserResponse userResponse = new UserResponse();
			userResponse.setUserOid(userOid);
			connection.updateWithParams(QUERY_DELETE_USER, new JsonArray().add(userOid), updateRs -> {
				if (updateRs.succeeded()) {
					userRespHandler.process(userResponse,SUCCESS , "user mark for deletion");
					// and close the connection
					this.closeConnection(connection);
				} else {
					userRespHandler.process(null, APP_ERROR, updateRs.cause().getMessage());
					// and close the connection
					this.closeConnection(connection);
				}
			});
		});
	}
	
	private User getUser(JsonObject jsonObject){
		User user = new User();
		Address addr = new Address();
		user.setUserOid(jsonObject.getInteger("USER_OID"));
		user.setUserId(jsonObject.getString("USER_ID"));
		//user.setActivationDate(jsonObject.getString("ACTIVATION_DATE"));
		user.setActive(jsonObject.getInteger("ACTIVE"));
		user.setDob(jsonObject.getString("DOB"));
		user.setEmail(jsonObject.getString("EMAIL"));
		user.setFirstName(jsonObject.getString("FIRST_NAME"));
		user.setLastName(jsonObject.getString("LAST_NAME"));
		user.setParentOid(jsonObject.getInteger("PARENT_OID"));
		user.setPhone(jsonObject.getString("PHONE"));
		
		addr.setAddressOid(jsonObject.getInteger("ADDRESS_OID"));
		addr.setCity(jsonObject.getString("CITY"));
		addr.setCountry(jsonObject.getString("COUNTRY"));
		addr.setLine1(jsonObject.getString("LINE1"));
		addr.setLine2(jsonObject.getString("LINE2"));
		addr.setState(jsonObject.getString("STATE"));
		addr.setZipCode(jsonObject.getString("ZIP_CODE"));
		
		user.setAddress(addr);
		
		return user;
	}
}