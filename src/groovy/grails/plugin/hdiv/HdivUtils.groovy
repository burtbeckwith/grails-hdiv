package grails.plugin.hdiv

import grails.util.Environment

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Helper methods.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class HdivUtils {

	private static final Logger log = LoggerFactory.getLogger(this)

	private static ConfigObject config

	/**
	 * Parse and load the configuration.
	 * @param application the application
	 * @return the configuration
	 */
	static ConfigObject getConfig(GrailsApplication application) {
		if (config == null) {
			mergeConfig application.config.grails.plugin.hdiv, 'DefaultHdivConfig', application
		}

		config
	}

	/**
	 * Merge in a secondary config (provided by a plugin as defaults) into the main config.
	 * @param currentConfig the current configuration
	 * @param className the name of the config class to load
	 */
	private static void mergeConfig(ConfigObject currentConfig, String className, GrailsApplication application) {
		GroovyClassLoader classLoader = new GroovyClassLoader(Thread.currentThread().contextClassLoader)
		ConfigSlurper slurper = new ConfigSlurper(Environment.current.name)
		ConfigObject secondaryConfig = slurper.parse(classLoader.loadClass(className))

		config = mergeConfig(currentConfig, secondaryConfig.hdiv)
		application.config.grails.plugin.hdiv = config
	}

	/**
	 * Merge two configs together. The order is important; if <code>secondary</code> is not null then
	 * start with that and merge the main config on top of that. This lets the <code>secondary</code>
	 * config act as default values but let user-supplied values in the main config override them.
	 *
	 * @param currentConfig the main config, starting from Config.groovy
	 * @param secondary new default values
	 * @return the merged configs
	 */
	private static ConfigObject mergeConfig(ConfigObject currentConfig, ConfigObject secondary) {
		ConfigObject config = new ConfigObject()
		if (secondary == null) {
			config.putAll(currentConfig)
		}
		else {
			config.putAll(secondary.merge(currentConfig))
		}
		config
	}
}
