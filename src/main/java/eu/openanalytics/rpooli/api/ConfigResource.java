
package eu.openanalytics.rpooli.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import de.walware.rj.servi.pool.RServiNodeConfig;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.ConfRJson;
import eu.openanalytics.rpooli.api.spec.model.EnvironmentVariable;
import eu.openanalytics.rpooli.api.spec.resource.Config;

public class ConfigResource extends AbstractRPooliServerAware implements Config
{
    public ConfigResource(final RPooliServer server)
    {
        super(server);
    }

    @Override
    public GetConfigRDefaultResponse getConfigRDefault() throws Exception
    {
        return GetConfigRDefaultResponse.jsonOK(buildRConfig(server.getDefaultConfig()));
    }

    @Override
    public GetConfigRResponse getConfigR() throws Exception
    {
        return GetConfigRResponse.jsonOK(buildRConfig(server.getCurrentConfig()));
    }

    @Override
    public void putConfigR(final boolean save, final ConfRJson config) throws Exception
    {
        final RServiNodeConfig nodeConfig = new RServiNodeConfig();
        nodeConfig.setBaseWorkingDirectory(config.getWorkingDirectory());
        nodeConfig.setEnableConsole(config.getEnableDebugConsole());
        nodeConfig.setEnableVerbose(config.getEnableVerboseLogging());
        nodeConfig.setJavaArgs(config.getJavaArguments());
        nodeConfig.setJavaHome(config.getJavaHome());
        nodeConfig.setNodeArgs(config.getNodeArguments());
        nodeConfig.setRArch(config.getRArchitecture());
        nodeConfig.setRHome(config.getRHome());
        nodeConfig.setRStartupSnippet(config.getStartupSnippet());
        nodeConfig.setStartStopTimeout(config.getStartStopTimeout());

        for (final EnvironmentVariable var : config.getEnvironmentVariables())
        {
            nodeConfig.getEnvironmentVariables().put(var.getName(), var.getValue());
        }

        if (save)
        {
            server.applyAndSaveConfiguration(nodeConfig);
        }
        else
        {
            server.applyConfiguration(nodeConfig);
        }
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
}
