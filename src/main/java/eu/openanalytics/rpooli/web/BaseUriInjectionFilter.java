
package eu.openanalytics.rpooli.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class BaseUriInjectionFilter implements Filter
{
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
        throws IOException, ServletException
    {
        final HttpServletRequest req = (HttpServletRequest) request;
        final String baseUri = StringUtils.replace(req.getRequestURL().toString(), req.getRequestURI(),
            req.getContextPath());
        request.setAttribute("baseUri", baseUri);

        chain.doFilter(request, response);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException
    {
        // NOOP
    }

    @Override
    public void destroy()
    {
        // NOOP
    }
}
