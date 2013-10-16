import grails.plugin.hdiv.GrailsEditableParameterValidator
import grails.plugin.hdiv.HdivUtils
import grails.plugin.webxml.FilterManager

import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener
import org.hdiv.filter.ValidatorFilter
import org.hdiv.listener.InitListener
import org.hdiv.web.multipart.HdivCommonsMultipartResolver
import org.hdiv.web.validator.GrailsEditableParameterValidatorConstraint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.filter.DelegatingFilterProxy

class HdivGrailsPlugin {

	private Logger log = LoggerFactory.getLogger(getClass())

	String version = '1.0-RC2'
	String grailsVersion = '2.3.1 > *'
	String title = 'HDIV Plugin'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt@burtbeckwith.com'
	String description = 'HDIV plugin'
	String documentation = 'http://burtbeckwith.github.io/grails-hdiv'
	List loadAfter = ['controllers', 'springSecurityCore']
	List pluginExcludes = [
		'docs/**',
		'src/docs/**'
	]

	String license = 'APACHE'
	def scm = [url: 'https://github.com/burtbeckwith/grails-hdiv']
	def issueManagement = [system: 'Github', url: 'https://github.com/burtbeckwith/grails-hdiv/issues']

	def getWebXmlFilterOrder() {
		[ValidatorFilter: FilterManager.GRAILS_WEB_REQUEST_POSITION - 100]
	}

	def doWithWebDescriptor = { xml ->

		def hdivConfig = HdivUtils.getConfig(application)

		def contextParam = xml.'context-param'

		contextParam[contextParam.size() - 1] + {
			filter {
				'filter-name'('hdivFilter')
				'filter-class'(DelegatingFilterProxy.name)
			}
		}

		String validatorFilterUrlPattern = hdivConfig.validatorFilterUrlPattern // '/*'

		def mappingLocation = xml.'filter'
		mappingLocation + {
			'filter-mapping' {
				'filter-name'('hdivFilter')
				'url-pattern'(validatorFilterUrlPattern)
			}
		}

		def listenerLocation = xml.'listener'.find { it.'listener-class'.text() == GrailsContextLoaderListener.name }
		listenerLocation + {
			listener {
				'listener-class'(InitListener.name)
			}
		}
	}

	def doWithSpring = {

		ConstrainedProperty.registerNewConstraint GrailsEditableParameterValidatorConstraint.NAME, GrailsEditableParameterValidatorConstraint

		def hdivConfig = HdivUtils.getConfig(application)

		xmlns hdiv:'http://www.hdiv.org/schema/hdiv'

		hdiv.config(id:                                 hdivConfig.config.id, // config
		            avoidCookiesConfidentiality:        hdivConfig.config.avoidCookiesConfidentiality.toString(), // false
		            avoidCookiesIntegrity:              hdivConfig.config.avoidCookiesIntegrity.toString(), // false
		            avoidValidationInUrlsWithoutParams: hdivConfig.config.avoidValidationInUrlsWithoutParams.toString(), // false
		            confidentiality:                    hdivConfig.config.confidentiality.toString(), // true
		            debugMode:                          hdivConfig.config.debugMode.toString(), // false
		            errorPage:                          hdivConfig.config.errorPage, // error.jsp
		            excludedExtensions:                 hdivConfig.config.excludedExtensions,
		            maxPagesPerSession:                 hdivConfig.config.maxPagesPerSession.toString(), // 5
		            protectedExtensions:                hdivConfig.config.protectedExtensions, // '.*'
		            randomName:                         hdivConfig.config.randomName.toString(), // false
		            strategy:                           hdivConfig.config.strategy, // memory
		            userData:                           hdivConfig.config.userData, // ''
		            showErrorPageOnEditableValidation:  hdivConfig.config.showErrorPageOnEditableValidation) { // false

			def startPageConfig = hdivConfig.config.startPages
			if (startPageConfig instanceof CharSequence) {
				startPageConfig = [ANY: startPageConfig.toString()]
			}
			else if (startPageConfig instanceof Map) {
				startPageConfig = [:] + startPageConfig
			}
			else {
				// TODO
			}

			if (hdivConfig.config.sessionExpiredLoginPage || hdivConfig.config.sessionExpiredHomePage) {
				sessionExpired(loginPage: hdivConfig.config.sessionExpiredLoginPage ?: '', homePage: hdivConfig.config.sessionExpiredHomePage ?: '')
			}

			startPageConfig.each { method, pages ->
				if (pages && method) {
					if ('ANY'.equalsIgnoreCase(method)) {
						startPages(pages.toString())
					}
					else {
						startPages(pages.toString(), method: method.toUpperCase())
					}
				}
			}

			if (hdivConfig.config.startParameters) {
				startParameters(hdivConfig.config.startParameters)
			}
			paramsWithoutValidation {
				hdivConfig.config.paramsWithoutValidation.each { pwv ->
					mapping(url: pwv.url, parameters: pwv.parameters)
				}
			}
		}

		// Accepted pattern within the application for all editable parameters (generated from textbox and textarea)
		hdivConfig.validations.each { validation ->
			hdiv.validation(id: validation.id, componentType: validation.componentType ?: '') {
				if (validation.acceptedPattern) acceptedPattern(validation.acceptedPattern)
				if (validation.rejectedPattern) rejectedPattern(validation.rejectedPattern)
			}
		}

		def toBoolean = { value ->
			if (value instanceof CharSequence) {
				value = Boolean.valueOf(value.toString())
			}
			else if (!(value instanceof Boolean)) {
				value = true
			}
			value
		}

		// Finally, it's necessary to define editable data validation list for the application
		hdivConfig.editableValidations.each { editableValidation ->
			hdiv.editableValidations(id: editableValidation.id, registerDefaults: toBoolean(editableValidation.registerDefaults).toString()) {
				for (rule in editableValidation.validationRules) {
					validationRule(url: rule.url, enableDefaults: toBoolean(rule.enableDefaults).toString(), rule.validationIds)
				}
			}
		}

		hdivEditableValidator(GrailsEditableParameterValidator)

		hdivFilter(ValidatorFilter)

		if (!application.config.grails.disableCommonsMultipart) {
			multipartResolver(HdivCommonsMultipartResolver) {
				maxUploadSize = 100000
			}
		}
	}
}
