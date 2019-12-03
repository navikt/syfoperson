package no.nav.syfo.filters

import no.nav.syfo.CALL_ID
import no.nav.syfo.util.MDCOperations
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CallIdFilter : Filter {

    override fun init(filterConfig: FilterConfig) {}

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        try {
            val callId = (servletRequest as? HttpServletRequest)?.getHeader(CALL_ID) ?: MDCOperations.generateCallId()

            MDCOperations.putToMDC(CALL_ID, callId)

            filterChain.doFilter(servletRequest, servletResponse)
        } finally {
            MDCOperations.remove(CALL_ID)
        }
    }

    override fun destroy() {}
}