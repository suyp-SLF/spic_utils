package kd.cus.conmWebservice.register;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.SelectUtils;
import kd.cus.api.entity.FilterEntity;
import kd.cus.conmWebservice.action.WebserviceMainTemplate;
import kd.cus.conmWebservice.action.entity.ResultEntity;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Purcontract implements WebserviceMainTemplate {

    private static final String CONM_PURCONTRACT_LOGO = "conm_purcontract";//采购合同
    private static final String CONM_SALCONTRACT_LOGO = "conm_salcontract";
    private static final String SPIC_CONM_TYPE_LOGO = "spic_conm_type";//二开合同类型
    private static final String BOS_USER_LOGO = "bos_user";//人员
    private static final String BOS_ORG_LOGO = "bos_org";//组织

    private static final String BD_SUPPLIER_LOGO = "bd_supplier";//供应商
    private static final String BD_BIZPARTNER_LOGO = "bd_bizpartner";//商务伙伴
    private static final String BD_CURRENCY_LOGO = "bd_currency";//币别

    private static final String BOS_ASSISTANTDATA_DETAIL = "bos_assistantdata_detail";//辅助资料

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Map<String, DynamicObject> list = new HashMap<>();

    private enum TYPE {
        AMOUNT, TIMER, BOOBLEN, DEFAULT;
    }

    /**
     * 开始进行数据解析
     *
     * @param p_source
     * @return
     * @throws Exception
     */
    public List<ResultEntity> disposeDate(Element child, String p_source) throws Exception {
        // TODO Auto-generated method stub
        List<ResultEntity> resultEntitys = new ArrayList<>();
        return save(resultEntitys, child);
    }

    private List<ResultEntity> save(List<ResultEntity> resultEntitys, Element child) throws Exception {
        List<DynamicObject> dataEntities = new ArrayList<>();
        Map<String, String> unique = new HashMap<String, String>();
        List<FilterEntity> filterEntities = new ArrayList<>();
        Element DATAINFO = child.element("DATAINFO");
        String billname = DATAINFO.elementText("CONTRACTNAME"),//合同编号
                billno = DATAINFO.elementText("CONTRACTCODE"),//合同编号
                spic_contracttype = DATAINFO.elementText("CONTRACTTYPE"),//合同类型编号
                spic_contractor = DATAINFO.elementText("UNDERTAKEPERSONACCOUNT"),//承办人
                spic_contractdate = DATAINFO.elementText("CREATIONTIME"),//承办日期
                spic_relatedproject = DATAINFO.elementText("ISRELATEDPROJECT"),//关联项目
                spic_valuetype = DATAINFO.elementText("VALUATIONMODE"),//计价方式
                spic_purchasetype = DATAINFO.elementText("PURCHASETYPE"),//采购方式
                spic_contractobject = DATAINFO.elementText("CONTRACTSUBJECT"),//合同标的
                direct = DATAINFO.elementText("PAYMENTDIRECTION"),
                //====================================================================================================//本方
                this_code = DATAINFO.elementText("SIGNINGSUBJECTCODE");
        //====================================================================================================//对方
        Element OPPOSITE = (Element) child.element("OPPOSITES").elements("OPPOSITE").get(0);
        String opposite_name = OPPOSITE.elementText("FULLNAME"),//对方
                opposite_contactperson = OPPOSITE.elementText("RELATIONNAME"),//联系人
                opposite_paybank = OPPOSITE.elementText("BANKNAME"),//开户银行
                opposite_paybankaccount = OPPOSITE.elementText("BANKACCOUNT"),//银行账户
                opposite_paybanknum = OPPOSITE.elementText("BANKNUM");//银行账号
        //====================================================================================================//第三
        Element THIRD = (Element) child.element("OPPOSITES").elements("OPPOSITE").get(0);
        String third_name = THIRD.elementText("FULLNAME"),//对方
                third_contactperson = THIRD.elementText("RELATIONNAME"),//联系人
                third_paybank = THIRD.elementText("BANKNAME"),//开户银行
                third_paybankaccount = THIRD.elementText("BANKACCOUNT"),//银行账户
                third_paybanknum = THIRD.elementText("BANKNUM");//银行账号
        //====================================================================================================//金额
        String settlecurrency = DATAINFO.elementText("CURRENCYNAME"),
                totalallamount = DATAINFO.elementText("CONTRACTAMOUNT"),
                totalamount = DATAINFO.elementText("CONTRACT_AMOUNT_NOTAX");
        //====================================================================================================//付款计划
        List<Element> CONTRACTPLANS = child.element("CONTRACTPLANS").elements("CONTRACTPLAN");
        CONTRACTPLANS.forEach(CONTRACTPLAN -> {
            String paydate = CONTRACTPLAN.elementText("DUEDATE"),
                    payamount = CONTRACTPLAN.elementText("PLANAMOUNT"),
                    payrate = CONTRACTPLAN.elementText("RECEIPTPAYPLANPERCENT");
        });

        //查询
        DynamicObject orgDy = BusinessDataServiceHelper.loadSingle(BOS_ORG_LOGO, "", new QFilter[]{new QFilter("number", QCP.equals, this_code)});
        String orgId = "";
        if (null != orgDy) {
            orgId = orgDy.getPkValue().toString();
        }
        list.put(BOS_ORG_LOGO + "@_@" + "number" + "@_@" + this_code, orgDy);
        filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_contracttype));
        filterEntities.add(new FilterEntity(BOS_USER_LOGO, "number", spic_contractor));
        filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_valuetype));
        filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_purchasetype));
        filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_contractobject));
