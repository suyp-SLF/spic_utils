package kd.cus.webservice.register;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.IUserService;
import kd.bos.permission.model.UserParam;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;

public class Person implements WebserviceMainTemplate {

    private final static String USER_LOGO = "bos_user";// 行政组织对应单据
    private final static String ORG_ADMINORG_LOGO = "bos_adminorg";
    private final static String SOURCE_VALUE = "主数据";

    @Override
    public List<ResultEntity> disposeDate(List<Element> children) throws Exception {
        List<ResultEntity> resultEntitys = new ArrayList<>();
        return save(resultEntitys, children);
    }

    /**
     * 保存
     *
     * @param resultEntitys
     * @param children
     * @return
     */
    private List<ResultEntity> save(List<ResultEntity> resultEntitys, List<Element> children) throws Exception {
        List<UserParam> paramList = new ArrayList<>();
        List<String> codeids = new ArrayList<>();
        List<String> versions = new ArrayList<>();
        children.forEach(child -> {
            String codeid = null, version = null, code = null, name = null, phone = null, email = null,
                    idcard = null, birthday = null, gender = null, dptOrgCode = null, dptDepartmentCode, position = null;
            String[] partjobCodes = null;//, partjobName = null;
            codeid = child.element("CODEID").getText();
            version = child.element("VERSION").getText();
            code = child.element("CODE").getText();
            name = child.element("DESC1").getText();
            phone = child.element("DESC14").getText();
            email = child.element("DESC15").getText();
            idcard = child.element("DESC9").getText();
            birthday = child.element("DESC4").getText();
            gender = child.element("DESC5").getText();
            dptOrgCode = child.element("DESC44").getText();
            position = child.element("DESC21").getText();
            partjobCodes = child.element("DESC53").getText().split(",");
            //partjobName = child.element("DESC53").getText().split(",");

            codeids.add(codeid);
            versions.add(version);

            UserParam user = new UserParam();
            // 判断当前用户是否存在
            QFilter[] userFilters = {new QFilter("number", QCP.equals, code)};
            DynamicObject dy_value = BusinessDataServiceHelper.loadSingle(USER_LOGO, "id", userFilters);
            if (null != dy_value) {
                user.setId(Long.parseLong(dy_value.getPkValue().toString()));
            }
            // user.setCustomId(123456780L);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("number", code);
            dataMap.put("name", name);
            dataMap.put("username", code);
            dataMap.put("usertype", "1");
            dataMap.put("phone", phone);
            dataMap.put("email", StringUtils.isEmpty(email) ? code + "@SPIC.com" : email);
            dataMap.put("idcard", idcard);
            dataMap.put("birthday", birthday);
            dataMap.put("gender", gender);
            dataMap.put("picturefield", "");

            // 职位分录
            List<Map<String, Object>> posList = new ArrayList<>();

            Map<String, Object> entryentity = new HashMap<>();

            if(StringUtils.isNotBlank(dptOrgCode)){
                QFilter[] orgFilters = {new QFilter("number", QCP.equals, dptOrgCode)};
                dy_value = BusinessDataServiceHelper.loadSingle(ORG_ADMINORG_LOGO, "", orgFilters);
                // 设置部门ID
                entryentity.put("dpt", dy_value == null ? null : dy_value.getPkValue());
                entryentity.put("position", StringUtils.isEmpty(position) ? "职员" : position);
                entryentity.put("isincharge", false);
                entryentity.put("ispartjob", false);
                entryentity.put("seq", 1);
                posList.add(entryentity);
            }


            for (String partjobCode : partjobCodes) {
                if (StringUtils.isNotBlank(partjobCode)) {
                    entryentity = new HashMap<>();
                    QFilter[] partjobFilters = {new QFilter("number", QCP.equals, partjobCode)};
                    dy_value = BusinessDataServiceHelper.loadSingle(ORG_ADMINORG_LOGO, "", partjobFilters);
                    // 设置部门ID
                    entryentity.put("dpt", dy_value == null ? null : dy_value.getPkValue());
                    entryentity.put("position", "兼职职员");
                    entryentity.put("isincharge", false);
                    entryentity.put("ispartjob", true);
                    entryentity.put("seq", 1);
                    posList.add(entryentity);
                }
            }
            if(posList.size()>0) {
                dataMap.put("entryentity", posList);
            }
            user.setDataMap(dataMap);
            paramList.add(user);
        });
        // 业务调用可以参照接口名，以下举例说明微服务调用方式
        IUserService userService = (IUserService) ServiceFactory.getService(IUserService.class);
        // 判断执行结果
        userService.addOrUpdate(paramList);
        int i = 0;
        for (UserParam result : paramList) {
            if (!result.isSuccess()) {
                resultEntitys.add(ResultEntity.PROCESS_ERROR(result.getMsg()).setInitDate(codeids.get(i), versions.get(i), SOURCE_VALUE));
            } else {
                resultEntitys.add(ResultEntity.SUCCESS().setInitDate(codeids.get(i), versions.get(i), SOURCE_VALUE));
            }
            i++;
        }
        return resultEntitys;
    }
}
