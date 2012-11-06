grails.project.work.dir = 'target'
grails.project.source.level = 1.6
grails.project.docs.output.dir = 'docs/manual' // for backwards-compatibility, the docs are checked into gh-pages branch

grails.project.dependency.resolution = {

	inherits 'global'
	log 'warn'

	repositories {
		grailsCentral()
		mavenLocal()
		mavenCentral()
	}

	dependencies {
		runtime('org.hdiv:hdiv-core:2.1.2') {
			excludes 'commons-codec', 'commons-fileupload', 'commons-logging', 'junit',
			         'servlet-api', 'spring-test', 'spring-web'
		}

		runtime('org.hdiv:hdiv-spring-mvc:2.1.2') {
			excludes 'commons-logging', 'hdiv-core', 'jsp-api', 'junit',
			         'servlet-api', 'spring-test', 'spring-webmvc'
		}

		runtime('org.hdiv:hdiv-config:2.1.2') {
			excludes 'commons-logging', 'hdiv-core', 'hdiv-spring-mvc', 'junit',
						'spring-test', 'spring-web'
		}

		runtime 'javax.servlet.jsp:jsp-api:2.1'
		runtime('javax.servlet:jstl:1.2') {
			excludes 'jsp-api'
		}
		runtime('org.hdiv:hdiv-jstl-taglibs-1.2:2.1.2') {
			excludes 'commons-logging', 'hdiv-core', 'jsp-api', 'jstl', 'junit', 'servlet-api'
		}
	}

	plugins {
		build(':release:2.0.4', ':rest-client-builder:1.0.2') {
			export = false
		}
	}
}
