package kd.cus.conmWebservice.action.impl;

import kd.bos.context.RequestContextCreator;
import kd.bos.dataentity.utils.StringUtils;
import kd.cus.api.LogBillUtils;
import kd.cus.api.LogEntity;
import kd.cus.api.LogUtils;
import kd.cus.api.XmlUtils;
import kd.cus.conmWebservice.action.WebserviceFactory;
import kd.cus.conmWebservice.action.WebserviceInterface;
import kd.cus.conmWebservice.action.WebserviceMainTemplate;
import kd.cus.conmWebservice.action.entity.ResultEntity;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.xml.sax.SAXException;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author suyp
 */
@WebService
public class WebserviceInterfaceImpl implements WebserviceInterface {

    private final static String WS_REGISTERED_BILLCODE = "spic_maindata_registered";// 注册接口的苍穹基础资料编码
    private final static String TARGETNAMESPACE_PARAMS = "http://impl.action.conmWebservice.cus.kd/";
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
        String configs = System.getProperty("WS_CONFIG");
        if (StringUtils.isNotBlank(configs)){
            RequestContextCreator.createForTripSI(configs.split(",")[0],configs.split(",")[1],configs.split(",")[2]) ;
        }
//        Log logger = LogFactory.getLog(WebserviceInterfaceImpl.class);
        LogEntity logResult = LogBillUtils.createLog(p_content, "", p_source, p_data_id, p_interface_type);
        Date startDate = LogUtils.log(null, p_interface_type, START_STR, p_content, "", null, null);
        List<ResultEntity> results = new ArrayList<>();
        String syscodesyncode, syscode, uniqueid;
        List<Element> children;
        Element rootElt;
        // 初步解析xml文件获得SYSCODESYNCODE与SYSCODE
        try {
            Document doc = XmlUtils.getDocument(p_content);
            rootElt = doc.getRootElement();
//            children = rootElt.elements("DATAINFO");

            syscodesyncode = rootElt.attribute("SYSCODESYNCODE").getText();
            syscode = rootElt.attribute("SYSCODE").getText();
            uniqueid = rootElt.attribute("UNIQUEID").getText();

        } catch (ParserConfigurationException | SAXException | IOException | DocumentException e) {
            // 解析xml文件出现问题
            Log(logResult, FAILURE_CODE,
                    ResultEntity.XML_INITERROR().resultXML(ERROR_STR, ERROR_STR, ERROR_STR, results), "", p_interface_type, p_content,
                    ResultEntity.XML_INITERROR().getName(), startDate, e);
            return ResultEntity.REGISTEREDCODE_DISABLE().resultXML(ERROR_STR, ERROR_STR, ERROR_STR, results);
        }
        // 获得对应的子类
        try {
            WebserviceFactory webserviceFactory = new WebserviceFactory();
            WebserviceMainTemplate webserviceMainTemplate = webserviceFactory
                    .getWebserviceMainTemplate(p_interface_type);
            results = webserviceMainTemplate.disposeDate(rootElt, p_source);
            Boolean allSuccess = true;
            for (ResultEntity wsResult : results) {
                if (SUCCESS_CODE.equals(wsResult.getIsSuccess())) {
                    allSuccess = false;
                }
            }
            if (allSuccess) {
                Log(logResult, SUCCESS_CODE,
                        ResultEntity.SUCCESS().resultXML(syscodesyncode, syscode, uniqueid, results), "", p_interface_type, p_content,
                        ResultEntity.SUCCESS().getName(), startDate, null);
            } else {
                Log(logResult, FAILURE_CODE,
                        ResultEntity.PARTLY_SUCCESS().resultXML(syscodesyncode, syscode, uniqueid, results), "", p_interface_type,
                        p_content, ResultEntity.PARTLY_SUCCESS().getName(), startDate, null);
            }
            return ResultEntity.resultXML(syscodesyncode, syscode, uniqueid, results);
        } catch (Exception e) {
            Log(logResult, FAILURE_CODE, ResultEntity.resultXML(syscodesyncode, syscode, uniqueid, results), "", p_interface_type,
                    p_content, "其他错误", startDate, e);
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
