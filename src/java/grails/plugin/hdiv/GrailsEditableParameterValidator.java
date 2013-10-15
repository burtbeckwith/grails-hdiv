package grails.plugin.hdiv;

import org.codehaus.groovy.grails.web.binding.DataBindingListener2;
import org.grails.databinding.errors.BindingError;
import org.hdiv.web.validator.EditableParameterValidator;
import org.springframework.validation.BindingResult;

/**
 * Wrapper to implement the DataBindingListener2 interface.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsEditableParameterValidator extends EditableParameterValidator implements DataBindingListener2 {

	public void afterBinding(Object target, BindingResult errors) {
		validate(target, errors);
	}

	public Boolean beforeBinding(Object target, BindingResult errors) { return true; }
	public Boolean beforeBinding(Object target, String propertyName, Object value) { return true; }
	public void afterBinding(Object target, String propertyName) { /* no-op */ }
	public void bindingError(BindingError error) { /* no-op */ }
}
