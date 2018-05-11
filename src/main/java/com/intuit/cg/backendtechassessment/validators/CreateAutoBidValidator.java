package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.CreateAutoBidDetails;
import javax.ws.rs.WebApplicationException;

public class CreateAutoBidValidator implements ApiValidator<CreateAutoBidDetails> {
    @Override
    public void validate(CreateAutoBidDetails data) throws WebApplicationException {
        if (data == null) {
            throw ApiErrors.required("The body");
        } else if (data.getMinimumBid() == null) {
            throw ApiErrors.required("The minimum bid");
        } else if (data.getStartingBid() == null) {
            throw ApiErrors.required("The starting bid");
        } else if (data.getMinimumBid() <= 0 || data.getStartingBid() <= 0 || data.getMinimumBid() > data.getStartingBid()) {
            // We should probably return customized message depending on the error like in CreateProjectValidator.
            throw ApiErrors.invalidBid();
        }
    }
}
