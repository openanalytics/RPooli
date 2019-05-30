/**
 * This file is part of RPooli.
 *
 * RPooli is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with RPooli.  If not, see <http://www.apache.org/licenses/>.
 */
package eu.openanalytics.rpooli.api;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.withJsonOK;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.Validate;

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

        return GetNodesResponse.withJsonOK(nodes);
    }

    @Override
    public GetNodesByNodeIdResponse getNodesByNodeId(final String nodeId) throws Exception
    {
        final RPooliNode rpn = server.findNodeById(nodeId);
        return rpn == null
                          ? GetNodesByNodeIdResponse.withJsonNotFound(new ErrorJson().withError("Node not found: "
                                                                                                + nodeId))
                          : withJsonOK(buildNode(rpn));
    }

    @Override
    public DeleteNodesByNodeIdResponse deleteNodesByNodeId(final String nodeId, final boolean kill)
        throws Exception
    {
        getNodeOrDie(nodeId).getObject().evict(kill ? 0L : server.getConfig().getEvictionTimeout());

        return DeleteNodesByNodeIdResponse.withNoContent();
    }

    @Override
    public PostNodesByNodeIdConsoleResponse postNodesByNodeIdConsole(final String nodeId) throws Exception
    {
        getNodeOrDie(nodeId).getObject().getNodeHandler().enableConsole(NO_AUTH_CONFIG);

        return PostNodesByNodeIdConsoleResponse.withNoContent();
    }

    @Override
    public DeleteNodesByNodeIdConsoleResponse deleteNodesByNodeIdConsole(final String nodeId)
        throws Exception
    {
        getNodeOrDie(nodeId).getObject().getNodeHandler().disableConsole();

        return DeleteNodesByNodeIdConsoleResponse.withNoContent();
    }

    @Override
    public PostNodesTestResponse postNodesTest() throws Exception
    {
        clientSimulator.acquireNode();

        return PostNodesTestResponse.withNoContent();
    }

    @Override
    public DeleteNodesTestResponse deleteNodesTest() throws Exception
    {
        clientSimulator.releaseAllNodes();

        return DeleteNodesTestResponse.withNoContent();
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
        return new Node().withId(rpn.getId())
            .withAddress(rpn.getAddress())
            .withClientId(nullIfNegativeOne(rpn.getItem().getCurrentClientId()))
            .withClientLabel(rpn.getObject().getClientLabel())
            .withCreationTime(rpn.getItem().getCreationTime())
            //TODO Is this value still available?
//            .withDestructionTime(nullIfNegativeOne(rpn.getItem().getDestrutionTime()))
            .withLentCount(rpn.getItem().getUsageCount())
            .withLentDuration(rpn.getItem().getUsageDuration())
            .withState(Node.State.fromValue(rpn.getItem().getState().toString()))
            .withStateTime(rpn.getItem().getStateTime())
            .withConsoleEnabled(rpn.getObject().getNodeHandler().isConsoleEnabled());
    }

    private static Long nullIfNegativeOne(final String value)
    {
        if (value == null) return null;
        try {
    	    long lValue = Long.parseLong(value);
    	    return lValue == -1 ? null : lValue;
        } catch (Exception e) {}
        return null;
    }
}
