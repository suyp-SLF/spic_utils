package kd.cus.erpWebservice.register;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.clr.DataEntityType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.operate.result.IOperateInfo;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.operate.webapi.Save;
import kd.bos.entity.property.LongProp;
import kd.bos.entity.property.PKFieldProp;
import kd.bos.entity.property.VarcharProp;
import kd.bos.form.operate.webapi.OperateDataConverter;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.business.datamodel.DynamicFormModelProxy;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import kd.cus.api.SelectUtils;
import kd.cus.api.entity.FilterEntity;
import kd.cus.conmWebservice.register.Purcontract;
import kd.cus.erpWebservice.action.WebserviceMainTemplate;
import kd.cus.erpWebservice.action.entity.ResultEntity;
import kd.drp.pos.common.consts.DataEntity;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Contract implements WebserviceMainTemplate {

    private static final String CONM_PURCONTRACT_LOGO = "conm_purcontract";//采购合同
    private static final String CONM_SALCONTRACT_LOGO = "conm_salcontract";
    private static final String SPIC_CONM_TYPE_LOGO = "spic_conm_type";//二开合同类型
    private static final String BOS_USER_LOGO = "bos_user";//人员
    private static final String BOS_ORG_LOGO = "bos_org";//组织

    private static final String BD_SUPPLIER_LOGO = "bd_supplier";//供应商
    private static final String BD_BIZPARTNER_LOGO = "bd_bizpartner";//商务伙伴
    private static final String BD_CURRENCY_LOGO = "bd_currency";//币别

    private static final String BD_TYPEID_LOGO = "conm_type";//合同类型基础资料
    private static final String BD_TAXTABLE_LOGO = "bd_exratetable";//汇率表基础资料

    private static final String TYPEID_LOGO_ID = "type";//合同类型标识
    private static final String TAXTABLE_LOGO_ID = "exratetable";//汇率表标识
    private static final String CURRENCY_LOGO_ID = "currency";//本位币标识

    private static final String BOS_ASSISTANTDATA_DETAIL = "bos_assistantdata_detail";//辅助资料

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static String NutNullMsg = "";

    private enum TYPE {
        AMOUNT, TIMER, BOOBLEN, DEFAULT;
    }
    private Map<String, DynamicObject> list = new HashMap<>();

    @Override
    public List<ResultEntity> disposeDate(List<Element> children, String p_source) throws Exception {
        List<ResultEntity> resultEntitys = new ArrayList<>();
        return save(resultEntitys, children);
    }

    private List<ResultEntity> save(List<ResultEntity> resultEntitys, List<Element> children) throws Exception {
        Map<String, String> unique = new HashMap<String, String>();
        Map<String, String> notNullMap = new HashMap<>();
        List<DynamicObject> dataEntities = new ArrayList<>();
        List<FilterEntity> filterEntities = new ArrayList<>();
        DynamicObjectType type = MetadataServiceHelper.getDataEntityType(CONM_PURCONTRACT_LOGO);

        for (Element child : children){
            String valuationmode = child.elementText("VALUATIONMODE");
            String underTakePersonAccount = child.elementText("UNDERTAKEPERSONACCOUNT");
            String supplier = child.elementText("supplier");
            String org = child.elementText("org");
            String partc = child.elementText("partc");
            String settlecurrency = child.elementText("settlecurrency");

            String ftypeid = child.elementText("ftypeid");
            String fexratetableid = child.elementText("fexratetableid");
            String fcurrencyid = child.elementText("fcurrencyid");

            DynamicObject orgDy = BusinessDataServiceHelper.loadSingle(BOS_ORG_LOGO, "", new QFilter[]{new QFilter("number", QCP.equals, org)});
            String orgId = "null";
            if (null != orgDy) {
                orgId = orgDy.getPkValue().toString();
            }
            list.put(BOS_ORG_LOGO + "@_@" + "number" + "@_@" + org, orgDy);
//            filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_contracttype));
            filterEntities.add(new FilterEntity(BOS_USER_LOGO, "number", underTakePersonAccount));
            filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", valuationmode));
//            filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "numer", spic_purchasetype));
//            filterEntities.add(new FilterEntity(BOS_ASSISTANTDATA_DETAIL, "number", spic_contractobject));
//            filterEntities.add(new FilterEntity(BOS_ORG_LOGO, "number", org));
            filterEntities.add(new FilterEntity(BD_SUPPLIER_LOGO, "number", supplier, orgId));
            filterEntities.add(new FilterEntity(BD_BIZPARTNER_LOGO, "number", partc));
            filterEntities.add(new FilterEntity(BD_CURRENCY_LOGO, "number", settlecurrency));

            filterEntities.add(new FilterEntity(BD_TYPEID_LOGO,"number",ftypeid));
            filterEntities.add(new FilterEntity(BD_TAXTABLE_LOGO,"number",fexratetableid));
            filterEntities.add(new FilterEntity(BD_CURRENCY_LOGO, "number", fcurrencyid));

            list = SelectUtils.loadAll(list, filterEntities);
        }

        for (Element child : children) {
            String codeid = child.elementText("codeid");
            String contractname = child.elementText("contractname");
            String contractcode = child.elementText("contractcode");
            String undertakepersonacction = child.elementText("undertakepersonaccount");
            String creationtime = child.elementText("creationtime");
            String valuationmode = child.elementText("valuationmode");
            String org = child.elementText("org");
            String contractperson1st = child.elementText("contactperson1st");
            String supplier = child.elementText("supplier");
            String contactperson2nd = child.elementText("contactperson2nd");
            String spic_paybanknum2nd = child.elementText("spic_paybanknum2nd");
            String phone2nd = child.elementText("phone2nd");
            String partc = child.elementText("partc");
            String settlecurrency = child.elementText("settlecurrency");
            String totalallamount = child.elementText("totalallamount");

            String ftypeid = child.elementText("ftypeid");
            String fexratetableid = child.elementText("fexratetableid");
            String fcurrencyid = child.elementText("fcurrencyid");



            NutNullMsg = "";

            //查询
            ((MainEntityType) type).getAppId();
            PKFieldProp pkProp = (PKFieldProp)type.getPrimaryKey();
//            String properites = StringUtils.join(MetadataServiceHelper.getDataEntityType(CONM_PURCONTRACT_LOGO).getAllFields().entrySet().stream().map(Map.Entry::getKey).toArray(), ",");
            QFilter[] qFilters = new QFilter[]{new QFilter("billno", QCP.equals, contractcode)};
//            DynamicObject this_dy = BusinessDataServiceHelper.loadSingle(CONM_PURCONTRACT_LOGO, properites, qFilters);
            DynamicObject this_dy = BusinessDataServiceHelper.loadSingleFromCache(CONM_PURCONTRACT_LOGO,qFilters);
            if (null == this_dy) {
                Map<Class<?>, Object> services = new HashMap<>();
                DynamicFormModelProxy model = new DynamicFormModelProxy(CONM_PURCONTRACT_LOGO, UUID.randomUUID().toString(), services);
                model.createNewData();
                if (pkProp instanceof LongProp) {
                    model.getDataEntity().set(pkProp, DBServiceHelper.genGlobalLongId());
                } else if (pkProp instanceof VarcharProp) {
                    model.getDataEntity().set(pkProp, DBServiceHelper.genStringId());
                }
//                model.setValue("billname", contractname);
                this_dy = model.getDataEntity(true);
//                this_dy.set("billname", contractname);
//                model.updateCache();
                Object pkValue = model.getDataEntity().getPkValue();
                Object e = pkProp.getValueFast(this_dy);
                unique.put(e.toString(),codeid);
//                this_dy.getPkValue();
            }else{
                unique.put(this_dy.getPkValue().toString(),codeid);
            }
            NotNull(NutNullMsg,"codeid",codeid,codeid);
            this_dy.set("billname", NotNull(NutNullMsg,"billname",contractname,contractname));
            this_dy.set("billno", NotNull(NutNullMsg,"billno",contractcode,contractcode));//
//            this_dy.set("spic_contracttype", check(BOS_ASSISTANTDATA_DETAIL, spic_contracttype));
            this_dy.set("spic_contractor", NotNull(NutNullMsg,"spic_contractor",check(BOS_USER_LOGO, undertakepersonacction),undertakepersonacction));
            this_dy.set("spic_contractdate",  NotNull(NutNullMsg,"spic_contractdate",check(TYPE.TIMER, creationtime),creationtime));
//            this_dy.set("spic_relatedproject", check(TYPE.BOOBLEN, spic_relatedproject));
            this_dy.set("spic_valuetype",  NotNull(NutNullMsg,"spic_valuetype",check(BOS_ASSISTANTDATA_DETAIL, valuationmode),valuationmode));
//            this_dy.set("spic_purchasetype", check(BOS_ASSISTANTDATA_DETAIL, spic_purchasetype));
//            this_dy.set("spic_contractobject", check(BOS_ASSISTANTDATA_DETAIL, spic_contractobject));

            this_dy.set("org", NotNull(NutNullMsg,"org",check(BOS_ORG_LOGO, org),org));
            this_dy.set("contactperson1st", NotNull(NutNullMsg,"contactperson1st",check(TYPE.DEFAULT, contractperson1st),contractperson1st));
//            this_dy.set("spic_paybank1st", check(TYPE.DEFAULT, spic_paybank1st));
//            this_dy.set("spic_paybankaccount1st", check(TYPE.DEFAULT, spic_paybankaccount1st));
//            this_dy.set("spic_paybanknum1st", check(TYPE.DEFAULT, spic_paybanknum1st));
//            this_dy.set("spic_paybankname1st", check(TYPE.DEFAULT, spic_paybankname1st));
//            this_dy.set("phone1st", check(TYPE.DEFAULT, phone1st));

                this_dy.set("supplier", NotNull(NutNullMsg,"supplier",check(BD_SUPPLIER_LOGO, "number", supplier, org),supplier));
//            this_dy.set("party2nd", check(TYPE.DEFAULT, opposite_name));
            this_dy.set("contactperson2nd", NotNull(NutNullMsg,"contactperson2nd",check(TYPE.DEFAULT, contactperson2nd),contactperson2nd));
//                this_dy.set("spic_paybank2nd", check(TYPE.DEFAULT, ));
//            this_dy.set("spic_paybankaccount2nd", check(TYPE.DEFAULT, opposite_paybankaccount));
            this_dy.set("spic_paybanknum2nd", NotNull(NutNullMsg,"spic_paybanknum2nd",check(TYPE.DEFAULT, spic_paybanknum2nd),spic_paybanknum2nd));
//            this_dy.set("spic_paybankname2nd", check(TYPE.DEFAULT, opposite_paybank));
                this_dy.set("phone2nd", NotNull(NutNullMsg,"phone2nd",check(TYPE.DEFAULT, phone2nd),phone2nd));

            this_dy.set("partc", NotNull(NutNullMsg,"partc",check(BD_BIZPARTNER_LOGO, partc),partc));
//            this_dy.set("spic_contactperson3rd", check(TYPE.DEFAULT, third_contactperson));
//                this_dy.set("spic_paybank3rd", check(TYPE.DEFAULT, spic_paybank3rd));
//            this_dy.set("spic_paybankaccount3rd", check(TYPE.DEFAULT, third_paybankaccount));
//            this_dy.set("spic_paybanknum3rd", check(TYPE.DEFAULT, third_paybanknum));
//            this_dy.set("spic_paybankname3rd", check(TYPE.DEFAULT, third_paybank));
//                this_dy.set("spic_phone3rd", check(TYPE.DEFAULT, spic_phone3rd));

            this_dy.set("settlecurrency",  NotNull(NutNullMsg,"settlecurrency",check(BD_CURRENCY_LOGO, settlecurrency),settlecurrency));
            this_dy.set("totalallamount", NotNull(NutNullMsg,"totalallamount",check(TYPE.AMOUNT, totalallamount),totalallamount));

            this_dy.set(TYPEID_LOGO_ID,NotNull(NutNullMsg,TYPEID_LOGO_ID,check(BD_TYPEID_LOGO, ftypeid),ftypeid));
            this_dy.set(TAXTABLE_LOGO_ID,NotNull(NutNullMsg,TAXTABLE_LOGO_ID,check(BD_TAXTABLE_LOGO, fexratetableid),fexratetableid));
            this_dy.set(CURRENCY_LOGO_ID,NotNull(NutNullMsg,CURRENCY_LOGO_ID,check(BD_CURRENCY_LOGO, fcurrencyid),fcurrencyid));
//            this_dy.set("totalamount", check(TYPE.AMOUNT, totalamount));

//            DynamicObjectCollection payentry_COL = this_dy.getDynamicObjectCollection("payentry");
//            payentry_COL.clear();
//            DynamicObjectType payentry_YTPE = payentry_COL.getDynamicObjectType();
//            for (Element CONTRACTPLAN : CONTRACTPLANS) {
//                DynamicObject payentry_ONE = new DynamicObject(payentry_YTPE);
//                String paydate = CONTRACTPLAN.elementText("DUEDATE"),//付款日期---PAYDATE
//                        payamount = CONTRACTPLAN.elementText("PLANAMOUNT"),//付款金额---PAYAMOUNT
//                        payrate = CONTRACTPLAN.elementText("RECEIPTPAYPLANPERCENT");//付款比例---PAYRATE
//                payentry_ONE.set("paydate", check(TYPE.TIMER, paydate));
//                payentry_ONE.set("payamount", check(TYPE.AMOUNT, payamount));
//                payentry_ONE.set("payrate", check(TYPE.DEFAULT, payrate));
//                payentry_COL.add(payentry_ONE);
//            }
            if (StringUtils.isNotBlank(NutNullMsg)){

            }else {
                dataEntities.add(this_dy);
            }
            // 保存/更新到数据库

        }
        OperateOption option = OperateOption.create();

        option.setVariableValue("currbizappid", ((MainEntityType) type).getAppId());
        option.setVariableValue("ignorewarn", String.valueOf(true));
        option.setVariableValue("ignoreinteraction", String.valueOf(true));
        OperationResult operationResult = OperationServiceHelper.executeOperate("save", CONM_PURCONTRACT_LOGO, dataEntities.toArray(new DynamicObject[dataEntities.size()]), option);
        Map<String, String> errorMsgs = new HashMap<>();
//        List<IOperateInfo> iOperateInfos = operationResult.getAllErrorOrValidateInfo();
//        iOperateInfos.forEach(iOperateInfo -> {
//            iOperateInfo.getMessage()
//        });
        operationResult.getAllErrorOrValidateInfo().forEach(iOperateInfo -> {
            if (errorMsgs.size() == 0) {
                errorMsgs.put(iOperateInfo.getPkValue().toString(), iOperateInfo.getMessage());
            } else {
                errorMsgs.put(iOperateInfo.getPkValue().toString(), errorMsgs.get(iOperateInfo.getPkValue().toString()) + iOperateInfo.getMessage());
            }
        });


        errorMsgs.entrySet().forEach(errorMsg -> {
            resultEntitys.add(ResultEntity.PROCESS_ERROR(errorMsg.getValue()).setInitDate(unique.get(errorMsg.getKey()), "1", null));
        });
        operationResult.getSuccessPkIds().forEach(successMsg -> {
            resultEntitys.add(ResultEntity.SUCCESS().setInitDate(unique.get(successMsg.toString()), "1", null));
        });
        return resultEntitys;
    }

    /**
     * 校验基础资料
     *
     * @param entityNumber
     * @param properite
     * @param value
     * @return
     */
    private Object check(String entityNumber, String properite, String value, String org){
//        return list.get(entityNumber + "@_@" + properite + "@_@" + value + "@_@" + org);
        return list.get(entityNumber + "@_@" + properite + "@_@" + value);
    }

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
        if (kd.bos.dataentity.utils.StringUtils.isBlank(value)) {
            value = "";
        }
        switch (type) {
            case DEFAULT:
                return value;
            case TIMER:
                return "".equals(value)?null:simpleDateFormat.parse(value);
            case BOOBLEN:
                return "1".equals(value) ? true : false;
            case AMOUNT:
                return "".equals(value)?null:new BigDecimal(value);
        }
        return null;
    }

    private Object NotNull(String errorMsg , String field, Object value, String StrValue){
        switch (field){
            case "codeid":
                if (null == value){
                    errorMsg += "code不能为空;";
                }
                break;
            case "contractname":
                if (null == value){
                    errorMsg += "contractname不能为空;";
                }
                break;
            case "contractcode":
                if (null == value){
                    errorMsg += "contractcode不能为空;";
                }
                break;
            case "undertakepersonacction":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "undertakepersonacction未找到当前用户;";
                }
                break;
            case "creationtime":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "creationtime创建时间有误;";
                }
                break;
            case "valuationmode":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "creationtime未找到计价方式;";
                }
                break;
            case "org":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "org未找到采购组织;";
                }
                break;
            case "contractperson1st":
                if (StringUtils.isNotBlank(StrValue) && null == value){

                }
                break;
            case "supplier":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "supplier未找到供应商;";
                }
                break;
            case "contactperson2nd":
                if (StringUtils.isNotBlank(StrValue) && null == value){

                }
                break;
            case "spic_paybanknum2nd":
                if (StringUtils.isNotBlank(StrValue) && null == value){

                }
                break;
            case "phone2nd":
                if (StringUtils.isNotBlank(StrValue) && null == value){

                }
                break;
            case "partc":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "partc未找到第三方;";
                }
                break;
            case "settlecurrency":
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "settlecurrency未找到结算币别;";
                }
                break;
            case "totalallamount":
                if (StringUtils.isNotBlank(StrValue) && null == value){

                }
                break;
            case BD_TYPEID_LOGO:
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "未找到合同类型" + value;
                }
                break;
            case BD_TAXTABLE_LOGO:
                if (StringUtils.isNotBlank(StrValue) && null == value){
                    errorMsg += "未找到汇率表" + value;
                }
                break;
        }
        return value;
    }
}
