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

import static org.eclipse.core.runtime.IStatus.ERROR;
import static org.eclipse.core.runtime.IStatus.INFO;
import static org.eclipse.core.runtime.IStatus.WARNING;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.statet.ecommons.runtime.core.ECommonsRuntime;
import org.eclipse.statet.ecommons.runtime.core.ECommonsRuntime.AppEnvironment;
import org.eclipse.statet.jcommons.lang.Disposable;
import org.eclipse.statet.rj.server.RjsComConfig;
import org.eclipse.statet.rj.server.client.RClientGraphic;
import org.eclipse.statet.rj.server.client.RClientGraphic.InitConfig;
import org.eclipse.statet.rj.server.client.RClientGraphicActions;
import org.eclipse.statet.rj.server.client.RClientGraphicDummy;
import org.eclipse.statet.rj.server.client.RClientGraphicFactory;


/**
 * The {@link AppEnvironment} implementation specific for RPooli.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliAppEnvironment implements AppEnvironment, Disposable
{
    private static final Log LOGGER = LogFactory.getLog(RPooliAppEnvironment.class);

    private final Set<Disposable> stopListeners = new CopyOnWriteArraySet<>();

    public RPooliAppEnvironment()
    {
        ECommonsRuntime.init(ECommonsRuntime.BUNDLE_ID, this);

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
        for (final Disposable listener : this.stopListeners)
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
    public void addStoppingListener(final Disposable listener)
    {
        stopListeners.add(listener);
    }

    @Override
    public void removeStoppingListener(final Disposable listener)
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
