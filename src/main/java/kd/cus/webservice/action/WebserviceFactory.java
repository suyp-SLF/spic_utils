package kd.cus.webservice.action;

import kd.cus.webservice.register.AssetCategory;
import kd.cus.webservice.register.Customer;
import kd.cus.webservice.register.Department;
import kd.cus.webservice.register.Org;
import kd.cus.webservice.register.Person;
import kd.cus.webservice.register.Supplier;

/**
 * 工厂接口类
 * @author suyp
 *
 */
public class WebserviceFactory  {
	
	private final static String ASSETCATEGORY_CODE = "JTCWGXPTGDZCLB";//资产类别
	private final static String SUPPLIER_CODE = "JTCWGXPTGYS";//供应商
	private final static String PERSON_CODE = "JTCWGXPTYG";//人员
	private final static String ORG_CODE = "JTCWGXPTZZ";//组织
	private final static String DEPARTMENT_CODE = "JTCWGXPTBM";//部门
	private final static String CUSTOMER_CODE = "JTCWGXPTTEST"; //客户

	
	
	
	//对应注册类
	public WebserviceMainTemplate getWebserviceMainTemplate(String registerType) {
		
//		if (ASSETCATEGORY_CODE.equalsIgnoreCase(registerType)) {
//			return new AssetCategory();
//		} else if (SUPPLIER_CODE.equalsIgnoreCase(registerType)) {
//			return new Supplier();
//		} else if (PERSON_CODE.equalsIgnoreCase(registerType)) {
//			return new Person();
//		} else if (ORG_CODE.equalsIgnoreCase(registerType)) {
//			return new Org();
//		} else if (DEPARTMENT_CODE.equals(registerType)) {
//			return new Department();
//		} else if (CUSTOMER_CODE.equals(registerType)) {
//			return new Customer();
//		} else {
//			return null;
//		}
			return null;
	}
}
