package kd.cus.webservice.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.org.api.IOrgService;
import kd.bos.org.model.OrgParam;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.service.ServiceFactory;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.cus.webservice.action.WebserviceMainTemplate;
import kd.cus.webservice.action.entity.ResultEntity;

/**
 * 部门
 * 
 * @author suyp
 *
 */

public class Department implements WebserviceMainTemplate {

	private final static String ORG_ADMINORG_LOGO = "bos_adminorg";
	private final static String SOURCE_VALUE = "主数据";

	/* 
	 * 重写disposeDate方法用于解析xml文件以及对xml文件进行相关参数梳理 (non-Javadoc)
	 * 
	 * @see kd.cus.ws.main.WsMainTemplate#disposeDate(java.lang.String)
	 */
	@Override
	public List<ResultEntity> disposeDate(List<Element> children) throws Exception {
		List<ResultEntity> resultEntitys = new ArrayList<>();
		return save(resultEntitys, children);
	}
	
	/**
	 * 保存
	 * @param resultEntitys
	 * @param children
	 * @return
	 */
	private List<ResultEntity> save(List<ResultEntity> resultEntitys, List<Element> children) throws Exception {
		List<OrgParam> paramList = new ArrayList<>();
		List<String> codeids = new ArrayList<>();
		List<String> versions = new ArrayList<>();
		children.forEach(child->{
			String codeid = null, version = null, code = null, name = null, parentOrgCode = null, parentDepartmentCode = null, partnerid = null;
			codeid = child.element("CODEID").getText();
			version = child.element("VERSION").getText();
			code = child.element("CODE").getText();
			name = child.element("DESC2").getText();
			parentOrgCode = child.element("DESC5").getText();
			parentDepartmentCode = child.element("DESC15").getText();
			partnerid = child.element("DESC11").getText();
		
			codeids.add(codeid);
			versions.add(version);
			// 获得根节点id
			long rootOrgId = OrgUnitServiceHelper.getRootOrgId();

			// 判断该code是否已经注册到苍穹平台
			QFilter[] this_Filters = { new QFilter("number", QCP.equals, code) };
			DynamicObject this_dy_value = BusinessDataServiceHelper.loadSingle(ORG_ADMINORG_LOGO, "id", this_Filters);
			/* 新增单个视图方案的组织 */
			OrgParam param = new OrgParam();
			// 判断
			if (null != this_dy_value) {
				param.setId(Long.parseLong(this_dy_value.getPkValue().toString()));
			}
			// 判断父组织是否存在
            String parentCode = "100000";
			if (StringUtils.isNotBlank(parentDepartmentCode)) {
                parentCode = parentDepartmentCode;
			} else {
                parentCode = parentOrgCode;
			}
            QFilter[] qFilters = { new QFilter("number", QCP.equals, parentCode)};
            DynamicObject dy_value = BusinessDataServiceHelper.loadSingle(ORG_ADMINORG_LOGO, "id", qFilters);
            if (null != dy_value) {
                param.setParentId(Long.parseLong(dy_value.getPkValue().toString()));
            }

			param.setName(name);
			param.setNumber(code);
			param.setOrgPatternId(4);
			param.setDuty(OrgViewType.Admin);
			// 设置组织属性
			Map<String, Object> proMap = new HashMap<>();
			proMap.put("spic_partnerid", partnerid);
			param.setPropertyMap(proMap);
			// 业务调用可以参照接口名，以下举例说明微服务调用方式
			paramList.add(param);
		});
		IOrgService orgService = ServiceFactory.getService(IOrgService.class);
		orgService.addOrUpdate(paramList);
		// 判断执行结果
		int i = 0;
		for (OrgParam result : paramList) {
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
