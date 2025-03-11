package kd.cus.conmWebservice.action;

/**
 * 接收信息（当前废弃）1
 * @author suyp
 *
 */
public interface WebserviceInterface {
	String receive(String p_interface_type,
                   String p_source,
                   String p_user_name,
                   String p_password,
                   String p_data_id,
                   String p_content);
}
