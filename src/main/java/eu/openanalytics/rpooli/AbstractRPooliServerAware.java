
package eu.openanalytics.rpooli;

import org.apache.commons.lang3.Validate;

public abstract class AbstractRPooliServerAware
{
    protected final RPooliServer server;

    public AbstractRPooliServerAware(final RPooliServer server)
    {
        this.server = Validate.notNull(server, "RPooliServer can't be null");
    }
}
