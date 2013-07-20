grails.project.work.dir = 'target'
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

		String hdivVersion = '2.1.4'

		compile "org.hdiv:hdiv-core:$hdivVersion", {
			excludes 'commons-codec', 'commons-fileupload', 'commons-logging', 'junit',
			         'servlet-api', 'spring-test', 'spring-web'
		}

		compile "org.hdiv:hdiv-spring-mvc:$hdivVersion", {
			excludes 'commons-logging', 'hdiv-core', 'jsp-api', 'junit',
			         'servlet-api', 'spring-test', 'spring-webmvc'
		}

		compile "org.hdiv:hdiv-config:$hdivVersion", {
			excludes 'commons-logging', 'hdiv-core', 'hdiv-spring-mvc', 'junit',
			         'spring-test', 'spring-web'
		}

		runtime 'javax.servlet.jsp:jsp-api:2.1'
		runtime 'javax.servlet:jstl:1.2', {
			excludes 'jsp-api'
		}
		compile "org.hdiv:hdiv-jstl-taglibs-1.2:$hdivVersion", {
			excludes 'commons-logging', 'hdiv-core', 'jsp-api', 'jstl', 'junit', 'servlet-api'
		}

		compile 'taglibs:standard:1.1.2'
	}

	plugins {
		build ':release:2.2.1', ':rest-client-builder:1.0.3', {
			export = false
		}
	}
}
