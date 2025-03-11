package kd.cus.treasuryWebservice.action.entity;

/**
 * webservice传递参数实体类
 * @author suyp
 *
 */
public class MsgEntity {
	private String idCode;
	private String infCode;
	private String xmlData;
	
	public MsgEntity(String idCode, String infCode, String xmlData) {
		this.idCode = idCode;
		this.infCode = infCode;
		this.xmlData = xmlData;
	}
	
	public String getIdCode() {
		return idCode;
	}
	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}
	public String getInfCode() {
		return infCode;
	}
	public void setInfCode(String infCode) {
		this.infCode = infCode;
	}
	public String getXmlData() {
		return xmlData;
	}
	public void setXmlData(String xmlData) {
		this.xmlData = xmlData;
	}
	
	@Override
		public String toString() {
			String classInfo = "idCode:" + this.idCode + "\r\n"
					+ "infCode:" + this.infCode + "\r\n"
					+ "xmlData" + this.xmlData + "\r\n";
			return classInfo;
		}
}
