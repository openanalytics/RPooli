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

package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Pool.GetPoolResponse.jsonOK;

import java.net.URI;

import de.walware.rj.servi.pool.JMPoolServer;
import de.walware.rj.servi.pool.RServiPoolManager.Counter;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.PoolJson;
import eu.openanalytics.rpooli.api.spec.resource.Pool;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class PoolResource extends AbstractRPooliServerAware implements Pool
{
    public PoolResource(final RPooliServer server)
    {
        super(server);
    }

    @Override
    public GetPoolResponse getPool() throws Exception
    {
        final JMPoolServer ps = server.getServer();
        final Counter counter = ps.getManager().getCounter();

        final PoolJson poolJson = new PoolJson().withAddress(URI.create(ps.getPoolAddress()))
            .withIdlingCount((long) counter.numIdling)
            .withIdlingMax((long) counter.maxIdling)
            .withInUseCount((long) counter.numInUse)
            .withInUseMax((long) counter.maxInUse)
            .withTotalCount((long) counter.numTotal)
            .withTotalMax((long) counter.maxTotal);

        return jsonOK(poolJson);
    }
}
