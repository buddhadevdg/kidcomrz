package test.com.shareart.service;

public class Test {
	public static void main(String[] args) {
		String st = "http://52.11.102.161:8092/service/img/adasdas.jpg";
		System.out.println(st.substring(st.lastIndexOf("img/")));
		String yourstring = "asd'sds";
		String cellVal = "2016-01:21 00:00:00";
		System.out.println(yourstring.replace("'", "''"));
		String allwdRegex;
		String allowedsplChars = "@\'";
		allwdRegex = "[a-zA-Z0-9 \\-\\:]*";
		System.out.println(cellVal.matches(allwdRegex));
		//yourstring = yourstring.replace(/'/g, "''")
	}
}
