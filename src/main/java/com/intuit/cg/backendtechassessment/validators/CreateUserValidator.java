package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.CreateUserDetails;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;

public class CreateUserValidator implements ApiValidator<CreateUserDetails> {
    @Override
    public void validate(CreateUserDetails data) throws WebApplicationException {
        if (data == null) {
            throw ApiErrors.required("The request body");
        } else if (StringUtils.isEmpty(data.getName())) {
            throw ApiErrors.required("The name");
        } else if (StringUtils.isEmpty(data.getPassword())) {
            throw ApiErrors.required("The password");
        } else if (StringUtils.isEmpty(data.getConfirmationPassword())) {
            throw ApiErrors.required("The confirmation password");
        } else if (StringUtils.isEmpty(data.getEmail())) {
            throw ApiErrors.required("The email");
        } else if (!data.getPassword().equals(data.getConfirmationPassword())) {
            throw ApiErrors.passwordDoNotMatch();
        }
    }
}
