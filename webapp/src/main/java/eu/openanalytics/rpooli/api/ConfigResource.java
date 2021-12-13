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

package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_AND_SAVE;
import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_ONLY;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.statet.jcommons.lang.NonNull;
import org.eclipse.statet.jcommons.lang.Nullable;

import org.eclipse.statet.rj.servi.node.RServiNodeConfig;
import org.eclipse.statet.rj.servi.pool.NetConfig;
import org.eclipse.statet.rj.servi.pool.PoolConfig;

import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.RPooliServer.ConfigAction;
import eu.openanalytics.rpooli.api.spec.model.ConfNetResolvedJson;
import eu.openanalytics.rpooli.api.spec.model.ConfNetResolvedJsonParent;
import eu.openanalytics.rpooli.api.spec.model.ConfPoolJson;
import eu.openanalytics.rpooli.api.spec.model.ConfRJson;
import eu.openanalytics.rpooli.api.spec.model.EnvironmentVariable;
import eu.openanalytics.rpooli.api.spec.resource.Config;

/**
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class ConfigResource extends AbstractRPooliServerAware implements Config
{
    public ConfigResource(final RPooliServer server)
    {
        super(server);
    }

    //
    // R Configuration
    //

    @Override
    public GetConfigRDefaultResponse getConfigRDefault() throws Exception
    {
        return GetConfigRDefaultResponse.withJsonOK(buildRConfig(this.server.getDefaultRConfig()));
    }

    @Override
    public GetConfigRResponse getConfigR() throws Exception
    {
        return GetConfigRResponse.withJsonOK(buildRConfig(this.server.getCurrentRConfig()));
    }

	@Override
	public PutConfigRResponse putConfigR(final boolean save, final ConfRJson config)
			throws Exception {
		final RServiNodeConfig rConfig = new RServiNodeConfig();
		rConfig.setBaseWorkingDirectory(config.getWorkingDirectory());
		rConfig.setEnableConsole(config.getEnableDebugConsole());
		rConfig.setEnableVerbose(config.getEnableVerboseLogging());
		rConfig.setJavaArgs(config.getJavaArguments());
		rConfig.setJavaHome(config.getJavaHome());
		rConfig.setNodeArgs(config.getNodeArguments());
		rConfig.setRArch(config.getRArchitecture());
		rConfig.setRHome(config.getRHome());
		rConfig.setRStartupSnippet(config.getStartupSnippet());
		rConfig.setStartStopTimeout(toStartStopTimeout(config));
		
		for (final EnvironmentVariable var : config.getEnvironmentVariables()) {
			rConfig.getEnvironmentVariables().put(var.getName(), var.getValue());
		}
		
		this.server.setConfiguration(rConfig, asAction(save));
		
		return PutConfigRResponse.withNoContent();
	}
	
	private ConfRJson buildRConfig(final RServiNodeConfig config) {
		final List<EnvironmentVariable> environmentVariables = new ArrayList<>();
		for (final Entry<String, String> var : config.getEnvironmentVariables().entrySet()) {
			environmentVariables.add(new EnvironmentVariable()
					.withName(var.getKey())
					.withValue(var.getValue()));
		}
		
		return new ConfRJson().withEnableDebugConsole(config.getEnableConsole())
				.withEnableVerboseLogging(config.getEnableVerbose())
				.withEnvironmentVariables(environmentVariables)
				.withJavaArguments(config.getJavaArgs())
				.withJavaHome(config.getJavaHome())
				.withNodeArguments(config.getNodeArgs())
				.withRArchitecture(config.getRArch())
				.withRHome(config.getRHome())
				.withRLibraries(config.getEnvironmentVariables().get("R_LIBS"))
				.withStartStopTimeout(toStartStopTimeout(config))
				.withStartupSnippet(config.getRStartupSnippet())
				.withWorkingDirectory(config.getBaseWorkingDirectory());
	}
	
    //
    // Pool Configuration
    //

    @Override
    public GetConfigPoolResponse getConfigPool() throws Exception
    {
        return GetConfigPoolResponse.withJsonOK(buildPoolConfig(this.server.getCurrentPoolConfig()));
    }

    @Override
    public GetConfigPoolDefaultResponse getConfigPoolDefault() throws Exception
    {
        return GetConfigPoolDefaultResponse.withJsonOK(buildPoolConfig(this.server.getDefaultPoolConfig()));
    }

	@Override
	public PutConfigPoolResponse putConfigPool(final boolean save, final ConfPoolJson config)
			throws Exception {
		final PoolConfig poolConfig = new PoolConfig();
		poolConfig.setEvictionTimeout(toEvictionTimeout(config));
		poolConfig.setMaxIdleCount(config.getMaxIdleNodes().intValue());
		poolConfig.setMaxTotalCount(config.getMaxTotalNodes().intValue());
		poolConfig.setMaxUsageCount(config.getMaxNodeReuse().intValue());
		poolConfig.setAllocationTimeout(toAllocationTimeout(config));
		poolConfig.setMinIdleCount(config.getMinIdleNodes().intValue());
		poolConfig.setAutoEvictionMinIdleTime(toAutoEvictionMinIdleTime(config));
		
		this.server.setConfiguration(poolConfig, asAction(save));
		
		return PutConfigPoolResponse.withNoContent();
	}
	
	private ConfPoolJson buildPoolConfig(final PoolConfig config) {
		return new ConfPoolJson().withMaxIdleNodes((long) config.getMaxIdleCount())
				.withMaxNodeReuse((long) config.getMaxUsageCount())
				.withMaxTotalNodes((long) config.getMaxTotalCount())
				.withMaxWaitTimeMillis(toMaxWaitTime(config))
				.withMinIdleNodes((long) config.getMinIdleCount())
				.withMinNodeIdleTimeMillis(toMinNodeIdleTime(config))
				.withNodeEvictionTimeoutMillis(toNodeEvictionTimeoutMillis(config));
	}
	
    //
    // Net Configuration
    //

    @Override
    public GetConfigNetResponse getConfigNet() throws Exception
    {
        return GetConfigNetResponse.withJsonOK(buildNetConfig(this.server.getCurrentNetConfig()));
    }

    @Override
    public GetConfigNetDefaultResponse getConfigNetDefault() throws Exception
    {
        return GetConfigNetDefaultResponse.withJsonOK(buildNetConfig(this.server.getDefaultNetConfig()));
    }

	@Override
	public PutConfigNetResponse putConfigNet(final boolean save, final ConfNetResolvedJsonParent config)
			throws Exception {
		final NetConfig netConfig = new NetConfig();
		netConfig.setHostAddress(config.getHost());
		netConfig.setRegistryEmbed(config.getStartEmbeddedRegistry());
		netConfig.setRegistryPort(config.getPort().intValue());
		netConfig.setSSLEnabled(config.getEnabledSsl());
		
		this.server.setConfiguration(netConfig, asAction(save));
		
		return PutConfigNetResponse.withNoContent();
	}
	
	private ConfNetResolvedJson buildNetConfig(final NetConfig config) {
		return (ConfNetResolvedJson) new ConfNetResolvedJson()
				.withEffectiveHost(config.getEffectiveHostAddress())
				.withEffectivePort((long) config.getEffectiveRegistryPort())
				.withEnabledSsl(config.isSSLEnabled())
				.withHost(config.getHostAddress())
				.withPort((long) config.getRegistryPort())
				.withStartEmbeddedRegistry(config.getRegistryEmbed());
	}
	
	
	private static ConfigAction asAction(final boolean save) {
		return save ? APPLY_AND_SAVE : APPLY_ONLY;
	}
	
	
	private static long toNodeEvictionTimeoutMillis(final PoolConfig config) {
		final var duration= config.getEvictionTimeout();
		return duration.toMillis();
	}
	
	private static @NonNull Duration toEvictionTimeout(final ConfPoolJson config) {
		final var millis= (long)config.getNodeEvictionTimeoutMillis();
		return Duration.ofMillis(millis);
	}
	
	private static long toMinNodeIdleTime(final PoolConfig config) {
		final var duration= config.getAutoEvictionMinIdleTime();
		return duration.toMillis();
	}
	
	private static @NonNull Duration toAutoEvictionMinIdleTime(final ConfPoolJson config) {
		final var millis= (long)config.getMinNodeIdleTimeMillis();
		return Duration.ofMillis(millis);
	}
	
	private static long toMaxWaitTime(final PoolConfig config) {
		final var duration= config.getAllocationTimeout();
		return (duration != null) ? duration.toMillis() : -1;
	}
	
	private static @Nullable Duration toAllocationTimeout(final ConfPoolJson config) {
		final var millis= (long)config.getMaxWaitTimeMillis();
		return (millis >= 0) ? Duration.ofMillis(millis) : null;
	}
	
	private static long toStartStopTimeout(final RServiNodeConfig config) {
		final var duration= config.getStartStopTimeout();
		return (duration != null) ? duration.toMillis() : -1;
	}
	
	private static @Nullable Duration toStartStopTimeout(final ConfRJson config) {
		final var millis= (long)config.getStartStopTimeout();
		return (millis >= 0) ? Duration.ofMillis(millis) : null;
	}
	
}
