/*
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

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import static eu.openanalytics.rpooli.api.spec.resource.Nodes.GetNodesByNodeIdResponse.withJsonOK;

import javax.ws.rs.WebApplicationException;

import org.eclipse.statet.rj.servi.pool.PoolNodeItem;
import org.eclipse.statet.rj.servi.pool.PoolNodeState;

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
 * @author "Open Analytics &lt;rsb.development@openanalytics.eu&gt;"
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

        for (final RPooliNode rpn : this.server.getNodes())
        {
            nodes.getNodes().add(buildNode(rpn));
        }

        return GetNodesResponse.withJsonOK(nodes);
    }

    @Override
    public GetNodesByNodeIdResponse getNodesByNodeId(final String nodeId) throws Exception
    {
        final RPooliNode rpn = this.server.findNodeById(nodeId);
        return rpn == null
                          ? GetNodesByNodeIdResponse.withJsonNotFound(new ErrorJson().withError("Node not found: "
                                                                                                + nodeId))
                          : withJsonOK(buildNode(rpn));
    }

	@Override
	public DeleteNodesByNodeIdResponse deleteNodesByNodeId(final String nodeId, final boolean kill)
			throws Exception {
		getNodeOrDie(nodeId).getObject().evict(
				(kill) ? null : this.server.getConfig().getEvictionTimeout() );
		
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
        this.clientSimulator.acquireNode();

        return PostNodesTestResponse.withNoContent();
    }

    @Override
    public DeleteNodesTestResponse deleteNodesTest() throws Exception
    {
        this.clientSimulator.releaseAllNodes();

        return DeleteNodesTestResponse.withNoContent();
    }

    private RPooliNode getNodeOrDie(final String nodeId)
    {
        final RPooliNode rpn = this.server.findNodeById(nodeId);
        if (rpn == null)
        {
            throw new WebApplicationException(NOT_FOUND);
        }
        return rpn;
    }
	
	private static Node buildNode(final RPooliNode rpn) {
		final PoolNodeItem nodeItem= rpn.getItem();
		
		return new Node().withId(rpn.getId())
				.withAddress(rpn.getAddress())
				.withClientLabel(nodeItem.getCurrentClientLabel())
				.withCreationTime(toCreationTime(nodeItem))
				.withLentCount(nodeItem.getUsageCount())
				.withLentDuration(toLentDuration(nodeItem))
				.withState(convertState(rpn.getItem().getState()))
				.withStateTime(toStateTime(nodeItem))
				.withConsoleEnabled(nodeItem.isConsoleEnabled());
	}
	
	
	private static long toCreationTime(final PoolNodeItem nodeItem) {
		final var stamp= nodeItem.getCreationTime();
		return (stamp != null) ? stamp.toEpochMilli() : -1;
	}
	
	private static long toStateTime(final PoolNodeItem nodeItem) {
		final var stamp= nodeItem.getStateTime();
		return (stamp != null) ? stamp.toEpochMilli() : -1;
	}
	
	private static long toLentDuration(final PoolNodeItem nodeItem) {
		final var duration= nodeItem.getUsageDuration();
		return (duration != null) ? duration.toMillis() : -1;
	}
	
	private static Node.State convertState(final PoolNodeState state) {
		switch (state) {
		case INITIALIZING:
			return Node.State.INITIALIZING;
		case CHECKING:
			return Node.State.CHECKING;
		case IDLING:
			return Node.State.IDLING;
		case ALLOCATED:
			return Node.State.ALLOCATED;
		case DISPOSING:
			return Node.State.EVICTING;
		case DISPOSED:
			return Node.State.DISPOSED;
		default:
			throw new IllegalArgumentException(state.toString());
		}
	}
	
}
