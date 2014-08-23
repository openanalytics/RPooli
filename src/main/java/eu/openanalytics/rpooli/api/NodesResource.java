
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.jsonOK;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.notFound;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesResponse.jsonOK;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.Validate;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.ClientSimulator;
import eu.openanalytics.rpooli.RPooliNode;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.Node;
import eu.openanalytics.rpooli.api.spec.model.NodeCommandJson;
import eu.openanalytics.rpooli.api.spec.model.NodeCommandJson.Command;
import eu.openanalytics.rpooli.api.spec.model.NodesJson;
import eu.openanalytics.rpooli.api.spec.resource.Nodes;

public class NodesResource extends AbstractRPooliServerAware implements Nodes
{
    private static final String NO_AUTH_CONFIG = "none";

    private final ClientSimulator clientSimulator;

    public NodesResource(final RPooliServer server, final ClientSimulator clientSimulator)
    {
        super(server);
        this.clientSimulator = Validate.notNull(clientSimulator);
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

    @Override
    public void postNodesByNodeIdCommand(final String nodeId, final NodeCommandJson entity) throws Exception
    {
        final RPooliNode rpn = server.findNodeById(nodeId);
        if (rpn == null)
        {
            throw new WebApplicationException(NOT_FOUND);
        }

        final Command command = entity.getCommand();

        switch (command)
        {
            case ENABLE_CONSOLE :
                rpn.getObject().enableConsole(NO_AUTH_CONFIG);
                return;
            case DISABLE_CONSOLE :
                rpn.getObject().disableConsole();
                return;
            case STOP :
                rpn.getObject().evict(server.getConfig().getEvictionTimeout());
                return;
            case KILL :
                rpn.getObject().evict(0L);
                return;
            default :
                throw new IllegalStateException("Unsupported command: " + command);
        }
    }

    @Override
    public void postNodesTest() throws Exception
    {
        clientSimulator.acquireNode();
    }

    @Override
    public void deleteNodesTest() throws Exception
    {
        clientSimulator.releaseAllNodes();
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
