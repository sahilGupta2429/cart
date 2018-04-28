package validators;

import org.apache.commons.validator.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import model.CustomerInfo;

@Component
public class CustomerInfoValidator implements Validator{
	private EmailValidator emailvalidator = EmailValidator.getInstance();
	

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz == CustomerInfo.class;
	}

	@Override
	public void validate(Object target, Errors errors) {

		CustomerInfo customerInfo = (CustomerInfo) target;
		
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name" , "NotEmpty.customerForm.name");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.customerForm.email");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.customerForm.address");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "NotEmpty.customerForm.phone");
		
		if(!emailvalidator.isValid(customerInfo.getEmail())) {
			errors.rejectValue("email", "Pattern.customerForm.email");
		}
	}
}
