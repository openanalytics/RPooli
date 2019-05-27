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
package eu.openanalytics.rpooli;

import static org.apache.commons.lang3.StringUtils.strip;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.net.URI;

import org.apache.commons.lang3.Validate;

import org.eclipse.statet.rj.servi.pool.PoolNodeItem;
import org.eclipse.statet.rj.servi.pool.PoolNodeObject;

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliNode
{
    private final PoolNodeObject object;
    private final String id;
    private final URI address;

    public RPooliNode(final PoolNodeObject nodeObj)
    {
        this.object = Validate.notNull(nodeObj, "nodeObj can't be null");
        this.address = URI.create(this.object.getNodeHandler().getAddress().toString());
        this.id = strip(address.getPath(), "/");
    }

    public PoolNodeItem getItem()
    {
        return new PoolNodeItem(this.object, System.currentTimeMillis());
    }

    public String getId()
    {
        return id;
    }

    public URI getAddress()
    {
        return address;
    }

    public PoolNodeObject getObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}
