
package org.webframe.plugins.webflow.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.EnterStateVetoException;
import org.springframework.webflow.execution.FlowExecutionListenerAdapter;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.security.SecurityFlowExecutionListener;

/**
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆</a>
 * @since 2012-5-15 下午4:41:41
 * @version
 */
public class SecurityFlowExecutionListenerProxy
			extends
				FlowExecutionListenerAdapter implements InitializingBean {

	private Object	listener	= null;

	protected Log	log		= LogFactory.getLog(getClass());

	@Override
	public void sessionCreating(RequestContext context, FlowDefinition definition) {
		if (listener != null) {
			SecurityFlowExecutionListener securityFlowExecutionListener = convertListener(listener);
			if (securityFlowExecutionListener != null) {
				securityFlowExecutionListener.sessionCreating(context, definition);
			}
		}
	}

	@Override
	public void stateEntering(RequestContext context, StateDefinition state)
				throws EnterStateVetoException {
		if (listener != null) {
			SecurityFlowExecutionListener securityFlowExecutionListener = convertListener(listener);
			if (securityFlowExecutionListener != null) {
				securityFlowExecutionListener.stateEntering(context, state);
			}
		}
	}

	@Override
	public void transitionExecuting(RequestContext context, TransitionDefinition transition) {
		if (listener != null) {
			SecurityFlowExecutionListener securityFlowExecutionListener = convertListener(listener);
			if (securityFlowExecutionListener != null) {
				securityFlowExecutionListener.transitionExecuting(context,
					transition);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			listener = new SecurityFlowExecutionListener();
		} catch (Throwable e) {
			log.debug("Spring Security可能未注入！", e);
		}
	}

	private SecurityFlowExecutionListener convertListener(Object listener) {
		if (listener instanceof SecurityFlowExecutionListener) {
			return (SecurityFlowExecutionListener) listener;
		}
		return null;
	}
}
