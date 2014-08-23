package eu.openanalytics.rpooli;

import static org.apache.commons.lang3.StringUtils.strip;
import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.net.URI;

import org.apache.commons.lang3.Validate;

import de.walware.rj.servi.acommons.pool.ObjectPoolItem;
import de.walware.rj.servi.internal.PoolObject;

public class RPooliNode
{
    private final ObjectPoolItem item;
    private final String id;
    private final URI address;
    private final PoolObject object;

    public RPooliNode(final ObjectPoolItem item)
    {
        this.item = Validate.notNull(item, "item can't be null");
        this.object = (PoolObject) item.getObject();
        this.address = URI.create(this.object.getAddress().toString());
        this.id = strip(address.getPath(), "/");
    }

    public ObjectPoolItem getItem()
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

    public PoolObject getObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        return reflectionToString(this, SHORT_PREFIX_STYLE);
    }
}