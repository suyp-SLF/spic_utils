package kd.cus.treasuryWebservice.action.impl;

import kd.cus.treasuryWebservice.action.entity.ReqAppBodyEntity;
import kd.cus.treasuryWebservice.action.entity.ReqAppHeadEntity;
import kd.cus.treasuryWebservice.action.entity.ReqSysHeadEntity;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author suyp
 */
@WebService
public class WebserviceInterfaceImpl {

    private final static String WS_REGISTERED_BILLCODE = "spic_maindata_registered";// 注册接口的苍穹基础资料编码
    private final static String TARGETNAMESPACE_PARAMS = "http://impl.action.conmWebservice.cus.kd/";
    private final static String MET_PARAMS = "http://esb.dcitsbiz.com/metadata";
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
    public String Req9999200000103(@WebParam(name = "ReqSysHead", targetNamespace = MET_PARAMS) ReqSysHeadEntity ReqSysHead,
                          @WebParam(name = "ReqAppHead", targetNamespace = MET_PARAMS) ReqAppHeadEntity ReqAppHead,
                          @WebParam(name = "ReqAppBody", targetNamespace = MET_PARAMS) ReqAppBodyEntity ReqAppBody) {

        return null;
    }

}
