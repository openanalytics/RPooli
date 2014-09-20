
package eu.openanalytics.rpooli;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.CollectionUtils.find;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;

import de.walware.ecommons.IDisposable;
import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.pool.JMPoolServer;
import de.walware.rj.servi.pool.PoolConfig;
import de.walware.rj.servi.pool.PropertiesBean;
import de.walware.rj.servi.pool.PropertiesBean.ValidationMessage;
import de.walware.rj.servi.pool.RServiNodeConfig;

/**
 * The actual server that bootstraps R nodes.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliServer implements IDisposable
{
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
        return collect(asList(server.getManager().getPoolItemsData()),
            new Transformer<ObjectPoolItem, RPooliNode>()
            {
                @Override
                public RPooliNode transform(final ObjectPoolItem item)
                {
                    return new RPooliNode(item);
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
        return config;
    }

    public RServiNodeConfig getDefaultRConfig()
    {
        return new RServiNodeConfig();
    }

    public PoolConfig getCurrentPoolConfig()
    {
        final PoolConfig config = getDefaultPoolConfig();
        server.getPoolConfig(config);
        return config;
    }

    public PoolConfig getDefaultPoolConfig()
    {
        return new PoolConfig();
    }

    public void applyConfiguration(final RServiNodeConfig config)
    {
        validate(config);

        try
        {
            server.setNodeConfig(config);
        }
        catch (final RjInvalidConfigurationException rice)
        {
            throw new IllegalArgumentException("Invalid configuration for " + config.getBeanId() + ": "
                                               + rice.getMessage(), rice);
        }
    }

    public void applyAndSaveConfiguration(final RServiNodeConfig config) throws IOException
    {
        applyConfiguration(config);
        saveProperties(config);
    }

    private void saveProperties(final PropertiesBean bean) throws IOException
    {
        final Properties properties = new Properties();
        bean.save(properties);
        server.getRJContext().saveProperties(bean.getBeanId(), properties);
    }

    private static void validate(final PropertiesBean bean)
    {
        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        if (bean.validate(messages))
        {
            return;
        }
        else
        {
            final String reason = join(transform(messages, new Function<ValidationMessage, String>()
            {
                @Override
                public String apply(final ValidationMessage vm)
                {
                    return vm.getPropertyId() + ": " + vm.getMessage();
                }
            }), ", ");

            throw new IllegalArgumentException("Invalid configuration for " + bean.getBeanId() + ": "
                                               + reason);
        }
    }
}
