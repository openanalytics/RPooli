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

import static org.eclipse.statet.internal.jcommons.runtime.CommonsRuntimeInternals.BUNDLE_ID;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.UrlResource;

import org.eclipse.statet.jcommons.io.UriUtils;
import org.eclipse.statet.jcommons.lang.Disposable;
import org.eclipse.statet.jcommons.lang.NonNullByDefault;
import org.eclipse.statet.jcommons.runtime.AppEnvironment;
import org.eclipse.statet.jcommons.runtime.BasicAppEnvironment;
import org.eclipse.statet.jcommons.runtime.ClassLoaderUtils;
import org.eclipse.statet.jcommons.runtime.CommonsRuntime;
import org.eclipse.statet.jcommons.runtime.bundle.BundleEntryProvider;
import org.eclipse.statet.jcommons.runtime.bundle.BundleResolver;
import org.eclipse.statet.jcommons.runtime.bundle.Bundles;
import org.eclipse.statet.jcommons.runtime.bundle.DefaultBundleResolver;
import org.eclipse.statet.jcommons.status.ErrorStatus;
import org.eclipse.statet.jcommons.status.StatusException;
import org.eclipse.statet.jcommons.status.util.ACommonsLoggingStatusLogger;
import org.eclipse.statet.rj.server.RjsComConfig;
import org.eclipse.statet.rj.server.client.RClientGraphicDummyFactory;


/**
 * The {@link AppEnvironment} implementation specific for RPooli.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
@NonNullByDefault
public class RPooliAppEnvironment extends BasicAppEnvironment implements Disposable
{

    private static BundleEntryProvider createBundleEntryProvider(final String refUrl) throws Exception {
        if (UriUtils.isFileUrl(refUrl) || UriUtils.isJarUrl(refUrl)) {
            return Bundles.detectEntryProvider(refUrl);
        }
        if (refUrl.endsWith(".jar/")) { //$NON-NLS-1$
            // url= 'vfs:/data/exploded-deploys/rpooli-1.3.0-20200317.093657-3/WEB-INF/lib/org.eclipse.statet.jcommons.util-4.2.0-20200317.093409-7.jar/'
            final URI jarUrl= new URI(refUrl.substring(0, refUrl.length() - 1));
            final UrlResource springResource= new UrlResource(jarUrl);
            final Path jarPath= springResource.getFile().toPath().toAbsolutePath();
            if (!Files.isRegularFile(jarPath)) {
                throw new UnsupportedOperationException("jarFile= " + jarPath); //$NON-NLS-1$
            }
            return Bundles.detectEntryProvider(
                    UriUtils.toJarUrlString(jarPath.toUri().toString()) );
        }
        throw new UnsupportedOperationException("url= " + refUrl); //$NON-NLS-1$
    }
    
    private static BundleResolver createResolver() throws StatusException {
        if (System.getProperty(Bundles.BUNDLE_RESOLVERS_PROPERTY_KEY) != null) {
            return Bundles.createResolver();
        }
        
        final Class<?> refClass= Bundles.class;
        String refUrl= null;
        try {
            refUrl= ClassLoaderUtils.getClassLocationUrlString(refClass);
            return new DefaultBundleResolver(createBundleEntryProvider(refUrl));
        }
        catch (final Exception e) {
            throw new StatusException(new ErrorStatus(BUNDLE_ID,
                    String.format("Failed to detect bundle location" +
                            "%n\tclass= %1$s" + //$NON-NLS-1$
                            "%n\turl= %2$s", //$NON-NLS-1$
                            (refClass != null) ? refClass.getName() : "<NA>", //$NON-NLS-1$
                            (refUrl != null) ? '\'' + refUrl + '\'' : "<NA>" ), //$NON-NLS-1$
                    e ));
        }
    }


    public RPooliAppEnvironment() throws StatusException
    {
        super("eu.openanalytics.rpooli", new ACommonsLoggingStatusLogger(), createResolver());
        CommonsRuntime.init(this);

        RjsComConfig.setProperty("rj.servi.graphicFactory", new RClientGraphicDummyFactory());
    }

    @Override
    public void dispose()
    {
        onAppStopping();
    }

}
