package kd.cus.webservice.action;

import java.util.List;

import org.dom4j.Element;

import kd.cus.webservice.action.entity.ResultEntity;


/**
 * 对外开放函数，自定义接口
 * @author suyp
 *
 */
public interface WebserviceMainTemplate  {
	List<ResultEntity> disposeDate(List<Element> children) throws Exception;
}
