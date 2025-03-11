package kd.cus.conmWebservice.action;

import kd.cus.conmWebservice.register.Purcontract;

/**
 * 工厂接口类
 * @author suyp
 *
 */
public class WebserviceFactory  {
	
	private final static String PURCONTRACT = "purcontract";//资产类别

	//对应注册类
	public WebserviceMainTemplate getWebserviceMainTemplate(String registerType) {
		switch (registerType){
			case PURCONTRACT:
				return new Purcontract();
		}
		return null;
	}
}
