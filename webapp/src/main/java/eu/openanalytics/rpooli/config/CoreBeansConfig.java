/*
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

import javax.servlet.ServletContext;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import org.eclipse.statet.jcommons.status.StatusException;
import org.eclipse.statet.rj.RjInitFailedException;

import eu.openanalytics.rpooli.ClientSimulator;
import eu.openanalytics.rpooli.RPooliAppEnvironment;
import eu.openanalytics.rpooli.RPooliContext;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.ConfigResource;
import eu.openanalytics.rpooli.api.NodesResource;
import eu.openanalytics.rpooli.api.PoolResource;

@Configuration
@ComponentScan("eu.openanalytics.rpooli")
public class CoreBeansConfig {
	
	@Autowired
	ServletContext servletContext;
	
	@Bean(destroyMethod = "dispose")
	public RPooliAppEnvironment environment() {
        try
        {
            return new RPooliAppEnvironment();
        } catch (StatusException e)
        {
            throw new BeanCreationException(e.getMessage(), e);
        }
	}
	
	@Bean
	@DependsOn("environment")
	public RPooliContext context() {
		try {
			RPooliContext context = new RPooliContext(servletContext);
			return context;
		} catch (RjInitFailedException e) {
			throw new BeanCreationException(e.getMessage(), e);
		}
	}
	
	@Bean(destroyMethod = "dispose")
	public RPooliServer server() {
		RPooliServer server = RPooliServer.create(servletContext, context());
		return server;
	}
	
	@Bean
	public ClientSimulator clientSimulator() {
		ClientSimulator clientSimulator = new ClientSimulator(server());
		return clientSimulator;
	}
	
	@Bean
	public NodesResource nodesResource() {
		NodesResource nodesResource = new NodesResource(server(), clientSimulator());
		return nodesResource;
	}
	
	@Bean
	public PoolResource poolResource() {
		PoolResource poolResource = new PoolResource(server());
		return poolResource;
	}
	
	@Bean
	public ConfigResource configResource() {
		ConfigResource configResource = new ConfigResource(server());
		return configResource;
	}
}
