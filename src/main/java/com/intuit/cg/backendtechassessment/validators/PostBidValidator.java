package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.PostBidDetails;
import javax.ws.rs.WebApplicationException;

public class PostBidValidator implements ApiValidator<PostBidDetails> {
    @Override
    public void validate(PostBidDetails data) throws WebApplicationException {
        if (data == null) {
            throw ApiErrors.required("The body");
        } else if (data.getValue() == null) {
            throw ApiErrors.required("The value of the bid");
        } else if (data.getValue() <= 0) {
            throw ApiErrors.invalidBid();
        }
    }
}
