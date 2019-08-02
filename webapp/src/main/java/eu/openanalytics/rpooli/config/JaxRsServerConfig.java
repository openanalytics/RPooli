/**
 * This file is part of RPooli.
 *
 * RPooli is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with RPooli.  If not, see <http://www.apache.org/licenses/>.
 */
package eu.openanalytics.rpooli.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.spring.JAXRSServerFactoryBeanDefinitionParser.SpringJAXRSServerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import eu.openanalytics.rpooli.api.ConfigResource;
import eu.openanalytics.rpooli.api.NodesResource;
import eu.openanalytics.rpooli.api.PoolResource;
import eu.openanalytics.rpooli.api.ThrowableExceptionMapper;

@Configuration
@ComponentScan("eu.openanalytics.rpooli")
@ImportResource(value= {"classpath:META-INF/cxf/cxf.xml", 
						"classpath:META-INF/cxf/cxf-servlet.xml"})
@Import(CoreBeansConfig.class)
public class JaxRsServerConfig {
	
	@Autowired
	NodesResource nodesResource;
	
	@Autowired
	PoolResource poolResource;
	
	@Autowired
	ConfigResource configResource;
	
	@Bean
	public SpringBus cxf() {
		return new SpringBus();
	}
	
	@Bean
	public Server jaxRsServer() {
		SpringJAXRSServerFactoryBean bean = new SpringJAXRSServerFactoryBean();
		bean.setAddress("/v1/");
		
		List<Object> serviceBeans = new ArrayList<>();
		serviceBeans.add(nodesResource);
		serviceBeans.add(poolResource);
		serviceBeans.add(configResource);
		bean.setServiceBeans(serviceBeans);
		
		List<Class<?>> providers = new ArrayList<>();
		providers.add(JacksonJsonProvider.class);
		providers.add(ThrowableExceptionMapper.class);
		
		bean.setProviders(providers);
		Server server = bean.create();
		return server;
	}
}
