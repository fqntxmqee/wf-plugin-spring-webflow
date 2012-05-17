
package org.webframe.plugins.webflow.core;

/**
 * webflow xml定位接口，需要使用webflow模块，需实现该接口；
 * 在location文件下的webflow需要一定规则配置：location根目录下是各个webflow名称（flowId）的文件夹， 该文件夹下的webflow
 * xml配置文件命名格式为'wf-flow-*.xml'，例如：'/flows/shoping/wf-flow-shoping.xml'，'/flows'为location
 * 
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆</a>
 * @since 2012-5-15 下午1:50:26
 * @version
 */
public interface IFlowSupport {

	public static final String	FLOW_FILE_PATTERN	= "/**/wf-flow-*.xml";

	/**
	 * 获取webflow配置文件的跟文件位置，例如：webflow配置文件： '/flows/shoping/wf-flow-shoping.xml'，location为：'/flows'
	 * 
	 * @return
	 * @author 黄国庆 2012-5-15 下午1:51:36
	 */
	String getFlowLocation();
}
