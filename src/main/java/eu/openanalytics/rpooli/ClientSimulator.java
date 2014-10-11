/**
 * This file is part of RPooli.
 *
 * RPooli is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RPooli is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with RPooli.  If not, see <http://www.gnu.org/licenses/>.
 */

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

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
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
