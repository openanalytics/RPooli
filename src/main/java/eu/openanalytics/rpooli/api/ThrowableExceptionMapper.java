
package eu.openanalytics.rpooli.api;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThrowableExceptionMapper implements ExceptionMapper<Throwable>
{
    public static final String INTERNAL_SERVER_ERROR_PUBLIC_MESSAGE = "Server-side guru meditation :( The issue has been logged.";
    private static final Log LOGGER = LogFactory.getLog(ThrowableExceptionMapper.class);

    public Response toResponse(final Throwable t)
    {
        LOGGER.error(t.getMessage(), t);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.TEXT_PLAIN)
            .entity(INTERNAL_SERVER_ERROR_PUBLIC_MESSAGE)
            .build();
    }
}