//            filterEntities.add(new FilterEntity(BOS_ORG_LOGO, "number", org));
//            filterEntities.add(new FilterEntity(BD_SUPPLIER_LOGO, "number", supplier, orgId));
//            filterEntities.add(new FilterEntity(BD_BIZPARTNER_LOGO, "number", partc));
        filterEntities.add(new FilterEntity(BD_CURRENCY_LOGO, "number", settlecurrency));
        list = SelectUtils.loadAll(list, filterEntities);

        if ("1".equals(direct)) {//采购合同
            String properites = StringUtils.join(MetadataServiceHelper.getDataEntityType(CONM_PURCONTRACT_LOGO).getAllFields().entrySet().stream().map(Map.Entry::getKey).toArray(), ",");
            QFilter[] qFilters = new QFilter[]{new QFilter("billno", QCP.equals, billno)};
            DynamicObject this_dy = BusinessDataServiceHelper.loadSingle(CONM_PURCONTRACT_LOGO, properites, qFilters);
            if (null == this_dy) {
                Map<Class<?>, Object> services = new HashMap<>();
                DynamicFormModelProxy model = new DynamicFormModelProxy(CONM_PURCONTRACT_LOGO, UUID.randomUUID().toString(), services);
                model.createNewData();
                this_dy = model.getDataEntity();
            }

            this_dy.set("billname", billname);
            this_dy.set("billno", billno);//
            this_dy.set("spic_contracttype", check(BOS_ASSISTANTDATA_DETAIL, spic_contracttype));
            this_dy.set("spic_contractor", check(BOS_USER_LOGO, spic_contractor));
            this_dy.set("spic_contractdate", check(TYPE.TIMER, spic_contractdate));
            this_dy.set("spic_relatedproject", check(TYPE.BOOBLEN, spic_relatedproject));
            this_dy.set("spic_valuetype", check(BOS_ASSISTANTDATA_DETAIL, spic_valuetype));
            this_dy.set("spic_purchasetype", check(BOS_ASSISTANTDATA_DETAIL, spic_purchasetype));
            this_dy.set("spic_contractobject", check(BOS_ASSISTANTDATA_DETAIL, spic_contractobject));

            this_dy.set("org", check(BOS_ORG_LOGO, this_code));
//            this_dy.set("contactperson1st", check(TYPE.DEFAULT, contactperson1st));
//            this_dy.set("spic_paybank1st", check(TYPE.DEFAULT, spic_paybank1st));
//            this_dy.set("spic_paybankaccount1st", check(TYPE.DEFAULT, spic_paybankaccount1st));
//            this_dy.set("spic_paybanknum1st", check(TYPE.DEFAULT, spic_paybanknum1st));
//            this_dy.set("spic_paybankname1st", check(TYPE.DEFAULT, spic_paybankname1st));
//            this_dy.set("phone1st", check(TYPE.DEFAULT, phone1st));

//                this_dy.set("supplier", check(BD_SUPPLIER_LOGO, supplier));
            this_dy.set("party2nd", check(TYPE.DEFAULT, opposite_name));
            this_dy.set("contactperson2nd", check(TYPE.DEFAULT, opposite_contactperson));
//                this_dy.set("spic_paybank2nd", check(TYPE.DEFAULT, ));
            this_dy.set("spic_paybankaccount2nd", check(TYPE.DEFAULT, opposite_paybankaccount));
            this_dy.set("spic_paybanknum2nd", check(TYPE.DEFAULT, opposite_paybanknum));
            this_dy.set("spic_paybankname2nd", check(TYPE.DEFAULT, opposite_paybank));
//                this_dy.set("phone2nd", check(TYPE.DEFAULT, phone2nd));

            this_dy.set("partc", check(BD_BIZPARTNER_LOGO, third_name));
            this_dy.set("spic_contactperson3rd", check(TYPE.DEFAULT, third_contactperson));
//                this_dy.set("spic_paybank3rd", check(TYPE.DEFAULT, spic_paybank3rd));
            this_dy.set("spic_paybankaccount3rd", check(TYPE.DEFAULT, third_paybankaccount));
            this_dy.set("spic_paybanknum3rd", check(TYPE.DEFAULT, third_paybanknum));
            this_dy.set("spic_paybankname3rd", check(TYPE.DEFAULT, third_paybank));
//                this_dy.set("spic_phone3rd", check(TYPE.DEFAULT, spic_phone3rd));

            this_dy.set("settlecurrency", check(BD_CURRENCY_LOGO, settlecurrency));
            this_dy.set("totalallamount", check(TYPE.AMOUNT, totalallamount));
            this_dy.set("totalamount", check(TYPE.AMOUNT, totalamount));

            DynamicObjectCollection payentry_COL = this_dy.getDynamicObjectCollection("payentry");
            payentry_COL.clear();
            DynamicObjectType payentry_YTPE = payentry_COL.getDynamicObjectType();
            for (Element CONTRACTPLAN : CONTRACTPLANS) {
                DynamicObject payentry_ONE = new DynamicObject(payentry_YTPE);
                String paydate = CONTRACTPLAN.elementText("DUEDATE"),//付款日期---PAYDATE
                        payamount = CONTRACTPLAN.elementText("PLANAMOUNT"),//付款金额---PAYAMOUNT
                        payrate = CONTRACTPLAN.elementText("RECEIPTPAYPLANPERCENT");//付款比例---PAYRATE
                payentry_ONE.set("paydate", check(TYPE.TIMER, paydate));
                payentry_ONE.set("payamount", check(TYPE.AMOUNT, payamount));
                payentry_ONE.set("payrate", check(TYPE.DEFAULT, payrate));
                payentry_COL.add(payentry_ONE);
            }
            dataEntities.add(this_dy);

            // 保存/更新到数据库
            OperationResult operationResult = OperationServiceHelper.executeOperate("save", CONM_PURCONTRACT_LOGO, dataEntities.toArray(new DynamicObject[dataEntities.size()]), OperateOption.create());
            Map<String, String> errorMsgs = new HashMap<>();
            operationResult.getAllErrorOrValidateInfo().forEach(iOperateInfo -> {
                if (errorMsgs.size() == 0) {
                    errorMsgs.put(iOperateInfo.getPkValue().toString(), iOperateInfo.getMessage());
                } else {
                    errorMsgs.put(iOperateInfo.getPkValue().toString(), errorMsgs.get(iOperateInfo.getPkValue().toString()) + iOperateInfo.getMessage());
                }
            });
            errorMsgs.entrySet().forEach(errorMsg -> {
                resultEntitys.add(ResultEntity.PROCESS_ERROR(errorMsg.getValue()).setInitDate(errorMsg.getKey(), unique.get(errorMsg.getKey().toString()), null));
            });
            operationResult.getSuccessPkIds().forEach(successMsg -> {
                resultEntitys.add(ResultEntity.SUCCESS().setInitDate(successMsg.toString(), unique.get(successMsg), null));
            });
            return resultEntitys;
        } else {//销售合同
            String properites = StringUtils.join(MetadataServiceHelper.getDataEntityType(CONM_SALCONTRACT_LOGO).getAllFields().entrySet().stream().map(Map.Entry::getKey).toArray(), ",");
            QFilter[] qFilters = new QFilter[]{new QFilter("billno", QCP.equals, billno)};
            DynamicObject this_dy = BusinessDataServiceHelper.loadSingle(CONM_SALCONTRACT_LOGO, properites, qFilters);
            if (null == this_dy) {
                Map<Class<?>, Object> services = new HashMap<>();
                DynamicFormModelProxy model = new DynamicFormModelProxy(CONM_SALCONTRACT_LOGO, UUID.randomUUID().toString(), services);
                model.createNewData();
                this_dy = model.getDataEntity();
            }

            this_dy.set("billname", billname);
            this_dy.set("billno", billno);//
            this_dy.set("spic_contracttype", check(BOS_ASSISTANTDATA_DETAIL, spic_contracttype));
            this_dy.set("spic_contractor", check(BOS_USER_LOGO, spic_contractor));
            this_dy.set("spic_contractdate", check(TYPE.TIMER, spic_contractdate));
            this_dy.set("spic_relatedproject", check(TYPE.BOOBLEN, spic_relatedproject));
            this_dy.set("spic_valuetype", check(BOS_ASSISTANTDATA_DETAIL, spic_valuetype));
            this_dy.set("spic_purchasetype", check(BOS_ASSISTANTDATA_DETAIL, spic_purchasetype));
            this_dy.set("spic_contractobject", check(BOS_ASSISTANTDATA_DETAIL, spic_contractobject));

            this_dy.set("org", check(BOS_ORG_LOGO, this_code));
            this_dy.set("party1st", check(TYPE.DEFAULT, opposite_name));
            this_dy.set("contactperson1st", check(TYPE.DEFAULT, opposite_contactperson));
//            this_dy.set("spic_paybank1st", check(TYPE.DEFAULT, spic_paybank1st));
            this_dy.set("spic_paybankaccount1st", check(TYPE.DEFAULT, opposite_paybankaccount));
            this_dy.set("spic_paybanknum1st", check(TYPE.DEFAULT, opposite_paybanknum));
            this_dy.set("spic_paybankname1st", check(TYPE.DEFAULT, opposite_paybank));
//            this_dy.set("phone1st", check(TYPE.DEFAULT, phone1st));

//                this_dy.set("supplier", check(BD_SUPPLIER_LOGO, supplier));
//            this_dy.set("party2nd", check(TYPE.DEFAULT, opposite_name));
//            this_dy.set("contactperson2nd", check(TYPE.DEFAULT, opposite_contactperson));
//                this_dy.set("spic_paybank2nd", check(TYPE.DEFAULT, ));
//            this_dy.set("spic_paybankaccount2nd", check(TYPE.DEFAULT, opposite_paybankaccount));
//            this_dy.set("spic_paybanknum2nd", check(TYPE.DEFAULT, opposite_paybanknum));
//            this_dy.set("spic_paybankname2nd", check(TYPE.DEFAULT, opposite_paybank));
//                this_dy.set("phone2nd", check(TYPE.DEFAULT, phone2nd));

            this_dy.set("partc", check(BD_BIZPARTNER_LOGO, third_name));
            this_dy.set("spic_contactperson3rd", check(TYPE.DEFAULT, third_contactperson));
//                this_dy.set("spic_paybank3rd", check(TYPE.DEFAULT, spic_paybank3rd));
            this_dy.set("spic_paybankaccount3rd", check(TYPE.DEFAULT, third_paybankaccount));
            this_dy.set("spic_paybanknum3rd", check(TYPE.DEFAULT, third_paybanknum));
            this_dy.set("spic_paybankname3rd", check(TYPE.DEFAULT, third_paybank));
//                this_dy.set("spic_phone3rd", check(TYPE.DEFAULT, spic_phone3rd));

            this_dy.set("settlecurrency", check(BD_CURRENCY_LOGO, settlecurrency));
            this_dy.set("totalallamount", check(TYPE.AMOUNT, totalallamount));
            this_dy.set("totalamount", check(TYPE.AMOUNT, totalamount));

            DynamicObjectCollection payentry_COL = this_dy.getDynamicObjectCollection("payentry");
            payentry_COL.clear();
            DynamicObjectType payentry_YTPE = payentry_COL.getDynamicObjectType();
            for (Element CONTRACTPLAN : CONTRACTPLANS) {
                DynamicObject payentry_ONE = new DynamicObject(payentry_YTPE);
                String paydate = CONTRACTPLAN.elementText("DUEDATE"),//付款日期---PAYDATE
                        payamount = CONTRACTPLAN.elementText("PLANAMOUNT"),//付款金额---PAYAMOUNT
                        payrate = CONTRACTPLAN.elementText("RECEIPTPAYPLANPERCENT");//付款比例---PAYRATE
                payentry_ONE.set("paydate", check(TYPE.TIMER, paydate));
                payentry_ONE.set("payamount", check(TYPE.AMOUNT, payamount));
                payentry_ONE.set("payrate", check(TYPE.DEFAULT, payrate));
                payentry_COL.add(payentry_ONE);
            }
            dataEntities.add(this_dy);

            // 保存/更新到数据库
            OperationResult operationResult = OperationServiceHelper.executeOperate("save", CONM_SALCONTRACT_LOGO, dataEntities.toArray(new DynamicObject[dataEntities.size()]), OperateOption.create());
            Map<String, String> errorMsgs = new HashMap<>();
            operationResult.getAllErrorOrValidateInfo().forEach(iOperateInfo -> {
                if (errorMsgs.size() == 0) {
                    errorMsgs.put(iOperateInfo.getPkValue().toString(), iOperateInfo.getMessage());
                } else {
                    errorMsgs.put(iOperateInfo.getPkValue().toString(), errorMsgs.get(iOperateInfo.getPkValue().toString()) + iOperateInfo.getMessage());
                }
            });
            errorMsgs.entrySet().forEach(errorMsg -> {
                resultEntitys.add(ResultEntity.PROCESS_ERROR(errorMsg.getValue()).setInitDate(errorMsg.getKey(), unique.get(errorMsg.getKey().toString()), null));
            });
            operationResult.getSuccessPkIds().forEach(successMsg -> {
                resultEntitys.add(ResultEntity.SUCCESS().setInitDate(successMsg.toString(), unique.get(successMsg), null));
            });
            return resultEntitys;
        }
    }

    /**
     * 校验基础资料
     *
     * @param entityNumber
     * @param properite
     * @param value
     * @return
     */
    private Object check(String entityNumber, String properite, String value) {
        return list.get(entityNumber + "@_@" + properite + "@_@" + value);
    }

    private Object check(String entityNumber, String value) {
        return check(entityNumber, "number", value);
    }


    /**
     * 校验转换参数
     *
     * @param type
     * @param value
     * @return
     * @throws ParseException
     */
    private Object check(TYPE type, String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            value = "";
        }
        switch (type) {
            case DEFAULT:
                return value;
            case TIMER:
                return simpleDateFormat.parse(value);
            case BOOBLEN:
                return "1".equals(value) ? true : false;
            case AMOUNT:
                BigDecimal bigDecimal = new BigDecimal(value);
                return bigDecimal.multiply(new BigDecimal(10000));
        }
        return null;
    }
}

