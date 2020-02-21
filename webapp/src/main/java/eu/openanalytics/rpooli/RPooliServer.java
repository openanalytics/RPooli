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
package eu.openanalytics.rpooli;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;
import static eu.openanalytics.rpooli.RPooliServer.ConfigAction.APPLY_AND_SAVE;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.IterableUtils.find;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.google.common.base.Function;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.statet.jcommons.lang.Disposable;
import org.eclipse.statet.rj.RjInvalidConfigurationException;
import org.eclipse.statet.rj.servi.node.PropertiesBean;
import org.eclipse.statet.rj.servi.node.PropertiesBean.ValidationMessage;
import org.eclipse.statet.rj.servi.node.RServiNodeConfig;
import org.eclipse.statet.rj.servi.pool.JMPoolServer;
import org.eclipse.statet.rj.servi.pool.NetConfig;
import org.eclipse.statet.rj.servi.pool.PoolConfig;
import org.eclipse.statet.rj.servi.pool.PoolNodeObject;

/**
 * The actual server that bootstraps R nodes.
 *
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public final class RPooliServer implements Disposable
{
    public enum ConfigAction
    {
        APPLY_ONLY, APPLY_AND_SAVE
    }

    private interface ConfigurationApplier<T extends PropertiesBean>
    {
        void apply(T config) throws RjInvalidConfigurationException;
    }

    private static final Log LOGGER = LogFactory.getLog(RPooliServer.class);

    private final JMPoolServer server;
    private final PoolConfig config;

    public static RPooliServer create(final ServletContext servletContext, final RPooliContext context)
    {
        checkNotNull(servletContext, "servletContext can't be null");
        checkNotNull(context, "context can't be null");

        final String serverId = removeStart(servletContext.getContextPath(), "/");
        return new RPooliServer(serverId, context);
    }

    private RPooliServer(final String id, final RPooliContext context)
    {
        LOGGER.info("Initializing with ID: " + id);

        try
        {
            LOGGER.info("RPooli properties file location: " + context.getPropertiesDirPath());
            LOGGER.info("Initializing: " + JMPoolServer.class.getSimpleName());
            server = new JMPoolServer(id, context);
            LOGGER.info("Starting: " + server);
            server.start();

            config = getDefaultPoolConfig();
            server.getPoolConfig(config);

            LOGGER.info("Started with pool address: " + server.getPoolAddress());
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    @Override
    public void dispose()
    {
        try
        {
            if (server != null)
            {
                LOGGER.info("Shutting down server: " + server);
                server.shutdown();
                server.waitForDisposal(0);
            }
        }
        catch (final Exception e)
        {
            LOGGER.error("Failed to shutdown server", e);
        }
    }

    public PoolConfig getConfig()
    {
        return config;
    }

    public JMPoolServer getServer()
    {
        return server;
    }

    public Collection<RPooliNode> getNodes()
    {
        return collect(server.getManager().getPoolNodeObjects(),
            new Transformer<PoolNodeObject, RPooliNode>()
            {
                @Override
                public RPooliNode transform(final PoolNodeObject nodeObj)
                {
                    return new RPooliNode(nodeObj);
                }
            });
    }

    public RPooliNode findNodeById(final String nodeId)
    {
        return find(getNodes(), new Predicate<RPooliNode>()
        {
            @Override
            public boolean evaluate(final RPooliNode node)
            {
                return node.getId().equals(nodeId);
            }
        });
    }

    public RServiNodeConfig getCurrentRConfig()
    {
        final RServiNodeConfig config = getDefaultRConfig();
        server.getNodeConfig(config);
        return validate(config);
    }

    public RServiNodeConfig getDefaultRConfig()
    {
        return validate(new RServiNodeConfig());
    }

    public void setConfiguration(final RServiNodeConfig config, final ConfigAction action) throws IOException
    {
        applyConfiguration(config, new ConfigurationApplier<RServiNodeConfig>()
        {
            @Override
            public void apply(final RServiNodeConfig config) throws RjInvalidConfigurationException
            {
                server.setNodeConfig(config);
            }
        }, action);
    }

    public PoolConfig getCurrentPoolConfig()
    {
        final PoolConfig config = getDefaultPoolConfig();
        server.getPoolConfig(config);
        return validate(config);
    }

    public PoolConfig getDefaultPoolConfig()
    {
        return validate(new PoolConfig());
    }

    public void setConfiguration(final PoolConfig config, final ConfigAction action) throws IOException
    {
        applyConfiguration(config, new ConfigurationApplier<PoolConfig>()
        {
            @Override
            public void apply(final PoolConfig config) throws RjInvalidConfigurationException
            {
                server.setPoolConfig(config);
            }
        }, action);
    }

    public NetConfig getCurrentNetConfig()
    {
        final NetConfig config = getDefaultNetConfig();
        server.getNetConfig(config);
        return validate(config);
    }

    public NetConfig getDefaultNetConfig()
    {
        return validate(new NetConfig());
    }

    public void setConfiguration(final NetConfig config, final ConfigAction action) throws IOException
    {
        applyConfiguration(config, new ConfigurationApplier<NetConfig>()
        {
            @Override
            public void apply(final NetConfig config) throws RjInvalidConfigurationException
            {
                server.setNetConfig(config);
            }
        }, action);
    }

    private <T extends PropertiesBean> void applyConfiguration(final T config,
                                                               final ConfigurationApplier<T> applier,
                                                               final ConfigAction action) throws IOException
    {
        validate(config);

        try
        {
            applier.apply(config);
        }
        catch (final RjInvalidConfigurationException rice)
        {
            throw new IllegalArgumentException("Invalid configuration for " + config.getBeanId() + ": "
                                               + rice.getMessage(), rice);
        }

        if (action == APPLY_AND_SAVE)
        {
            saveProperties(config);
        }
    }

    private static <T extends PropertiesBean> T validate(final T config)
    {
        final List<ValidationMessage> messages = new ArrayList<>();

        if (config.validate(messages))
        {
            return config;
        }
        else
        {
            final String reason = join(transform(messages, new Function<ValidationMessage, String>()
            {
                @Override
                public String apply(final ValidationMessage vm)
                {
                    return vm == null ? "n/a" : vm.getPropertyId() + ": " + vm.getMessage();
                }
            }), ", ");

            throw new IllegalArgumentException("Invalid configuration for " + config.getBeanId() + ": "
                                               + reason);
        }
    }

    private void saveProperties(final PropertiesBean bean) throws IOException
    {
        final Properties properties = new Properties();
        bean.save(properties);
        server.getRJContext().saveProperties(bean.getBeanId(), properties);
    }
}
