
package org.webframe.plugins.webflow.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;

/**
 * MvcViewFactoryCreator辅组工具类，主要通过ApplicationContext获取所有已注册的ViewResolver集合
 * 
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆</a>
 * @since 2012-5-15 下午4:30:48
 * @version
 */
public class MvcViewFactoryCreatorAid extends MvcViewFactoryCreator implements
			InitializingBean {

	private ApplicationContext	ac					= null;

	private List<ViewResolver>	viewResolvers	= null;

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, ViewResolver> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(
			ac, ViewResolver.class, true, false);
		if (!matchingBeans.isEmpty()) {
			this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
			OrderComparator.sort(this.viewResolvers);
			setViewResolvers(viewResolvers);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		ac = applicationContext;
		super.setApplicationContext(applicationContext);
	}
}
