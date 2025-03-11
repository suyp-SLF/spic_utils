package kd.cus.api;

import kd.bos.dataentity.utils.StringUtils;

/**
 * 解析executeOperation执行结果的返回信息工具类
 *
 * @author Wu Yanqi
 */
public class ParseResultMessageUtil {

    public static String parse(String message, String defaultValue){
        if (StringUtils.isBlank(message) || !message.contains(":")){
            return defaultValue;
        }
        int index = message.indexOf(":");
        return StringUtils.defaultIfBlank(message.substring(0, index), defaultValue);
    }
}
