package com.intuit.cg.backendtechassessment.validators;

import com.intuit.cg.backendtechassessment.exceptions.ApiErrors;
import com.intuit.cg.backendtechassessment.models.CreateProjectDetails;
import java.time.OffsetDateTime;
import javax.ws.rs.WebApplicationException;
import org.apache.commons.lang3.StringUtils;

public class CreateProjectValidator implements ApiValidator<CreateProjectDetails> {
    @Override
    public void validate(CreateProjectDetails data) throws WebApplicationException {
        if (data == null) {
            throw ApiErrors.required("The request body");
        } else if (data.getClosingBidTime() == null || data.getClosingBidTime().isBefore(OffsetDateTime.now())) {
            // TODO We should probably require the closing time to be at least X hours/days in the future.
            throw ApiErrors.invalidClosingBidTime();
        } else if (StringUtils.isEmpty(data.getName())) {
            throw ApiErrors.required("The name");
        } else if (StringUtils.isEmpty(data.getDescription())) {
            throw ApiErrors.required("The description");
        } else if (data.getBudget() == null) {
            throw ApiErrors.required("The budget");
        } else if (data.getBudget() <= 0) {
            throw ApiErrors.invalidBudget();
        }
    }
}
