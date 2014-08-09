
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesResponse.jsonOK;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import eu.openanalytics.rpooli.Consumer;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.Node;
import eu.openanalytics.rpooli.api.spec.resource.Nodes;

public class NodesResource implements Nodes
{
    private final RPooliServer server;

    public NodesResource(final RPooliServer server)
    {
        this.server = server;
    }

    @Override
    public GetNodesResponse getNodes() throws Exception
    {
        final eu.openanalytics.rpooli.api.spec.model.Nodes nodes = new eu.openanalytics.rpooli.api.spec.model.Nodes();
        nodes.setPoolAddress(server.getPoolAddress());

        server.visitNodes(new Consumer<ObjectPoolItem>()
        {
            @Override
            public void consume(final ObjectPoolItem opi)
            {
                final Node node = new Node().withId((long) opi.getObject().hashCode())
                    .withClientId(nullIfNegativeOne(opi.getClientId()))
                    .withCreationTime(opi.getCreationTime())
                    .withDestructionTime(nullIfNegativeOne(opi.getDestrutionTime()))
                    .withLentCount(opi.getLentCount())
                    .withLentDuration(opi.getLentDuration())
                    .withState(Node.State.fromValue(opi.getState().toString()))
                    .withStateTime(opi.getStateTime());

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
