package com.grookage.leia.mux.processors;

import com.grookage.leia.models.mux.MessageRequest;
import com.grookage.leia.models.schema.SchemaDetails;
import com.grookage.leia.models.schema.transformer.TransformationTarget;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JsonRuleTargetValidator implements TargetValidator {

    @Override
    public boolean validate(TransformationTarget transformationTarget,
                            MessageRequest messageRequest,
                            SchemaDetails schemaDetails) {
        if (null == transformationTarget.getCriteria()) { // If there is null, treat this NO_CRITERIA, return true!
            return true;
        }
        return transformationTarget.getCriteria().evaluate(messageRequest.getMessage());
    }
}
