hdiv {

	validatorFilterUrlPattern = '/*'

	config {
		errorPage = '/error.jsp'
		excludedExtensions = ''
		confidentiality = true
		id = 'config'
		avoidCookiesIntegrity = false
		avoidCookiesConfidentiality = false
		avoidValidationInUrlsWithoutParams = false
		strategy = 'memory' // or cipher or hash
		randomName = false
		protectedExtensions = '.*'
		userData = ''
		debugMode = false
		maxPagesPerSession = 5
		startPages = ''
		startParameters = ''
		paramsWithoutValidation = []
	}

	validations = []

	editableValidations = []
}
