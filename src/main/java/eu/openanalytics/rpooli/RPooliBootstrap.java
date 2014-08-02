
package eu.openanalytics.rpooli;

import static org.apache.commons.lang3.StringUtils.removeStart;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Bootstraps RPooli.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliBootstrap implements ServletContextListener
{
    private RPooliAppEnvironment env;
    private RPooliContext context;
    private RPooliServer server;

    @Override
    public void contextInitialized(final ServletContextEvent sce)
    {
        env = new RPooliAppEnvironment();

        context = new RPooliContext(sce.getServletContext());

        final String serverId = removeStart(sce.getServletContext().getContextPath(), "/");
        server = new RPooliServer(serverId, context);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce)
    {
        if (server != null)
        {
            server.dispose();
        }

        if (env != null)
        {
            env.dispose();
        }
    }
}
