package grails.plugin.hdiv;

import org.grails.databinding.errors.BindingError;
import org.grails.databinding.events.DataBindingListener;
import org.hdiv.web.validator.EditableParameterValidator;
import org.springframework.validation.Errors;

/**
 * Wrapper to implement the DataBindingListener interface.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
public class GrailsEditableParameterValidator extends EditableParameterValidator implements DataBindingListener {

	public void afterBinding(Object target, Object errors) {
		validate(target, (Errors) errors);
	}

	public Boolean beforeBinding(Object target, Object errors) { return true; }
	public Boolean beforeBinding(Object target, String propertyName, Object value, Object errors) { return true; }
	public void afterBinding(Object target, String propertyName, Object errors) { /* no-op */ }
	public void bindingError(BindingError error, Object errors) { /* no-op */ }
}
