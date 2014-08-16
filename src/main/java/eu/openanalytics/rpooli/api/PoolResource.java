
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Pool.GetPoolResponse.jsonOK;

import java.net.URI;

import de.walware.rj.servi.pool.JMPoolServer;
import de.walware.rj.servi.pool.RServiPoolManager.Counter;
import eu.openanalytics.rpooli.Consumer;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.PoolJson;
import eu.openanalytics.rpooli.api.spec.resource.Pool;

public class PoolResource extends AbstractResource implements Pool
{
    public PoolResource(final RPooliServer server)
    {
        super(server);
    }

    @Override
    public GetPoolResponse getPool() throws Exception
    {
        final PoolJson poolJson = new PoolJson();

        server.visitPool(new Consumer<JMPoolServer>()
        {
            @Override
            public void consume(final JMPoolServer ps)
            {
                poolJson.setAddress(URI.create(ps.getPoolAddress()));

                final Counter counter = ps.getManager().getCounter();
                poolJson.setIdlingCount((long) counter.numIdling);
                poolJson.setIdlingMax((long) counter.maxIdling);
                poolJson.setInUseCount((long) counter.numInUse);
                poolJson.setInUseMax((long) counter.maxInUse);
                poolJson.setTotalCount((long) counter.numTotal);
                poolJson.setTotalMax((long) counter.maxTotal);
            }
        });

        return jsonOK(poolJson);
    }
}
