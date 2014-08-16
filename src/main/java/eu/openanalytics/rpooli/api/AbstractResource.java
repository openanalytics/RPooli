
package eu.openanalytics.rpooli.api;

import org.apache.commons.lang3.Validate;

import eu.openanalytics.rpooli.RPooliServer;

public abstract class AbstractResource
{
    protected final RPooliServer server;

    public AbstractResource(final RPooliServer server)
    {
        this.server = Validate.notNull(server, "RPooliServer can't be null");
    }
}
