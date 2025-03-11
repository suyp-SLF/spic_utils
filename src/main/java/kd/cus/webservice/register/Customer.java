package kd.cus.webservice.register;

import kd.bd.master.consts.OperateConsts;
import kd.bd.master.util.GroupStandardUtils;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.MuliLangTextProp;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.ParseResultMessageUtil;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;
import kd.isc.iscb.platform.core.util.setter.MuliLangTextPropSetter;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.*;

/*
 示例数据：
 <?xml version="1.0" encoding="UTF-8"?>
<DATAINFOS SYSCODESYNCODE="kh" UNIQUEID="" SYSCODE="KHZSJ" COMPANY="三维天地科技有限公司" SENDTIME="(yyyy-MM-dd HH:mm:ss)">
    <DATAINFO>
        <CODE REMARK="客户编码"></CODE>
        <DESC1 REMARK="客户名称"></DESC1>
        <DESC2 REMARK="客户分类"></DESC2>
        <DESC3 REMARK="主要购买产品分类"></DESC3>
        <DESC4 REMARK="主要购买产品"></DESC4>
        <DESC5 REMARK="纳税登记证号"></DESC5>
        <DESC6 REMARK="组织机构代码"></DESC6>
        <DESC7 REMARK="法定代表人"></DESC7>
        <DESC8 REMARK="注册资本"></DESC8>
        <DESC9 REMARK="成立时间"></DESC9>
        <DESC10 REMARK="注册地址"></DESC10>
        <DESC11 REMARK="国家"></DESC11>
        <DESC12 REMARK="省/市"></DESC12>
        <DESC13 REMARK="城市"></DESC13>
        <DESC14 REMARK="行政区域代码"></DESC14>
        <DESC15 REMARK="邮政编码"></DESC15>
        <DESC16 REMARK="电话"></DESC16>
        <DESC17 REMARK="传真"></DESC17>
        <DESC18 REMARK="网址"></DESC18>
        <DESC19 REMARK="邮箱"></DESC19>
        <DESC20 REMARK="公司曾用名称"></DESC20>
        <DESC21 REMARK="总公司"></DESC21>
        <DESC22 REMARK="母公司"></DESC22>
        <DESC23 REMARK="英文名称"></DESC23>
        <DESC24 REMARK="简称"></DESC24>
        <DESC25 REMARK="主要业务范围"></DESC25>
        <DESC26 REMARK="公司类型"></DESC26>
        <DESC27 REMARK="客户稳定性"></DESC27>
        <CODEID REMARK="MDM主键"></CODEID>
        <FREEZEFLAG REMARK="数据状态"></FREEZEFLAG>
        <MNEMONICCODE REMARK="助记码"></MNEMONICCODE>
        <RECORDERCODE REMARK="制单人编码">admin</RECORDERCODE>
        <RECORDERDESC REMARK="制单人名称">超级管理员</RECORDERDESC>
        <RECORDTIME REMARK="制单时间">2010-05-01</RECORDTIME>
        <RECORDERDCORP REMARK="制单人单位编码">1000</RECORDERDCORP>
        <SUBMITCORP REMARK="提报单位编码">1000</SUBMITCORP>
        <AUDITORCODE REMARK="审核人编码">admin</AUDITORCODE>
        <AUDITORDESC REMARK="审核人名称">超级管理员</AUDITORDESC>
        <AUDITTIME REMARK="审核时间">2010-05-01</AUDITTIME>
        <VERSION REMARK="主数据版本"></VERSION>
        <SYSCODEVERSION REMARK="主数据模型版本"></SYSCODEVERSION>
        <SPECIALITYCODES>
            <SPECIALITYCODE SPECIALITYNAME="客户信息" SPECIALITYCODE="KHXX">
                <PROPERTYCODE PROPERTYNAME="客户名称" PROPERTYCODE="mc" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="客户分类" PROPERTYCODE="khfl" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要购买产品分类" PROPERTYCODE="cpfl" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要购买产品" PROPERTYCODE="gmcp" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="纳税登记证号" PROPERTYCODE="nsdjzh" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="组织机构代码" PROPERTYCODE="zzjgdm" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="法定代表人" PROPERTYCODE="fddbr" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="注册资本" PROPERTYCODE="zczb" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="成立时间" PROPERTYCODE="clsj" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="注册地址" PROPERTYCODE="zcdz" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="国家" PROPERTYCODE="gj" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="省/市" PROPERTYCODE="ss" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="城市" PROPERTYCODE="cs" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="行政区域代码" PROPERTYCODE="xxqy" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="邮政编码" PROPERTYCODE="yzbm" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="电话" PROPERTYCODE="dh" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="传真" PROPERTYCODE="cz" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="网址" PROPERTYCODE="wz" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="邮箱" PROPERTYCODE="yx" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="公司曾用名称" PROPERTYCODE="gscym" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="总公司" PROPERTYCODE="zgs" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="母公司" PROPERTYCODE="mgs" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="英文名称" PROPERTYCODE="ywmc" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="简称" PROPERTYCODE="jc" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要业务范围" PROPERTYCODE="ywfw" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="公司类型" PROPERTYCODE="gslx" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="客户稳定性" PROPERTYCODE="khwdx" STANDARDNAME="" STANDARDCODE="jnkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="客户名称" PROPERTYCODE="mc" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="客户分类" PROPERTYCODE="khfl" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要购买产品分类" PROPERTYCODE="cpfl" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要购买产品" PROPERTYCODE="gmcp" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="纳税登记证号" PROPERTYCODE="nsdjzh" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="组织机构代码" PROPERTYCODE="zzjgdm" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="法定代表人" PROPERTYCODE="fddbr" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="注册资本" PROPERTYCODE="zczb" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="成立时间" PROPERTYCODE="clsj" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="注册地址" PROPERTYCODE="zcdz" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="国家" PROPERTYCODE="gj" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="省/市" PROPERTYCODE="ss" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="城市" PROPERTYCODE="cs" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="行政区域代码" PROPERTYCODE="xxqy" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="邮政编码" PROPERTYCODE="yzbm" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="电话" PROPERTYCODE="dh" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="传真" PROPERTYCODE="cz" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="网址" PROPERTYCODE="wz" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="邮箱" PROPERTYCODE="yx" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="公司曾用名称" PROPERTYCODE="gscym" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="总公司" PROPERTYCODE="zgs" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="母公司" PROPERTYCODE="mgs" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="英文名称" PROPERTYCODE="ywmc" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="简称" PROPERTYCODE="jc" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="主要业务范围" PROPERTYCODE="ywfw" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="公司类型" PROPERTYCODE="gslx" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
                <PROPERTYCODE PROPERTYNAME="客户稳定性" PROPERTYCODE="khwdx" STANDARDNAME="" STANDARDCODE="jwkh" PREFIX="前置符号" SUFFIX="后置符号" BOUNDSYMBOL="连接符" UNIT="计量单位">元属性值</PROPERTYCODE>
            </SPECIALITYCODE>
            <SPECIALITYCODE SPECIALITYNAME="联系人信息" SPECIALITYCODE="LXRXX">
                <VALUELIST REMARK="多值">
                    <PROPERTYCODE PROPERTYNAME="联系人" PROPERTYCODE="lxr">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="联系人电话" PROPERTYCODE="lxrdh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="联系地址" PROPERTYCODE="lxdz">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="联系人e-mail" PROPERTYCODE="lwryx">启用多值元属性值</PROPERTYCODE>
                </VALUELIST>
            </SPECIALITYCODE>
            <SPECIALITYCODE SPECIALITYNAME="银行信息" SPECIALITYCODE="YHXX">
                <VALUELIST REMARK="多值">
                    <PROPERTYCODE PROPERTYNAME="开户银行" PROPERTYCODE="khyh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="开户银行分支行" PROPERTYCODE="fzh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="银行账号" PROPERTYCODE="yhzh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="银行联行行号" PROPERTYCODE="lhh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="银行结算账户" PROPERTYCODE="jszh">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="银行注册国家" PROPERTYCODE="yhzcgj">启用多值元属性值</PROPERTYCODE>
                    <PROPERTYCODE PROPERTYNAME="银行账户终止日期" PROPERTYCODE="zhzzrq">启用多值元属性值</PROPERTYCODE>
                </VALUELIST>
            </SPECIALITYCODE>
        </SPECIALITYCODES>
    </DATAINFO>
</DATAINFOS>
 */

