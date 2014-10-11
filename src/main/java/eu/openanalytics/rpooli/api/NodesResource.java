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

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.jsonNotFound;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.jsonOK;
import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesResponse.jsonOK;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.Validate;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import eu.openanalytics.rpooli.AbstractRPooliServerAware;
import eu.openanalytics.rpooli.ClientSimulator;
import eu.openanalytics.rpooli.RPooliNode;
import eu.openanalytics.rpooli.RPooliServer;
import eu.openanalytics.rpooli.api.spec.model.ErrorJson;
import eu.openanalytics.rpooli.api.spec.model.Node;
import eu.openanalytics.rpooli.api.spec.model.NodesJson;
import eu.openanalytics.rpooli.api.spec.resource.Nodes;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
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
        return rpn == null
                          ? jsonNotFound(new ErrorJson().withError("Node not found: " + nodeId))
                          : jsonOK(buildNode(rpn));
    }

    @Override
    public DeleteNodesByNodeIdResponse deleteNodesByNodeId(final String nodeId, final boolean kill)
        throws Exception
    {
        getNodeOrDie(nodeId).getObject().evict(kill ? 0L : server.getConfig().getEvictionTimeout());

        return DeleteNodesByNodeIdResponse.withoutContent();
    }

    @Override
    public PostNodesByNodeIdConsoleResponse postNodesByNodeIdConsole(final String nodeId) throws Exception
    {
        getNodeOrDie(nodeId).getObject().enableConsole(NO_AUTH_CONFIG);

        return PostNodesByNodeIdConsoleResponse.withoutContent();
    }

    @Override
    public DeleteNodesByNodeIdConsoleResponse deleteNodesByNodeIdConsole(final String nodeId)
        throws Exception
    {
        getNodeOrDie(nodeId).getObject().disableConsole();

        return DeleteNodesByNodeIdConsoleResponse.withoutContent();
    }

    @Override
    public PostNodesTestResponse postNodesTest() throws Exception
    {
        clientSimulator.acquireNode();

        return PostNodesTestResponse.withoutContent();
    }

    @Override
    public DeleteNodesTestResponse deleteNodesTest() throws Exception
    {
        clientSimulator.releaseAllNodes();

        return DeleteNodesTestResponse.withoutContent();
    }

    private RPooliNode getNodeOrDie(final String nodeId)
    {
        final RPooliNode rpn = server.findNodeById(nodeId);
        if (rpn == null)
        {
            throw new WebApplicationException(NOT_FOUND);
        }
        return rpn;
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
