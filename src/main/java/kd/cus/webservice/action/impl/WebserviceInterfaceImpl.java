package kd.cus.webservice.action.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;

import kd.bos.context.RequestContextCreator;
import kd.bos.dataentity.utils.StringUtils;
import kd.cus.api.LogEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cus.webservice.action.WebserviceFactory;
import kd.cus.webservice.action.WebserviceInterface;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;
import kd.cus.api.LogBillUtils;
import kd.cus.api.LogUtils;
import kd.cus.api.XmlUtils;

/**
 * 
 * @author suyp
 *
 */
@WebService
public class WebserviceInterfaceImpl implements WebserviceInterface {

	private final static String WS_REGISTERED_BILLCODE = "spic_maindata_registered";// 注册接口的苍穹基础资料编码
	private final static String TARGETNAMESPACE_PARAMS = "http://impl.action.webservice.cus.kd/";
	private final static String START_STR = "开始传输";
	private final static String FAILURE_STR = "传输失败";
	private final static String SUCCESS_STR = "传输成功";
	private final static String ERROR_STR = "解析失败";

	private final static String SUCCESS_CODE = "1";
	private final static String FAILURE_CODE = "2";

	/**
	 * arg0 唯一id arg1 接口编码 arg2 内容数据（xml）
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */

	@WebResult(name = "X_RETURN_MSG", targetNamespace = TARGETNAMESPACE_PARAMS)
	public String receive(@WebParam(name = "arg0", targetNamespace = TARGETNAMESPACE_PARAMS) String arg0,
			@WebParam(name = "arg1", targetNamespace = TARGETNAMESPACE_PARAMS) String arg1,
			@WebParam(name = "arg2", targetNamespace = TARGETNAMESPACE_PARAMS) String arg2) {
		String configs = System.getProperty("WS_CONFIG");
		if (StringUtils.isNotBlank(configs)){
			RequestContextCreator.createForTripSI(configs.split(",")[0],configs.split(",")[1],configs.split(",")[2]) ;
		}
//		Log logger = LogFactory.getLog(WebserviceInterfaceImpl.class);
		LogEntity logResult = LogBillUtils.createLog(arg2, "", "", arg0, arg1);
		Date startDate = LogUtils.log(null, arg1, START_STR, arg2, "", null, null);
		List<ResultEntity> results = new ArrayList<>();
		String syscodesyncode, syscode, uniqueid;
		List<Element> children;
		// 初步解析xml文件获得SYSCODESYNCODE与SYSCODE
		try {
			Document doc = XmlUtils.getDocument(arg2);
			Element rootElt = doc.getRootElement();
			children = rootElt.elements("DATAINFO");

			syscodesyncode = rootElt.attribute("SYSCODESYNCODE").getText();
			syscode = rootElt.attribute("SYSCODE").getText();
			uniqueid = rootElt.attribute("UNIQUEID").getText();

		} catch (ParserConfigurationException | SAXException | IOException | DocumentException e) {
			// 解析xml文件出现问题
			Log(logResult, FAILURE_CODE,
					ResultEntity.XML_INITERROR().resultXML(ERROR_STR, ERROR_STR, ERROR_STR, results), "", arg1, arg2,
					ResultEntity.XML_INITERROR().getName(), startDate, e);
			return ResultEntity.REGISTEREDCODE_DISABLE().resultXML(ERROR_STR, ERROR_STR, ERROR_STR, results);
		}
		// 获得对应的子类
		try {
			WebserviceFactory webserviceFactory = new WebserviceFactory();
			WebserviceMainTemplate webserviceMainTemplate = webserviceFactory
					.getWebserviceMainTemplate(arg1);
			results = webserviceMainTemplate.disposeDate(children);
			Boolean allSuccess = true;
			for (ResultEntity wsResult : results) {
				if (SUCCESS_CODE.equals(wsResult.getIsSuccess())) {
					allSuccess = false;
				}
			}
			if (allSuccess) {
				Log(logResult, SUCCESS_CODE,
						ResultEntity.SUCCESS().resultXML(syscodesyncode, syscode, uniqueid, results), "", arg1, arg2,
						ResultEntity.SUCCESS().getName(), startDate, null);
			} else {
				Log(logResult, FAILURE_CODE,
						ResultEntity.PARTLY_SUCCESS().resultXML(syscodesyncode, syscode, uniqueid, results), "", arg1,
						arg2, ResultEntity.PARTLY_SUCCESS().getName(), startDate, null);
			}
			return ResultEntity.resultXML(syscodesyncode, syscode, uniqueid, results);
		} catch (Exception e) {
			e.printStackTrace();
			Log(logResult, FAILURE_CODE, ResultEntity.resultXML(syscodesyncode, syscode, uniqueid, results), "", arg1,
					arg2, "其他错误", startDate, e);
			return ResultEntity.resultXML(syscodesyncode, syscode, uniqueid, results);
		}
	}

	private void Log(LogEntity logResult, String successStr, String xmlStr, String source, String interfaceStr,
					 String requestData, String functionName, Date startDate, Throwable e) {
		LogBillUtils.modifyLog(logResult, successStr, xmlStr, source);
		LogUtils.log(SUCCESS_CODE.equals(successStr), interfaceStr,
				SUCCESS_CODE.equals(successStr) ? SUCCESS_STR : FAILURE_STR, requestData, functionName, startDate, e);
	}
}
