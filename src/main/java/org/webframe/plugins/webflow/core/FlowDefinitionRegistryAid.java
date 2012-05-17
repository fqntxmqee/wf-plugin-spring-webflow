
package org.webframe.plugins.webflow.core;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.webflow.config.FlowDefinitionResource;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.registry.FlowDefinitionHolder;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.DefaultFlowHolder;
import org.springframework.webflow.engine.builder.FlowAssembler;
import org.springframework.webflow.engine.builder.FlowBuilder;
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.builder.model.FlowModelFlowBuilder;
import org.springframework.webflow.engine.builder.support.FlowBuilderContextImpl;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.model.builder.DefaultFlowModelHolder;
import org.springframework.webflow.engine.model.builder.FlowModelBuilder;
import org.springframework.webflow.engine.model.builder.xml.XmlFlowModelBuilder;
import org.springframework.webflow.engine.model.registry.FlowModelHolder;
import org.springframework.webflow.engine.model.registry.FlowModelRegistry;
import org.webframe.core.util.ReflectionUtils;
import org.webframe.support.driver.ModulePluginUtils;
import org.webframe.support.driver.resource.jar.JarResourceLoader.JarResource;
import org.webframe.support.driver.resource.jar.JarResourcePatternResolver;
import org.webframe.support.util.StringUtils;

/**
 * FlowDefinitionRegistry辅助类，用于加载jar中的webflow配置文件xml，再注册到已注入的flowRegistry中；
 * 通过模块插件工具类ModulePluginUtils，获取实现了IFlowSupport接口的实现类，从而获得webflow配置文件location；
 * 再通过JarResourcePatternResolver获取符合条件的Resources
 * 
 * @author <a href="mailto:guoqing.huang@foxmail.com">黄国庆</a>
 * @since 2012-5-15 下午3:14:36
 * @version
 */
public class FlowDefinitionRegistryAid implements InitializingBean {

	private FlowDefinitionRegistry	flowRegistry			= null;

	private FlowModelRegistry			flowModelRegistry		= null;

	private FlowBuilderServices		flowBuilderServices	= null;

	protected Log							log						= LogFactory.getLog(getClass());

	protected final String				SLASH						= "/";

	public void setFlowRegistry(FlowDefinitionRegistry flowRegistry) {
		this.flowRegistry = flowRegistry;
	}

	public FlowDefinitionRegistry getFlowRegistry() {
		return flowRegistry;
	}

	public void setFlowBuilderServices(FlowBuilderServices flowBuilderServices) {
		this.flowBuilderServices = flowBuilderServices;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(flowRegistry, "FlowDefinitionRegistry object is must!");
		Assert.notNull(flowBuilderServices, "FlowBuilderServices object is must!");
		flowModelRegistry = (FlowModelRegistry) ReflectionUtils.getFieldValue(
			flowRegistry, "flowModelRegistry");
		Assert.notNull(flowModelRegistry, "FlowModelRegistry object is must!");
		registerFlowDefinition();
	}

	/**
	 * 注册FlowDefinition；通过查询模块插件接口IFlowSupport的实现类，遍历所有实现类的getFlowLocation()方法，获取locations，
	 * 通过JarResourcePatternResolver获取Resources，生成FlowDefinitionResource对象，并注册。
	 * 
	 * @author 黄国庆 2012-5-15 下午4:04:04
	 */
	protected void registerFlowDefinition() {
		Enumeration<IFlowSupport> flowDrivers = ModulePluginUtils.getDrivers(IFlowSupport.class);
		while (flowDrivers.hasMoreElements()) {
			IFlowSupport flowSupport = flowDrivers.nextElement();
			String basePath = flowSupport.getFlowLocation();
			if (StringUtils.hasText(basePath)) {
				basePath = StringUtils.cleanPath(basePath);
				if (basePath.endsWith(SLASH)) {
					basePath = basePath.substring(0, basePath.length() - 1);
				}
				String location = basePath + IFlowSupport.FLOW_FILE_PATTERN;
				JarResourcePatternResolver resolver = JarResourcePatternResolver.getJarResourcePatternResolver(flowSupport.getClass());
				if (resolver != null) {
					try {
						Resource[] resouces = resolver.getResources(location);
						for (Resource resource : resouces) {
							if (resource.exists()) {
								FlowDefinitionResource flowDefinitionResource = new FlowDefinitionResource(getFlowId(
									resource, basePath), resource, getFlowAttributes());
								flowRegistry.registerFlowDefinition(createFlowDefinitionHolder(flowDefinitionResource));
							}
						}
					} catch (IOException e) {
						log.error("load webflow xml error: ", e);
					}
				}
			}
		}
	}

