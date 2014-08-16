
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesResponse.jsonOK;
import static org.apache.commons.lang3.StringUtils.strip;

import java.net.URI;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.internal.PoolObject;
import eu.openanalytics.rpooli.Consumer;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.Node;
import eu.openanalytics.rpooli.api.spec.model.NodesJson;
import eu.openanalytics.rpooli.api.spec.resource.Nodes;

public class NodesResource extends AbstractResource implements Nodes
{
    public NodesResource(final RPooliServer server)
    {
        super(server);
    }

    @Override
    public GetNodesResponse getNodes() throws Exception
    {
        final NodesJson nodes = new NodesJson();

        server.visitNodes(new Consumer<ObjectPoolItem>()
        {
            @Override
            public void consume(final ObjectPoolItem opi)
            {
                final PoolObject po = (PoolObject) opi.getObject();
                final URI address = URI.create(po.getAddress().toString());

                final Node node = new Node().withId(strip(address.getPath(), "/"))
                    .withAddress(address)
                    .withClientId(nullIfNegativeOne(opi.getClientId()))
                    .withClientLabel(po.getPoolItemData().getClientLabel())
                    .withCreationTime(opi.getCreationTime())
                    .withDestructionTime(nullIfNegativeOne(opi.getDestrutionTime()))
                    .withLentCount(opi.getLentCount())
                    .withLentDuration(opi.getLentDuration())
                    .withState(Node.State.fromValue(opi.getState().toString()))
                    .withStateTime(opi.getStateTime())
                    .withConsoleEnabled(po.isConsoleEnabled());

                nodes.getNodes().add(node);
            }
        });

        return jsonOK(nodes);
    }

    private static Long nullIfNegativeOne(final Long l)
    {
        return l == -1 ? null : l;
    }
}
