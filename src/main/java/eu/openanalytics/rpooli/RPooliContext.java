
package eu.openanalytics.rpooli;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.io.File.separatorChar;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.walware.rj.RjInvalidConfigurationException;
import de.walware.rj.server.srvext.RJContext;

/**
 * Variant of <code>de.walware.rj.servi.webapp.ServletRJContext</code> that can locate RJ JARs in
 * the classpath and that supports Maven-versioned JARs.
 * 
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
public class RPooliContext extends RJContext
{
    private static final Log LOGGER = LogFactory.getLog(RPooliContext.class);

    private static final String[] POOLI_CONFIGURATION_DIRECTORIES = {
        // *nix specific
        "/etc/rpooli",
        // any platform
        System.getProperty("user.home") + separatorChar + "rpooli",
        // web-app embedded
        "/WEB-INF/"};

    private final ServletContext servletContext;
    private final String propertiesDirPath;

    public RPooliContext(final ServletContext servletContext)
    {
        this.servletContext = checkNotNull(servletContext, "servletContext can't be null");
        propertiesDirPath = initializePropertiesDirPath();
    }

    private static String initializePropertiesDirPath()
    {
        for (final String confDir : POOLI_CONFIGURATION_DIRECTORIES)
        {
            if (new File(confDir).isDirectory())
            {
                return confDir;
            }
        }

        final String tempDir = System.getProperty("java.io.tmpdir");

        LOGGER.error("None of the configured configuration directories exist: "
                     + Arrays.toString(POOLI_CONFIGURATION_DIRECTORIES) + ", defaulting to: " + tempDir
                     + " so RPooli can start. The current setup is not production grade!");

        return tempDir;
    }

    @Override
    protected String[] getLibDirPaths() throws RjInvalidConfigurationException
    {
        try
        {
            final URL rjResourceUrl = RPooliContext.class.getClassLoader().getResource(
                RJContext.class.getName().replace('.', '/') + ".class");

            LOGGER.info("rj resource URL: " + rjResourceUrl);

            final String rjResourcePath = rjResourceUrl.getPath();
            final int indexOfColon = rjResourcePath.indexOf(':');
            final int indexOfBang = rjResourcePath.indexOf('!');

            final String rjJarsPath = new File(rjResourcePath.substring(indexOfColon + 1, indexOfBang)).getParentFile()
                .getCanonicalPath();

            LOGGER.info("rj JARs path: " + rjJarsPath);

            final String webInfLibPath = servletContext.getRealPath("WEB-INF/lib");

            final Set<String> uniqueLibDirPaths = new HashSet<>(asList(rjJarsPath, webInfLibPath));
            LOGGER.info("Collected lib dir paths: " + uniqueLibDirPaths);

            return uniqueLibDirPaths.toArray(EMPTY_STRING_ARRAY);
        }
        catch (final IOException ioe)
        {
            throw new RjInvalidConfigurationException("Failed to collect lib dir paths", ioe);
        }
    }

    @Override
    protected PathEntry searchLib(final List<PathEntry> files, final String libId)
    {
        final Pattern pattern = compile(".*" + quote(File.separatorChar + libId) + "([-_]{1}.*)?\\.jar$");

        for (final PathEntry entry : files)
        {
            if (pattern.matcher(entry.getPath()).matches())
            {
                return entry;
            }
        }

        return null;
    }

    @Override
    public String getServerPolicyFilePath() throws RjInvalidConfigurationException
    {
        String path = servletContext.getRealPath("WEB-INF/lib");

        if (isBlank(path) || !endsWithAny(path, "/", File.separator))
        {
            path = path + File.separatorChar;
        }

        return path + "security.policy";
    }

    @Override
    protected String getPropertiesDirPath()
    {
        return propertiesDirPath;
    }

    @Override
    protected InputStream getInputStream(final String path) throws IOException
    {
        // try first with a file path
        final File file = new File(path);
        if (file.isFile() && file.canRead())
        {
            return new FileInputStream(file);
        }
        else
        {
            // fallback to web-app embedded files
            return this.servletContext.getResourceAsStream(path);
        }
    }

    @Override
    protected OutputStream getOutputStream(final String path) throws IOException
    {
        // use a real file path if its parent exist and is a directory that is writable
        File file = new File(path);

        if (!file.getParentFile().isDirectory() || !file.getParentFile().canWrite())
        {
            final String realPath = this.servletContext.getRealPath(path);
            if (realPath == null)
            {
                throw new IOException("Writing to '" + path + "' not supported.");
            }
            file = new File(realPath);
        }

        try
        {
            if (!file.exists())
            {
                if (!file.createNewFile())
                {
                    throw new IOException("Failed to create file: " + file);
                }
            }

            return new FileOutputStream(file, false);
        }
        catch (final IOException ioe)
        {
            throw new IOException("Failed to get output stream for: " + file, ioe);
        }
    }
}