/**
 * @author yanqi_wu
 * @time 2020-7-14 17:52:22
 */
public class Customer implements WebserviceMainTemplate {

    // 常量定义
    private static final String SHARESTRATEGY = "1"; // 共享集中式

    @Override
    public List<ResultEntity> disposeDate(List<Element> children) throws Exception {
        Log logBack = LogFactory.getLog(Supplier.class);

        // 查询业务单元中，策略为共享集中式的组织id
        DynamicObject[] orgs = BusinessDataServiceHelper.load("bos_org", "id", new QFilter[]{new QFilter("spic_sharestrategy", QCP.equals, SHARESTRATEGY)});

        List<ResultEntity> resultEnties = new ArrayList<ResultEntity>();
        List<DynamicObject> customers = new ArrayList<DynamicObject>();
        Map<String, String> idVersion = new HashMap<String, String>(); // <id, version>

        Map<String, String> numberId = new HashMap<>(); // <number, id>
        for (Element dataInfo : children) {
            // 1. 从element中读取各属性的值
            String CODE = dataInfo.element("CODE").getText(); // 客户编码
            String CODEID = dataInfo.element("CODEID").getText(); // MDM主键
            String VERSION = dataInfo.element("VERSION").getText(); // 主数据版本
            // 2. 以CODEID作为供应商在苍穹系统中的主键
            boolean exists = QueryServiceHelper.exists("bd_customer", CODEID);
            if (!exists) {
                // 新增
                Map<Class<?>, Object> services = new HashMap<>();
                DynamicFormModelProxy model = new DynamicFormModelProxy("bd_customer", UUID.randomUUID().toString(),
                        services);
                model.createNewData();
                // 新建客户对象
                DynamicObject customerObj = model.getDataEntity();
                // 设置主键
                customerObj.set("id", CODEID);
                customerObj.set("number", CODE); // 编码，如果有自己的编码规则，则不需要传值

                doExecute(dataInfo, customerObj, false);

                customers.add(customerObj);
                idVersion.put(CODEID, VERSION);
                numberId.put(CODE, CODEID);
            } else {
                // 更新
                DynamicObject customerFromDB = BusinessDataServiceHelper.loadSingle(CODEID, "bd_customer");
                doExecute(dataInfo, customerFromDB, true);
                customers.add(customerFromDB);
                idVersion.put(CODEID, VERSION);
                numberId.put(CODE, CODEID);
            }
        }
        // 保存/更新到数据库
        OperationResult operationResult = OperationServiceHelper.executeOperate("save", "bd_customer",
                customers.toArray(new DynamicObject[customers.size()]), OperateOption.create());
        List<IOperateInfo> OperateInfos = operationResult.getAllErrorOrValidateInfo();
        Map<Object, String> resultErrMap = new HashMap<>();
        for (IOperateInfo operateInfo : OperateInfos) {
            String number = ParseResultMessageUtil.parse(operateInfo.getMessage(), "");
            if (null == resultErrMap.get(number)) {
                resultErrMap.put(number, operateInfo.getMessage());
            } else {
                resultErrMap.put(number, resultErrMap.get(number) + operateInfo.getMessage());
            }
        }
        resultErrMap.entrySet().forEach(res -> {
            resultEnties.add(ResultEntity.PROCESS_ERROR(res.getValue()).setInitDate(numberId.get(res.getKey().toString()),
                    idVersion.get(numberId.get(res.getKey().toString())), "主数据"));
        });
        // 提交/审核
        List<Object> successPkIdsList = operationResult.getSuccessPkIds();
        if (successPkIdsList.size() <= 0) {
            return resultEnties;
        }
        Set<Object> successPkIds = new HashSet<Object>(successPkIdsList);
        // 提交
        DynamicObject[] successDynamicObjs = BusinessDataServiceHelper.load(successPkIds.toArray(),
                customers.get(0).getDynamicObjectType());
        OperationResult executeOperate1 = OperationServiceHelper.executeOperate("submit", "bd_customer",
                successDynamicObjs, null);
        Set<Object> submitSuccess = new HashSet<Object>(executeOperate1.getSuccessPkIds());
        // 审核
        DynamicObject[] successSubmitDynamicObjs = BusinessDataServiceHelper.load(submitSuccess.toArray(),
                customers.get(0).getDynamicObjectType());
        OperationResult executeOperate2 = OperationServiceHelper.executeOperate("audit", "bd_customer",
                successSubmitDynamicObjs, null);
        Set<Object> auditSuccess = new HashSet<Object>(executeOperate2.getSuccessPkIds());
        for (Object object : auditSuccess) {
            resultEnties.add(
                    ResultEntity.SUCCESS().setInitDate(object.toString(), idVersion.get(object.toString()), "主数据"));
        }
        logBack.info("开始进行共享集中式生成客商！");
        // 共享集中式，单独为orgs中的每个组织创建客商
        Arrays.stream(orgs).forEach(org -> {
            for (DynamicObject dy : customers) {
                dy.set("id", null);
                dy.set("createorg", org);
                dy.set("ctrlstrategy", 6);
            }
            OperationResult sOperationResult = OperationServiceHelper.executeOperate("save", "bd_customer", customers.toArray(new DynamicObject[customers.size()]), OperateOption.create());
            logBack.info("共享集中式保存：" + formatOperationResultMessage(sOperationResult));
            List<Object> sSuccessPkIdsList = sOperationResult.getSuccessPkIds();
            Set<Object> sSuccessPkIds = new HashSet<Object>(sSuccessPkIdsList);
            DynamicObject[] sSuccessDynamicObjs = BusinessDataServiceHelper.load(sSuccessPkIds.toArray(), customers.get(0).getDynamicObjectType());
            OperationResult sExecuteOperate1 = OperationServiceHelper.executeOperate("submit", "bd_customer", sSuccessDynamicObjs, null);
            logBack.info("共享集中式提交：" + formatOperationResultMessage(sExecuteOperate1));
            Set<Object> sSubmitSuccess = new HashSet<Object>(sExecuteOperate1.getSuccessPkIds());
            // 审核
            OperationResult sExecuteOperate2 = OperationServiceHelper.executeOperate(OperateConsts.OP_AUDIT, "bd_customer", sSubmitSuccess.toArray(), null);
            logBack.info("共享集中式审核：" + formatOperationResultMessage(sExecuteOperate2));

        });
        return resultEnties;
    }

