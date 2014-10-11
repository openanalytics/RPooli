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

/**
 * @author "OpenAnalytics &lt;rsb.development@openanalytics.eu&gt;"
 */
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
