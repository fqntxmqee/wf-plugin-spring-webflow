package org.webframe.plugins.webflow;

import org.webframe.plugins.webflow.core.IFlowSupport;
import org.webframe.support.driver.AbstractModulePluginDriver;
import org.webframe.support.driver.ModulePluginManager;


/**
 * TestWebflowModule
 * 
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆</a>
 * @since 2012-5-15 下午4:09:36
 * @version
 */
public class TestWebflowModulePluginDriver extends AbstractModulePluginDriver
			implements IFlowSupport {

	static {
		ModulePluginManager.registerDriver(new TestWebflowModulePluginDriver());
	}

	/* (non-Javadoc)
	 * @see org.webframe.support.driver.ModulePluginDriver#getModuleName()
	 */
	@Override
	public String getModuleName() {
		return "TestWebflowModule";
	}

	@Override
	public String getFlowLocation() {
		return "/flows";
	}
}
