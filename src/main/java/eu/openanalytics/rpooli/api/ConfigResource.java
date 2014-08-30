
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
        final RServiNodeConfig config = server.getDefaultConfig();

        final List<EnvironmentVariable> environmentVariables = new ArrayList<>();
        for (final Entry<String, String> var : config.getEnvironmentVariables().entrySet())
        {
            environmentVariables.add(new EnvironmentVariable().withName(var.getKey()).withValue(
                var.getValue()));
        }

        final ConfRJson confRJson = new ConfRJson().withEnableDebugConsole(config.getEnableConsole())
            .withEnableVerboseLogging(config.getEnableVerbose())
            .withEnvironmentVariables(environmentVariables)
            .withJavaArguments(config.getJavaArgs())
            .withJavaHome(config.getJavaHome())
            .withRArchitecture(config.getRArch())
            .withRHome(config.getRHome())
            .withRLibraries(config.getEnvironmentVariables().get("R_LIBS"))
            .withStartStopTimeout(config.getStartStopTimeout())
            .withStartupSnippet(config.getRStartupSnippet())
            .withWorkingDirectory(config.getBaseWorkingDirectory());

        return GetConfigRDefaultResponse.jsonOK(confRJson);
    }

    @Override
    public GetConfigRResponse getConfigR() throws Exception
    {
        // FIXME implement me
        throw new UnsupportedOperationException();
    }

    @Override
    public void putConfigR(final boolean save) throws Exception
    {
        // FIXME implement me
        throw new UnsupportedOperationException();
    }
}
