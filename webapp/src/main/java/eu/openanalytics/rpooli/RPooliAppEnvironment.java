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

import org.eclipse.statet.jcommons.lang.Disposable;
import org.eclipse.statet.jcommons.runtime.AppEnvironment;
import org.eclipse.statet.jcommons.runtime.BasicAppEnvironment;
import org.eclipse.statet.jcommons.runtime.CommonsRuntime;
import org.eclipse.statet.jcommons.runtime.bundle.Bundles;
import org.eclipse.statet.jcommons.status.StatusException;
import org.eclipse.statet.jcommons.status.util.ACommonsLoggingStatusLogger;
import org.eclipse.statet.rj.server.RjsComConfig;
import org.eclipse.statet.rj.server.client.RClientGraphicDummyFactory;


/**
 * The {@link AppEnvironment} implementation specific for RPooli.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliAppEnvironment extends BasicAppEnvironment implements Disposable
{


    public RPooliAppEnvironment() throws StatusException
    {
        super("eu.openanalytics.rpooli", new ACommonsLoggingStatusLogger(), Bundles.createResolver());
        CommonsRuntime.init(this);

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicDummyFactory());
    }

    @Override
    public void dispose()
    {
        onAppStopping();
    }

}
