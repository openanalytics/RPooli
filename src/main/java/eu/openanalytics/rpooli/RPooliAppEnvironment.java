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

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.core.runtime.IStatus.WARNING;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.ECommons;
import de.walware.ecommons.ECommons.IAppEnvironment;
import de.walware.ecommons.IDisposable;
import de.walware.rj.server.RjsComConfig;
import de.walware.rj.server.client.RClientGraphic;
import de.walware.rj.server.client.RClientGraphic.InitConfig;
import de.walware.rj.server.client.RClientGraphicActions;
import de.walware.rj.server.client.RClientGraphicDummy;
import de.walware.rj.server.client.RClientGraphicFactory;

/**
 * The {@link IAppEnvironment} implementation specific for RPooli.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliAppEnvironment implements IAppEnvironment, IDisposable
{
    private static final Log LOGGER = LogFactory.getLog(RPooliAppEnvironment.class);

    private final Set<IDisposable> stopListeners = new CopyOnWriteArraySet<>();

    public RPooliAppEnvironment()
    {
        ECommons.init("de.walware.rj.services.eruntime", this);

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicFactory()
        {
            @Override
            public Map<String, ? extends Object> getInitServerProperties()
            {
                return null;
            }

            @Override
            public RClientGraphic newGraphic(final int devId,
                                             final double w,
                                             final double h,
                                             final InitConfig config,
                                             final boolean active,
                                             final RClientGraphicActions actions,
                                             final int options)
            {
                return new RClientGraphicDummy(devId, w, h);
            }

            @Override
            public void closeGraphic(final RClientGraphic graphic)
            {
                // NOOP
            }
        });
    }

    @Override
    public void dispose()
    {
        for (final IDisposable listener : this.stopListeners)
        {
            try
            {
                listener.dispose();
            }
            catch (final Exception e)
            {
                LOGGER.error("Failed to dispose: " + listener, e);
            }
        }
    }

    @Override
    public void addStoppingListener(final IDisposable listener)
    {
        stopListeners.add(listener);
    }

    @Override
    public void removeStoppingListener(final IDisposable listener)
    {
        stopListeners.remove(listener);
    }

    @Override
    public void log(final IStatus status)
    {
        switch (status.getSeverity())
        {
            case INFO :
                LOGGER.info(status.getMessage(), status.getException());
                break;

            case WARNING :
                LOGGER.warn(status.getMessage(), status.getException());
                break;

            case ERROR :
                LOGGER.error(status.getMessage(), status.getException());
                break;

            default :
                LOGGER.debug(status.getMessage(), status.getException());
        }
    }
}
