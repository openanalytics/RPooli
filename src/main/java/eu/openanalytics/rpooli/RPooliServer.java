
package eu.openanalytics.rpooli;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static org.apache.commons.collections4.CollectionUtils.collect;
import static org.apache.commons.collections4.CollectionUtils.find;
import static org.apache.commons.lang3.StringUtils.removeStart;

import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.walware.ecommons.IDisposable;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.pool.JMPoolServer;
import de.walware.rj.servi.pool.PoolConfig;

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

            config = new PoolConfig();
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
}
