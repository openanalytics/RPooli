
package eu.openanalytics.rpooli.api;

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
        nodes.getNodes().add(new Node().withId("fake_id"));
        return GetNodesResponse.jsonOK(nodes);
    }
}
