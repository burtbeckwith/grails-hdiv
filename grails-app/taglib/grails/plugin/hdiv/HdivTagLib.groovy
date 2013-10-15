package grails.plugin.hdiv

import javax.servlet.jsp.JspTagException
import javax.servlet.jsp.JspException
import javax.servlet.jsp.JspTagException

import org.apache.taglibs.standard.resources.Resources
import org.apache.taglibs.standard.tag.common.core.ImportSupport
import org.apache.taglibs.standard.tag.common.core.ParamSupport
import org.apache.taglibs.standard.tag.common.core.ParamSupport.ParamManager
import org.hdiv.dataComposer.IDataComposer
import org.hdiv.util.HDIVUtil

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class HdivTagLib {

	static namespace = 'hdiv'

	/**
	 * Ciphers data without using Spring MVC. Prints the encoded value of the 'value' attribute.
	 *
	 * @attr action In case of a link or a form, this is the url
	 * @attr parameter REQUIRED Name of the parameter to encode
	 * @attr value REQUIRED Value of the parameter to encode
	 */
	def cipher = { attrs ->
		String action = attrs.action
		String parameter = attrs.parameter
		String value = attrs.value

		IDataComposer dataComposer = request[HDIVUtil.DATACOMPOSER_REQUEST_KEY]
		String cipheredValue = action == null ?
			dataComposer.compose(parameter, value, false) :
			dataComposer.compose(action, parameter, value, false)

		out << cipheredValue
	}

	/**
	 * Redirects to a new URL. Grails version of the HDIV version of c:redirect.
	 *
	 * @attr url REQUIRED The URL of the resource to redirect to
	 * @attr context Name of the context when redirecting to a relative URL resource that belongs to a foreign context
	 */
	def redirect = { attrs ->

		def attrsCopy = [:] + attrs

		String url = attrsCopy.remove('url')
		String context = attrsCopy.remove('context')

		String baseUrl = resolveUrl(url, context)
		String result = aggregateParams(baseUrl, attrsCopy)

		// if the URL is relative, rewrite it with 'redirect' encoding rules
		if (!ImportSupport.isAbsoluteUrl(result)) {
			result = response.encodeRedirectURL(result)
		}

		// Call to HDIV
		result = HDIVUtil.getLinkUrlProcessor(servletContext).processUrl(request, result)

		response.sendRedirect result
	}

	/**
	 * Creates a URL with optional query parameters. Grails version of the HDIV version of c:url.
	 *
	 * @attr value REQUIRED URL to be processed
	 * @attr context Name of the context when specifying a relative URL resource that belongs to a foreign context
	 */
	def url = { attrs ->

		def attrsCopy = [:] + attrs
		String value = attrsCopy.remove('value')
		String context = attrsCopy.remove('context')

		String baseUrl = resolveUrl(value, context)
		String result = aggregateParams(baseUrl, attrsCopy)

		// if the URL is relative, rewrite it
		if (!ImportSupport.isAbsoluteUrl(result)) {
			result = response.encodeURL(result)
		}

		// Call to HDIV
		result = HDIVUtil.getLinkUrlProcessor(servletContext).processUrl(request, result)

		out << result
	}

	def errors = { attrs ->
		def bean = attrs.bean
		if (!bean.hasErrors()) {
			return
		}

		out << """<ul class="error" role="alert">"""
		for (error in bean.errors.allErrors) {
			out << """<li>${g.message(error: error, encodeAs: 'HTML')}</li>"""
		}

		out << '</ul>'
	}

	protected String resolveUrl(String url, String context) throws JspException {
		// don't touch absolute URLs
		if (ImportSupport.isAbsoluteUrl(url)) {
			return url
		}

		// normalize relative URLs against a context root
		if (context == null) {
			if (url.startsWith('/')) {
				return request.contextPath + url
			}
			return url
		}

		if (!context.startsWith('/') || !url.startsWith('/')) {
			throw new JspTagException(Resources.getMessage('IMPORT_BAD_RELATIVE'))
		}

		if (context.equals('/')) {
			// Don't produce string starting with '//', many
			// browsers interpret this as host name, not as
			// path on same host.
			return url
		}

		return context + url
	}

	protected String aggregateParams(String baseUrl, Map attrs) {
		ParamManager paramManager = new ParamManager()
		attrs.each { String name, value -> paramManager.addParameter(name, value) }
		paramManager.aggregateParams(baseUrl)
	}
}
