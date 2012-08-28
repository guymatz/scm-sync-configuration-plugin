package hudson.plugins.scm_sync_configuration.extensions;

import hudson.Extension;
import hudson.plugins.scm_sync_configuration.ScmSyncConfigurationDataProvider;
import hudson.plugins.scm_sync_configuration.ScmSyncConfigurationPlugin;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * @author fcamblor
 * Very important class in the plugin : it is the entry point allowing to decide what files should be
 * synchronized or not during current thread execution
 */
@Extension
public class ScmSyncConfigurationFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        // In the beginning of every http request, we should create a new threaded transaction
        ScmSyncConfigurationPlugin.getInstance().startThreadedTransaction();

        try {
            // Providing current ServletRequest in ScmSyncConfigurationDataProvider's thread local
            // in order to be able to access it from everywhere inside this call
            ScmSyncConfigurationDataProvider.provideRequestDuring((HttpServletRequest)request, new Callable() {
                public Object call() throws Exception {
                    try {
                        // Handling "normally" http request
                        chain.doFilter(request, response);
                    }finally{
                        // In the end of http request, we should commit current transaction
                        ScmSyncConfigurationPlugin.getInstance().getTransaction().commit();
                    }

                    return null;
                }
            });
        } catch(Exception e){
            throw new ServletException(e);
        }
    }

    public void destroy() {
    }
}
