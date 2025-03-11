package kd.cus.erpWebservice.action;

import kd.bos.context.RequestContextCreator;
import kd.cus.erpWebservice.register.Contract;
import kd.cus.erpWebservice.register.*;

/**
 * 工厂接口类
 * @author suyp
 *
 */
public class WebserviceFactory  {
	
	private final static String TEST_CODE = "TEST";//资产类别
	private final static String MATERIAL_CODE = "materials";//物料
	private final static String ASSET = "asset";//资产转固单
	private final static String BUSBILL = "busbill";//暂估应付单
	private final static String APFINAPBILL = "finapbill";//暂估应付单
	private final static String PAYBILL = "paybill";//付款单
	private final static String PURCONTRACT = "contract";
	private final static String TAXSYS = "TAXSYS";
	private final static String TAXVOUCHER = "fr_manualtallybi";
	//对应注册类
	public WebserviceMainTemplate getWebserviceMainTemplate(String registerType) {
	
		switch (registerType){
			case TEST_CODE:
				return new testMethod();
			case MATERIAL_CODE:
//				return new Material();
			case ASSET:
				return new AssetPurchaseBill();
			case BUSBILL:
				return new Busbill();
			case APFINAPBILL:
				return new ApFinApBill();
			case PAYBILL:
				return new PayBill();
			case PURCONTRACT:
				return new Contract();
			case TAXSYS:
				return new InvoicedInfoRec();
			case TAXVOUCHER:
//				return new Fr_ManualtallyBill();
		}
		return null;
	}
}
