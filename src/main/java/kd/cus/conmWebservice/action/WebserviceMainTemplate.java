package kd.cus.conmWebservice.action;

import kd.cus.conmWebservice.action.entity.ResultEntity;
import org.dom4j.Element;

import java.util.List;


/**
 * 对外开放函数，自定义接口
 * @author suyp
 *
 */
public interface WebserviceMainTemplate  {
	List<ResultEntity> disposeDate(Element children, String p_source) throws Exception;
}
