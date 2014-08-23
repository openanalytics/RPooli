
package eu.openanalytics.rpooli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;

import de.walware.rj.RjException;
import de.walware.rj.server.ServerLogin;
import de.walware.rj.servi.RServi;

public class ClientSimulator extends AbstractRPooliServerAware
{
    private static final Log LOGGER = LogFactory.getLog(ClientSimulator.class);

    private static final String CLIENT_ID = "rpooli-client-simulator";
    private static final ServerLogin NO_LOGIN = null;

    private final List<RServi> checkedOut;

    public ClientSimulator(final RPooliServer server)
    {
        super(server);
        checkedOut = new ArrayList<>();
    }

    public synchronized void acquireNode() throws RjException
    {
        checkedOut.add(server.getServer().getManager().getRServi(CLIENT_ID, NO_LOGIN));
    }

    public synchronized void releaseAllNodes()
    {
        for (final Iterator<RServi> i = checkedOut.iterator(); i.hasNext();)
        {
            final RServi rServi = i.next();

            try
            {
                rServi.close();
                i.remove();
            }
            catch (final CoreException ce)
            {
                LOGGER.error("Failed to close: " + rServi, ce);
            }
        }
    }
}
