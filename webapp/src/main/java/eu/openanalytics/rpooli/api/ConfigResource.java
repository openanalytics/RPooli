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
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_AND_SAVE;
import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
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
        return GetConfigRDefaultResponse.withJsonOK(buildRConfig(server.getDefaultRConfig()));
    }

    @Override
    public GetConfigRResponse getConfigR() throws Exception
    {
        return GetConfigRResponse.withJsonOK(buildRConfig(server.getCurrentRConfig()));
    }

    @Override
    public PutConfigRResponse putConfigR(final boolean save, final ConfRJson config) throws Exception
    {
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
        rConfig.setStartStopTimeout(config.getStartStopTimeout());

        for (final EnvironmentVariable var : config.getEnvironmentVariables())
        {
            rConfig.getEnvironmentVariables().put(var.getName(), var.getValue());
        }

        server.setConfiguration(rConfig, asAction(save));

        return PutConfigRResponse.withNoContent();
    }

    private ConfRJson buildRConfig(final RServiNodeConfig config)
    {
        final List<EnvironmentVariable> environmentVariables = new ArrayList<>();
        for (final Entry<String, String> var : config.getEnvironmentVariables().entrySet())
        {
            environmentVariables.add(new EnvironmentVariable().withName(var.getKey()).withValue(
                var.getValue()));
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
            .withStartStopTimeout(config.getStartStopTimeout())
            .withStartupSnippet(config.getRStartupSnippet())
            .withWorkingDirectory(config.getBaseWorkingDirectory());
    }

    //
    // Pool Configuration
    //

    @Override
    public GetConfigPoolResponse getConfigPool() throws Exception
    {
        return GetConfigPoolResponse.withJsonOK(buildPoolConfig(server.getCurrentPoolConfig()));
    }

    @Override
    public GetConfigPoolDefaultResponse getConfigPoolDefault() throws Exception
    {
        return GetConfigPoolDefaultResponse.withJsonOK(buildPoolConfig(server.getDefaultPoolConfig()));
    }

    @Override
    public PutConfigPoolResponse putConfigPool(final boolean save, final ConfPoolJson config)
        throws Exception
    {
        final PoolConfig poolConfig = new PoolConfig();
        poolConfig.setEvictionTimeout(config.getNodeEvictionTimeoutMillis());
        poolConfig.setMaxIdleCount(config.getMaxIdleNodes().intValue());
        poolConfig.setMaxTotalCount(config.getMaxTotalNodes().intValue());
        poolConfig.setMaxUsageCount(config.getMaxNodeReuse().intValue());
        poolConfig.setMaxWaitTime(config.getMaxWaitTimeMillis());
        poolConfig.setMinIdleCount(config.getMinIdleNodes().intValue());
        poolConfig.setMinIdleTime(config.getMinNodeIdleTimeMillis());

        server.setConfiguration(poolConfig, asAction(save));

        return PutConfigPoolResponse.withNoContent();
    }

    private ConfPoolJson buildPoolConfig(final PoolConfig config)
    {
        return new ConfPoolJson().withMaxIdleNodes((long) config.getMaxIdleCount())
            .withMaxNodeReuse((long) config.getMaxUsageCount())
            .withMaxTotalNodes((long) config.getMaxTotalCount())
            .withMaxWaitTimeMillis(config.getMaxWaitTime())
            .withMinIdleNodes((long) config.getMinIdleCount())
            .withMinNodeIdleTimeMillis(config.getMinIdleTime())
            .withNodeEvictionTimeoutMillis(config.getEvictionTimeout());
    }

    //
    // Net Configuration
    //

    @Override
    public GetConfigNetResponse getConfigNet() throws Exception
    {
        return GetConfigNetResponse.withJsonOK(buildNetConfig(server.getCurrentNetConfig()));
    }

    @Override
    public GetConfigNetDefaultResponse getConfigNetDefault() throws Exception
    {
        return GetConfigNetDefaultResponse.withJsonOK(buildNetConfig(server.getDefaultNetConfig()));
    }

    @Override
    public PutConfigNetResponse putConfigNet(final boolean save, final ConfNetResolvedJsonParent config)
        throws Exception
    {
        final NetConfig netConfig = new NetConfig();
        netConfig.setHostAddress(config.getHost());
        netConfig.setRegistryEmbed(config.getStartEmbeddedRegistry());
        netConfig.setRegistryPort(config.getPort().intValue());
        netConfig.setSSLEnabled(config.getEnabledSsl());

        server.setConfiguration(netConfig, asAction(save));

        return PutConfigNetResponse.withNoContent();
    }

    private ConfNetResolvedJson buildNetConfig(final NetConfig config)
    {
        return (ConfNetResolvedJson) new ConfNetResolvedJson().withEffectiveHost(
            config.getEffectiveHostAddress())
            .withEffectivePort((long) config.getEffectiveRegistryPort())
            .withEnabledSsl(config.isSSLEnabled())
            .withHost(config.getHostAddress())
            .withPort((long) config.getRegistryPort())
            .withStartEmbeddedRegistry(config.getRegistryEmbed());
    }

    private ConfigAction asAction(final boolean save)
    {
        return save ? APPLY_AND_SAVE : APPLY_ONLY;
    }
}
