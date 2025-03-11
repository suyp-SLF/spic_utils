package kd.cus.webservice.action;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * 接收信息（当前废弃）1
 * @author suyp
 *
 */
public interface WebserviceInterface {
	
	String receive(
			String arg0, 
			String arg1,
			String arg2);
}
