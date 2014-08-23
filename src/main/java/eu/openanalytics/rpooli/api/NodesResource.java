
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.jsonOK;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.notFound;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesResponse.jsonOK;
import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import eu.openanalytics.rpooli.RPooliNode;
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

        for (final RPooliNode rpn : server.getNodes())
        {
            nodes.getNodes().add(buildNode(rpn));
        }

        return jsonOK(nodes);
    }

    @Override
    public GetNodesByNodeIdResponse getNodesByNodeId(final String nodeId) throws Exception
    {
        final RPooliNode rpn = server.findNodeById(nodeId);
        return rpn == null ? notFound() : jsonOK(buildNode(rpn));
    }

    private static Node buildNode(final RPooliNode rpn)
    {
        final ObjectPoolItem opi = rpn.getItem();

        return new Node().withId(rpn.getId())
            .withAddress(rpn.getAddress())
            .withClientId(nullIfNegativeOne(opi.getClientId()))
            .withClientLabel(rpn.getObject().getPoolItemData().getClientLabel())
            .withCreationTime(opi.getCreationTime())
            .withDestructionTime(nullIfNegativeOne(opi.getDestrutionTime()))
            .withLentCount(opi.getLentCount())
            .withLentDuration(opi.getLentDuration())
            .withState(Node.State.fromValue(opi.getState().toString()))
            .withStateTime(opi.getStateTime())
            .withConsoleEnabled(rpn.getObject().isConsoleEnabled());
    }

    private static Long nullIfNegativeOne(final Long l)
    {
        return l == -1 ? null : l;
    }
}
