package com.shareart.service;

/**
 * @author Buddhadev <code>ClaimServiceConstants</code> class to refer all
 *         constant fields.
 */
public class ServiceConstants {
	public static final String VERSION = "ver";
	public static final String TENANT = "tenant";
	public static final String CLAIM_ID = "claimId";
	public static final String WEB_ORDER_NO = "weborder";
	public static final String HP_ORDER_NO = "hporder";
	public static final String FROM_DATE = "fromDate";
	public static final String TO_DATE = "toDate";
	public static final String OFFSET = "offset";
	public static final String LIMIT = "limit";
	public static final String HEADER_DATA = "header";
	public static final String CLAIM_ITEM_DATA = "item";
	public static final String ACTIVITY_TRACE = "trace";
	public static final String COMMUNICATION_HISTORY = "commhistory";
	public static final String RETURN_ORDER = "returnorder";
	public static final String USER = "user";
	public static final String YES = "y";
	public static final String CURRENT_VERSION = "1";
	
	 public static final String ERR_PRIMARY_KEY = "Too many Primary Keys Present";
	 public static final String ERR_PRIMARY_KEY_CLAIMS = "Atleast fromDate and toDate are required";
	 public static final String ERR_NO_PRIMARY  = "No Primary Key Specified";
	 public static final String ERR_EMPTY_PARAMETER  = "Empty Parameter";
	 public static final String ERR_VERSION ="Invalid Version Specified"; 
	 public static final int CLIENT_ERROR_CONFLICT = 409; //Conflict
	 public static final int CLIENT_AUTH_FAILED = 403; //authentication failed
	 public static final int CLIENT_AUTH_MISSING = 401; //authentication failed
	 public static final int APP_ERROR = 500; // Internal Server Error

	public static final String QUERY_CLAIM_BY_CLAIMID = "SELECT * FROM CLAIM WHERE CLAIM_OID = ? and TENANT = ? ";

	public static final String QUERY_CLAIM_ITEM_BY_CLAIMID = "SELECT * FROM CLAIM_ITEM WHERE CLAIM_OID = ? ";

	public static final String QUERY_ACTIVITY_TRACE_BY_CLAIMID = "SELECT * FROM ACTIVITY_TRACE WHERE CLAIM_OID = ? ";

	public static final String QUERY_EMAIL_QUEUE_BY_CLAIMID = "SELECT * FROM EMAIL_QUEUE WHERE CLAIM_OID = ? ";

	public static final String QUERY_RETURN_ORDER_BY_CLAIMID = "SELECT * FROM CREDIT_HEADER ch, CREDIT_ITEM_DETAIL cid where ch.CRH_OID = cid.CRH_OID and CLAIM_OID = ? and ROWNUM = 1 ";

	public static final String QUERY_USER_BY_CLAIMID = "SELECT usr.*, c.CLAIM_OID from ECLAIMS_USER usr, CLAIM c WHERE c.SUBMITTER_USER_OID = usr.USER_OID and c.CLAIM_OID = ? ";

	public static final String QUERY_CLAIMS = "SELECT unique at.CLAIM_OID,c.CLAIM_ID from ACTIVITY_TRACE at, CLAIM c where  at.CLAIM_OID = c.CLAIM_OID and at.TIME_STAMP >=TO_TIMESTAMP(?,'yyyy-mm-ddhh:mi:ss') and at.TIME_STAMP<= TO_TIMESTAMP(?,'yyyy-mm-ddhh:mi:ss') and c.TENANT = ? ";
	public static final String PART_QUERY_CLAIMS_HP_ORDERNO = " AND c.HP_ORDER_NO_FOR_CUSTOMER = ? ";
	public static final String PART_QUERY_CLAIMS_WEB_ORDERNO = " AND c.WEB_ORDER_NUMBER = ? ";
}
