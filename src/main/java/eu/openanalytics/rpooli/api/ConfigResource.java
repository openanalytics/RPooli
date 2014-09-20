
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_AND_SAVE;
import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.RServiNodeConfig;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.RPooliServer.ConfigAction;
import eu.openanalytics.rpooli.api.spec.model.ConfPoolJson;
import eu.openanalytics.rpooli.api.spec.model.ConfRJson;
import eu.openanalytics.rpooli.api.spec.model.EnvironmentVariable;
import eu.openanalytics.rpooli.api.spec.resource.Config;

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
        return GetConfigRDefaultResponse.jsonOK(buildRConfig(server.getDefaultRConfig()));
    }

    @Override
    public GetConfigRResponse getConfigR() throws Exception
    {
        return GetConfigRResponse.jsonOK(buildRConfig(server.getCurrentRConfig()));
    }

    @Override
    public void putConfigR(final boolean save, final ConfRJson config) throws Exception
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
        return GetConfigPoolResponse.jsonOK(buildPoolConfig(server.getCurrentPoolConfig()));
    }

    @Override
    public GetConfigPoolDefaultResponse getConfigPoolDefault() throws Exception
    {
        return GetConfigPoolDefaultResponse.jsonOK(buildPoolConfig(server.getDefaultPoolConfig()));
    }

    @Override
    public void putConfigPool(final boolean save, final ConfPoolJson config) throws Exception
    {
        final PoolConfig poolConfig = new PoolConfig();
        poolConfig.setEvictionTimeout(config.getNodeEvitionTimeoutMillis());
        poolConfig.setMaxIdleCount(config.getMaxIdleNodes().intValue());
        poolConfig.setMaxTotalCount(config.getMaxTotalNodes().intValue());
        poolConfig.setMaxUsageCount(config.getMaxNodeReuse().intValue());
        poolConfig.setMaxWaitTime(config.getMaxWaitTimeMillis());
        poolConfig.setMinIdleCount(config.getMinIdleNodes().intValue());
        poolConfig.setMinIdleTime(config.getMinNodeIdleTimeMillis());

        server.setConfiguration(poolConfig, asAction(save));
    }

    private ConfPoolJson buildPoolConfig(final PoolConfig config)
    {
        return new ConfPoolJson().withMaxIdleNodes((long) config.getMaxIdleCount())
            .withMaxNodeReuse((long) config.getMaxUsageCount())
            .withMaxTotalNodes((long) config.getMaxTotalCount())
            .withMaxWaitTimeMillis(config.getMaxWaitTime())
            .withMinIdleNodes((long) config.getMinIdleCount())
            .withMinNodeIdleTimeMillis(config.getMinIdleTime())
            .withNodeEvitionTimeoutMillis(config.getEvictionTimeout());
    }

    private ConfigAction asAction(final boolean save)
    {
        return save ? APPLY_AND_SAVE : APPLY_ONLY;
    }
}