//            String billname = child.element("CONTRACTNAME").getText(),//合同名称---NAME
//                    billno = child.element("NUMBER").getText(),//单据编号---NUMBER
//                    spic_contracttype = child.element("CONTACTTYPE").getText(),//合同类型---CONTACTTYPE
//                    spic_contractor = child.element("CONTACTOR").getText(),//承办人---CONTACTOR
//                    spic_contractdate = child.element("CONTACTDATE").getText(),//承办日期--CONTACTDATE
//                    spic_relatedproject = child.element("RELATEPROJECT").getText(),//关联项目---RELATEPROJECT
//                    spic_valuetype = child.element("VALUETYPE").getText(),//计价方式---VALUETYPE
//                    spic_purchasetype = child.element("PURCHASETYPE").getText(),//采购方式---PURCHASETYPE
//                    spic_contractobject = child.element("CONTACTOBJECT").getText(),//合同标的---CONTACTOBJECT
//                    //====================================================================================================
//                    org = child.element("ORG").getText(),//采购组织---ORG
//                    contactperson1st = child.element("CONTACTPERSON1ST").getText(),//甲方联系人---CONTACTPERSON1ST
//                    spic_paybank1st = child.element("PAYBANK1ST").getText(),//甲方开户银行---PAYBANK1ST
//                    spic_paybankaccount1st = child.element("PAYBANKACCOUNT1ST").getText(),//甲方银行账户---PAYBANKACCOUNT1ST
//                    spic_paybanknum1st = child.element("PAYBANKNUM1ST").getText(),//甲方银行账号---PAYBANKNUM1ST
//                    spic_paybankname1st = child.element("PAYBANKNAME1ST").getText(),//甲方银行行号---PAYBANKNAME1ST
//                    phone1st = child.element("PHONE1ST").getText(),//甲方联系电话---PHONE1ST
//                    //====================================================================================================
//                    supplier = child.element("SUPPLIER").getText(),//供应商---SUPPLIER
//                    party2nd = child.element("PARTY2ND").getText(),//乙方---PARTY2ND
//                    contactperson2nd = child.element("CONTACTPERSON2ND").getText(),//乙方联系人---CONTACTPERSON2ND
//                    spic_paybank2nd = child.element("PAYBANK2ND").getText(),//乙方开户银行---PAYBANK2ND
//                    spic_paybankaccount2nd = child.element("PAYBANKACCOUNT2ND").getText(),//乙方银行账户---PAYBANKACCOUNT2ND
//                    spic_paybanknum2nd = child.element("PAYBANKNUM2ND").getText(),//乙方银行账号---PAYBANKNUM2ND
//                    spic_paybankname2nd = child.element("PAYBANKNAME2ND").getText(),//乙方银行行号---PAYBANKNAME2ND
//                    phone2nd = child.element("PHONE2ND").getText(),//乙方联系电话---PHONE2ND
//                    //====================================================================================================
//                    partc = child.element("PARTC").getText(),//第三方---PARTC
//                    spic_contactperson3rd = child.element("CONTACTPERSON3RD").getText(),//第三方联系人---CONTACTPERSON3RD
//                    spic_paybank3rd = child.element("PAYBANK3RD").getText(),//第三方开户银行---PAYBANK3RD
//                    spic_paybankaccount3rd = child.element("PAYBANKACCOUNT3RD").getText(),//第三方银行账户---PAYBANKACCOUNT3RD
//                    spic_paybanknum3rd = child.element("PAYBANKNUM3RD").getText(),//第三方银行账号---PAYBANKNUM3RD
//                    spic_paybankname3rd = child.element("PAYBANKNAME3RD").getText(),//第三方银行行号---PAYBANKNAME3RD
//                    spic_phone3rd = child.element("PHONE3RD").getText(),//第三方联系电话---PHONE3RD
//                    //====================================================================================================//
//                    settlecurrency = child.element("SETTLECURRENCY").getText(),//结算币别---SETTLECURRENCY
//                    totalallamount = child.element("TOTALALLAMOUNT").getText(),//价税合计---TOTALALLAMOUNT
//                    totalamount = child.element("TOTALAMOUNT").getText();//金额---TOTALAMOUNT
////                    paydate = child.element("PAYDATE").getText(),//付款日期---PAYDATE
////                    payamount = child.element("PAYAMOUNT").getText(),//付款金额---PAYAMOUNT
////                    payrate = child.element("PAYRATE").getText();//付款比例---PAYRATE
//            List<Element> payentry = child.element("PAYENTRYS").elements("PAYENTRY");