    /**
     * 解析OperationResult的message
     *
     * @param sOperationResult OperationServiceHelper.executeOperate执行结果对象
     * @return 所有错误信息追加成一条
     */
    private String formatOperationResultMessage(OperationResult sOperationResult) {
        if (sOperationResult == null) {
            return null;
        }
        StringBuilder message = new StringBuilder();
        message.append(sOperationResult.getMessage() + "@_@");
        List<IOperateInfo> allErrorOrValidateInfo = sOperationResult.getAllErrorOrValidateInfo();
        allErrorOrValidateInfo.forEach(info -> {
            message.append(info.getMessage() + "@_@");
        });

        return message.toString();
    }

    private void doExecute(Element dataInfo, DynamicObject customerObj, boolean isUpdate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String DESC1 = dataInfo.element("DESC1").getText(); // 客户名称
        String DESC2 = dataInfo.element("DESC2").getText(); // 客户分类
        String DESC3 = dataInfo.element("DESC3").getText(); // 主要购买产品分类
        String DESC4 = dataInfo.element("DESC4").getText(); // 主要购买产品
        String DESC5 = dataInfo.element("DESC5").getText(); // 纳税登记证号
        String DESC6 = dataInfo.element("DESC6").getText(); // 组织机构代码
        String DESC7 = dataInfo.element("DESC7").getText(); // 法定代表人
        String DESC8 = dataInfo.element("DESC8").getText(); // 注册资本
        String DESC9 = dataInfo.element("DESC9").getText(); // 成立时间
        String DESC10 = dataInfo.element("DESC10").getText(); // 注册地址
        String DESC11 = dataInfo.element("DESC11").getText(); // 国家
        String DESC12 = dataInfo.element("DESC12").getText(); // 省/市
        String DESC13 = dataInfo.element("DESC13").getText(); // 城市
        String DESC14 = dataInfo.element("DESC14").getText(); // 行政区域代码
        String DESC15 = dataInfo.element("DESC15").getText(); // 邮政编码
        String DESC16 = dataInfo.element("DESC16").getText(); // 电话
        String DESC17 = dataInfo.element("DESC17").getText(); // 传真
        String DESC18 = dataInfo.element("DESC18").getText(); // 网址
        String DESC19 = dataInfo.element("DESC19").getText(); // 邮箱
        String DESC20 = dataInfo.element("DESC20").getText(); // 公司曾用名称
        String DESC21 = dataInfo.element("DESC21").getText(); // 总公司
        String DESC22 = dataInfo.element("DESC22").getText(); // 母公司
        String DESC23 = dataInfo.element("DESC23").getText(); // 英文名称
        String DESC24 = dataInfo.element("DESC24").getText(); // 简称
        String DESC25 = dataInfo.element("DESC25").getText(); // 主要业务范围
        String DESC26 = dataInfo.element("DESC26").getText(); // 公司类型
        String DESC27 = dataInfo.element("DESC27").getText(); // 客户稳定性

        Element SPECIALITYCODES = dataInfo.element("SPECIALITYCODES");

        customerObj.set("name", DESC1); // 客户名称
        // 客户分类
        customerObj.set("spic_customertype", DESC2);
        // 主要购买产品分类
        customerObj.set("spic_pribuyproducttype", DESC3);
        // 主要购买产品
        customerObj.set("spic_mainbuyproduct", DESC4);
        // 纳税人识别号
        customerObj.set("tx_register_no", DESC5);
        // 组织机构代码
        customerObj.set("orgcode", DESC6);
        // 法人代表
        MuliLangTextProp artificialperson = new MuliLangTextProp();
        MuliLangTextPropSetter artificialpersonSetter = new MuliLangTextPropSetter(artificialperson);
        artificialpersonSetter.setObjValue(customerObj, "artificialperson", DESC7);
        // 注册资本（万)
        MuliLangTextProp regcapital = new MuliLangTextProp();
        MuliLangTextPropSetter regcapitalSetter = new MuliLangTextPropSetter(regcapital);
        regcapitalSetter.setObjValue(customerObj, "regcapital", DESC8);
        // 成立时间
        customerObj.set("establishdate", dateFormat.parse(DESC9)); // 日期格式：yyyy-MM-dd
        // 详细地址 bizpartner_address
        MuliLangTextProp bizpartner_address = new MuliLangTextProp();
        MuliLangTextPropSetter bizpartner_addressSetter = new MuliLangTextPropSetter(bizpartner_address);
        bizpartner_addressSetter.setObjValue(customerObj, "bizpartner_address", DESC10);
        // 国家
        DynamicObject country = BusinessDataServiceHelper.loadSingle("bd_country", "",
                new QFilter[]{new QFilter("name", QCP.equals, DESC11)});
        customerObj.set("country", country);
        // 行政区域代码 spic_regioncode
        customerObj.set("spic_regioncode", DESC14);
        // 邮政编码 spic_postcode
        customerObj.set("spic_postcode", DESC15);
        // 电话
        customerObj.set("bizpartner_phone", DESC16);
        // 传真
        customerObj.set("bizpartner_fax", DESC17);
        // 公司网址
        customerObj.set("url", DESC18);
        // 电子邮箱
        customerObj.set("postal_code", DESC19);
        // 公司曾用名称
        customerObj.set("spic_originname", DESC20);
        // 总公司 DESC21
        customerObj.set("spic_firstoffice", DESC21);
        // 母公司 DESC22
        customerObj.set("spic_parentoffice", DESC22);
        // 英文名称 DESC23
        customerObj.set("spic_englishname", DESC23);
        // 简称
        MuliLangTextProp simplename = new MuliLangTextProp();
        MuliLangTextPropSetter simplenameSetter = new MuliLangTextPropSetter(simplename);
        simplenameSetter.setObjValue(customerObj, "simplename", DESC24);
        // 经营范围
        MuliLangTextProp businessscope = new MuliLangTextProp();
        MuliLangTextPropSetter businessscopeSetter = new MuliLangTextPropSetter(businessscope);
        businessscopeSetter.setObjValue(customerObj, "businessscope", DESC25);
        // 公司类型 spic_type
        customerObj.set("spic_type", DESC26);
        // 客户稳定性 spic_customerstability
        customerObj.set("spic_customerstability", DESC27);

        // 根据行政组织代码获取行政组织对象
        DynamicObject adminorg = BusinessDataServiceHelper.loadSingle("bos_adminorg", "",
                new QFilter[]{new QFilter("number", QCP.equals, "100000")});
        // 创建组织
        customerObj.set("createorg", adminorg);
        // 分录数据填充
        List<Element> specialityItems = SPECIALITYCODES.elements("SPECIALITYCODE");
        for (Element specialitycode : specialityItems) {
            String specialitycodeAttr = specialitycode.attribute("SPECIALITYCODE").getText();
            if (StringUtils.equals(specialitycodeAttr, "LXRXX")) {
                // ---------------------联系人---------------------
                DynamicObjectCollection entry_linkman = customerObj.getDynamicObjectCollection("entry_linkman");
//				if (isUpdate) {
                entry_linkman.clear();
//				}
                List<Element> valuelist = specialitycode.elements("VALUELIST");
                boolean isDefault = true;
                for (Element value : valuelist) {
                    DynamicObject entry_linkmanRow = entry_linkman.addNew();
                    // 需要设置一个默认联系人，取第一条记录设置为默认
                    if (isDefault) {
                        entry_linkmanRow.set("isdefault_linkman", true);
                        isDefault = false;
                    }
                    List<Element> elements = value.elements("PROPERTYCODE");
                    for (Element element : elements) {
                        String attr = element.attribute("PROPERTYCODE").getText();
                        String val = element.getText();
                        if (StringUtils.equals(attr, "lxr")) {
                            // 联系人 contactperson
                            MuliLangTextProp contactperson = new MuliLangTextProp();
                            MuliLangTextPropSetter contactpersonSetter = new MuliLangTextPropSetter(contactperson);
                            contactpersonSetter.setObjValue(entry_linkmanRow, "contactperson", val);
                            continue;
                        }
                        if (StringUtils.equals(attr, "lxrdh")) {
                            // 联系电话 phone
                            entry_linkmanRow.set("phone", val);
                            continue;
                        }
                        if (StringUtils.equals(attr, "lwryx")) {
                            // 联系邮箱 email
                            entry_linkmanRow.set("email", val);
                            continue;
                        }
                        if (StringUtils.equals("lxdz", attr)) {
                            // 联系地址
                            entry_linkmanRow.set("address", val);
                            continue;
                        }
                    }
                }
            } else if (StringUtils.equals(specialitycodeAttr, "YHXX")) {
                // ---------------------银行信息---------------------
                DynamicObjectCollection entry_bankEntry = customerObj.getDynamicObjectCollection("entry_bank");
//				if (isUpdate) {
                entry_bankEntry.clear();
//				}
                List<Element> valuelist = specialitycode.elements("VALUELIST");
                boolean isDefault = true;
                for (Element value : valuelist) {
                    DynamicObject entry_bankRow = entry_bankEntry.addNew();
                    // 需要设置一个默认银行信息，取第一条记录设置为默认
                    if (isDefault) {
                        entry_bankRow.set("isdefault_bank", true);
                        isDefault = false;
                    }
                    List<Element> elements = value.elements("PROPERTYCODE");
                    for (Element element : elements) {
                        String attr = element.attribute("PROPERTYCODE").getText();
                        String val = element.getText();
                        // 开户银行
                        if (StringUtils.equals("khyh", attr)) {
                            DynamicObject bank = BusinessDataServiceHelper.loadSingle("bd_bebank", "",
                                    new QFilter[]{new QFilter("name", QCP.equals, val)});
                            entry_bankRow.set("bank", bank);
                            continue;
                        }
                        // 开户银行分支行 spic_fzh
                        if (StringUtils.equals("fzh", attr)) {
                            entry_bankRow.set("spic_fzh", val);
                            continue;
                        }
                        // 银行账号
                        if (StringUtils.equals("yhzh", attr)) {
                            entry_bankRow.set("bankaccount", val);
                            continue;
                        }
                        // 银行联行行号
                        if (StringUtils.equals("lhh", attr)) {
                            entry_bankRow.set("spic_lhh", val);
                            continue;
                        }
                        // 银行结算账户 accountname
                        if (StringUtils.equals("jszh", attr)) {
                            entry_bankRow.set("accountname", val);
                            continue;
                        }
                        // 银行注册国家
                        if (StringUtils.equals("yhzcgj", attr)) {
                            entry_bankRow.set("spic_bankcountry", val);
                            continue;
                        }
                        // 银行账户终止日期(格式：YYYY-MM-DD)
                        if (StringUtils.equals("zhzzrq", attr)) {
                            entry_bankRow.set("spic_bankaccexpiredate", dateFormat.parse(val));
                            continue;
                        }
                    }
                }
            }
        }
        // ---------------------分类标准---------------------
        // 设置分录
        DynamicObjectCollection groupstandardEntry = customerObj.getDynamicObjectCollection("entry_groupstandard");
//		if (isUpdate) {
        groupstandardEntry.clear();
//		}
        // 设置分类标准
        Set<Long> groupStandard = GroupStandardUtils.getGroupStandard("bd_customer",
                Long.valueOf(adminorg.getPkValue().toString()), Boolean.TRUE, (QFilter) null);
        for (Long standardId : groupStandard) {
            DynamicObject entryRow = groupstandardEntry.addNew();
            DynamicObject standardObj = BusinessDataServiceHelper.loadSingle(standardId, "bd_customergroupstandard");
            entryRow.set("standardid", standardObj);
        }

        // ---------------------其他信息---------------------
        // 控制策略
        customerObj.set("ctrlstrategy", "5"); // 1. 逐级分配；2. 自由分配；5. 全局共享；6. 管控范围内共享；7. 私有
        // 使用状态
        customerObj.set("enable", "1"); // 0. 禁用；1. 可用
        // 数据状态
        customerObj.set("status", "A"); // A. 暂存；B. 已提交；C. 已审核
    }
}
