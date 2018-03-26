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
    private final PoolNodeItem item;
    private final String id;
    private final URI address;
    private final PoolNodeObject object;

    public RPooliNode(final PoolNodeObject object)
    {
    	this.object = Validate.notNull(object, "object can't be null");;
        this.item = new PoolNodeItem(object, 0); Validate.notNull(item, "item can't be null");
        this.address = URI.create(this.item.getAddress().toString());
        this.id = strip(address.getPath(), "/");
    }

    public PoolNodeItem getItem()
    {
        return item;
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