	protected String getFlowId(Resource flowResource, String basePath) {
		String filePath;
		if (flowResource instanceof ClassPathResource) {
			filePath = ((ClassPathResource) flowResource).getPath();
		} else if (flowResource instanceof FileSystemResource) {
			filePath = truncateFilePath(
				((FileSystemResource) flowResource).getPath(), basePath);
		} else if (flowResource instanceof JarResource) {
			filePath = SLASH + ((JarResource) flowResource).getEntryName();
		} else {
			// default to the filename
			return getFlowIdFromFileName(flowResource);
		}
		int beginIndex = 0;
		int endIndex = filePath.length();
		if (filePath.startsWith(basePath)) {
			beginIndex = basePath.length();
		} else if (filePath.startsWith(SLASH + basePath)) {
			beginIndex = basePath.length() + 1;
		}
		if (filePath.startsWith(SLASH, beginIndex)) {
			// ignore a leading slash
			beginIndex++;
		}
		if (filePath.lastIndexOf(SLASH) >= beginIndex) {
			// ignore the filename
			endIndex = filePath.lastIndexOf(SLASH);
		} else {
			// there is no path info, default to the filename
			return getFlowIdFromFileName(flowResource);
		}
		return filePath.substring(beginIndex, endIndex);
	}

	protected String truncateFilePath(String filePath, String basePath) {
		int basePathIndex = filePath.lastIndexOf(basePath);
		if (basePathIndex != -1) {
			return filePath.substring(basePathIndex);
		} else {
			return filePath;
		}
	}

	protected String getFlowIdFromFileName(Resource flowResource) {
		return StringUtils.stripFilenameExtension(flowResource.getFilename());
	}

	protected AttributeMap getFlowAttributes() {
		MutableAttributeMap flowAttributes = null;
		if (flowBuilderServices.getDevelopment()) {
			flowAttributes = new LocalAttributeMap(1, 1);
			flowAttributes.put("development", Boolean.TRUE);
		}
		return flowAttributes;
	}

	protected FlowDefinitionHolder createFlowDefinitionHolder(FlowDefinitionResource flowResource) {
		FlowBuilder builder = createFlowBuilder(flowResource);
		FlowBuilderContext builderContext = new FlowBuilderContextImpl(flowResource.getId(), flowResource.getAttributes(), flowRegistry, flowBuilderServices);
		FlowAssembler assembler = new FlowAssembler(builder, builderContext);
		return new DefaultFlowHolder(assembler);
	}

	protected FlowBuilder createFlowBuilder(FlowDefinitionResource resource) {
		return new FlowModelFlowBuilder(createFlowModelHolder(resource));
	}

	protected FlowModelHolder createFlowModelHolder(FlowDefinitionResource resource) {
		FlowModelHolder modelHolder = new DefaultFlowModelHolder(createFlowModelBuilder(resource));
		flowModelRegistry.registerFlowModel(resource.getId(), modelHolder);
		return modelHolder;
	}

	protected FlowModelBuilder createFlowModelBuilder(FlowDefinitionResource resource) {
		return new XmlFlowModelBuilder(resource.getPath(), flowModelRegistry);
	}
}
