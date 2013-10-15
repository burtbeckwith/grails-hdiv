import grails.plugin.hdiv.GrailsEditableParameterValidator
import grails.plugin.hdiv.HdivUtils
import grails.plugin.webxml.FilterManager

import org.codehaus.groovy.grails.web.context.GrailsContextLoaderListener
import org.hdiv.filter.ValidatorFilter
import org.hdiv.listener.InitListener
import org.hdiv.web.multipart.HdivCommonsMultipartResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.filter.DelegatingFilterProxy

class HdivGrailsPlugin {

	private Logger log = LoggerFactory.getLogger(getClass())

	String version = '0.1'
	String grailsVersion = '2.3.1 > *'
	String title = 'HDIV Plugin'
	String author = 'Burt Beckwith'
	String authorEmail = 'beckwithb@vmware.com'
	String description = 'HDIV plugin'
	String documentation = 'http://grails.org/plugin/hdiv'
	List loadAfter = ['controllers']
	List pluginExcludes = [
		'docs/**',
		'src/docs/**'
	]

	String license = 'APACHE'
//	def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPMYPLUGIN']
	def scm = [url: 'https://github.com/burtbeckwith/grails-hdiv']

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

		hdivEditableValidator(GrailsEditableParameterValidator)

		hdivFilter(ValidatorFilter)

		if (!application.config.grails.disableCommonsMultipart) {
			multipartResolver(HdivCommonsMultipartResolver) {
				maxUploadSize = 100000
			}
		}

		/*
		 <?xml version="1.0" encoding="UTF-8"?>
		 <beans xmlns="http://www.springframework.org/schema/beans"
			 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:hdiv="http://www.hdiv.org/schema/hdiv"
			 xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		 http://www.hdiv.org/schema/hdiv http://www.hdiv.org/schema/hdiv/hdiv.xsd">

			 <hdiv:config errorPage="/error.jsp" excludedExtensions="css,png,ico">
				 <hdiv:startPages>/,/attacks/.*,/login/auth,/j_spring_security_check,/grails-errorhandler</hdiv:startPages>
			 </hdiv:config>

			 <!-- Accepted pattern within the application for all editable parameters (generated from textbox and textarea) -->
			 <hdiv:validation id="safeText">
				 <hdiv:acceptedPattern><![CDATA[^[a-zA-Z0-9@.\-_]*$]]></hdiv:acceptedPattern>
			 </hdiv:validation>

			 <!-- Finally, it's necessary to define editable data validation list for the application -->
			 <hdiv:editableValidations>
				 <hdiv:validationRule url="/secure/.*">safeText</hdiv:validationRule>
				 <hdiv:validationRule url="/safetext/.*" enableDefaults="false">safeText</hdiv:validationRule>
			 </hdiv:editableValidations>

		 </beans>
		 */

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
		            userData:                           hdivConfig.config.userData) { // ''
			startPages(hdivConfig.config.startPages)
			if (hdivConfig.config.startParameters) startParameters(hdivConfig.config.startParameters)
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
	}
}
