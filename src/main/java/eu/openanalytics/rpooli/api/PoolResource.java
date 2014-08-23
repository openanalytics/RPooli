
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Pool.GetPoolResponse.jsonOK;

import java.net.URI;

import de.walware.rj.servi.pool.JMPoolServer;
import de.walware.rj.servi.pool.RServiPoolManager.Counter;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.PoolJson;
import eu.openanalytics.rpooli.api.spec.resource.Pool;

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
