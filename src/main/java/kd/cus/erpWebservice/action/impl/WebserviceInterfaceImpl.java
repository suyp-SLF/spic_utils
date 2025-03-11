
package kd.cus.erpWebservice.action.impl;

import kd.bos.context.RequestContextCreator;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.cus.api.*;
import kd.cus.erpWebservice.action.WebserviceFactory;
import kd.cus.erpWebservice.action.WebserviceInterface;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.postgresql.util.MD5Digest;
import org.xml.sax.SAXException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author suyp
 */
@WebService
public class WebserviceInterfaceImpl implements WebserviceInterface {

    private final static String WS_REGISTERED_BILLCODE = "spic_maindata_registered";// 注册接口的苍穹基础资料编码
    private final static String TARGETNAMESPACE_PARAMS = "http://impl.action.erpWebservice.cus.kd/";
    private final static String START_STR = "开始传输";
    private final static String FAILURE_STR = "传输失败";
    private final static String SUCCESS_STR = "传输成功";
    private final static String ERROR_STR = "解析失败";

    private final static String SUCCESS_CODE = "1";
    private final static String FAILURE_CODE = "2";

    /**
     * p_data_id 唯一id p_interface_type 接口编码 p_content 内容数据（xml）
     *
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */

    @WebResult(name = "X_RETURN_MSG", targetNamespace = TARGETNAMESPACE_PARAMS)
    public String receive(@WebParam(name = "p_interface_type", targetNamespace = TARGETNAMESPACE_PARAMS) String p_interface_type,
                          @WebParam(name = "p_source", targetNamespace = TARGETNAMESPACE_PARAMS) String p_source,
                          @WebParam(name = "p_user_name", targetNamespace = TARGETNAMESPACE_PARAMS) String p_user_name,
                          @WebParam(name = "p_password", targetNamespace = TARGETNAMESPACE_PARAMS) String p_password,
                          @WebParam(name = "p_data_id", targetNamespace = TARGETNAMESPACE_PARAMS) String p_data_id,
                          @WebParam(name = "p_content", targetNamespace = TARGETNAMESPACE_PARAMS) String p_content) {
        Map<String,String> key = new HashMap<String,String>(){{


        }};


        List<ResultEntity> results = new ArrayList<>();
//        if (StringUtils.isBlank(p_content)) {
//            return "未找到p_content";
//        }
//        if (StringUtils.isBlank(p_password)) {
//            return "未找到p_password";
//        }
//        if (!p_password.equalsIgnoreCase(encrypt(key.get(p_source),p_content))){
//            return "密码错误";
//        }

    	String configs = System.getProperty("WS_CONFIG");
        if (StringUtils.isNotBlank(configs)){
            RequestContextCreator.createForTripSI(configs.split(",")[0],configs.split(",")[1],configs.split(",")[2]) ;
        }
//        Log logger = LogFactory.getLog(WebserviceInterfaceImpl.class);
        LogEntity logResult = LogBillUtils.createLog(p_content, "", p_source, p_data_id, p_interface_type);
        Date startDate = LogUtils.log(null, p_interface_type, START_STR, p_content, "", null, null);
        String syscodesyncode, syscode, uniqueid;
        List<Element> children;
        // 初步解析xml文件获得SYSCODESYNCODE与SYSCODE
        try {
            Document doc = XmlUtils.getDocument(p_content);
            Element rootElt = doc.getRootElement();
            children = rootElt.elements("DATAINFO");

//            syscodesyncode = rootElt.attribute("SYSCODESYNCODE").getText();
//            syscode = rootElt.attribute("SYSCODE").getText();
//            uniqueid = rootElt.attribute("UNIQUEID").getText();

        } catch (ParserConfigurationException | SAXException | IOException | DocumentException e) {
            // 解析xml文件出现问题
            Log(logResult, FAILURE_CODE,
                    ResultEntity.XML_INITERROR().resultXML(results), "", p_interface_type, p_content,
                    ResultEntity.XML_INITERROR().getName(), startDate, e);
            return ResultEntity.REGISTEREDCODE_DISABLE().resultXML(results);
        }
        // 获得对应的子类
        try {
            WebserviceFactory webserviceFactory = new WebserviceFactory();
            WebserviceMainTemplate webserviceMainTemplate = webserviceFactory
                    .getWebserviceMainTemplate(p_interface_type);
            results = webserviceMainTemplate.disposeDate(children,p_source);
            Boolean allSuccess = true;
            for (ResultEntity wsResult : results) {
                if (SUCCESS_CODE.equals(wsResult.getIsSuccess())) {
                    allSuccess = false;
                }
            }
            if (allSuccess) {
                Log(logResult, SUCCESS_CODE,
                        ResultEntity.SUCCESS().resultXML(results), "", p_interface_type, p_content,
                        ResultEntity.SUCCESS().getName(), startDate, null);
            } else {
                Log(logResult, FAILURE_CODE,
                        ResultEntity.PARTLY_SUCCESS().resultXML(results), "", p_interface_type,
                        p_content, ResultEntity.PARTLY_SUCCESS().getName(), startDate, null);
            }
            return ResultEntity.resultXML(results);
        } catch (Exception e) {
            Log(logResult, FAILURE_CODE, ResultEntity.resultXML(results), "", p_interface_type,
                    p_content, "其他错误", startDate, e);
            results.add(ResultEntity.PROCESS_ERROR(ThrowableUtils.getStackTrace(e)));
            return ResultEntity.resultXML(results);
        }
    }

    private void Log(LogEntity logResult, String successStr, String xmlStr, String source, String interfaceStr,
                     String requestData, String functionName, Date startDate, Throwable e) {
        LogBillUtils.modifyLog(logResult, successStr, xmlStr, source);
        LogUtils.log(SUCCESS_CODE.equals(successStr), interfaceStr,
                SUCCESS_CODE.equals(successStr) ? SUCCESS_STR : FAILURE_STR, requestData, functionName, startDate, e);
    }

    private static String encrypt(String dataStr, String key) {
        try {
            dataStr = key + dataStr;
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(dataStr.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
