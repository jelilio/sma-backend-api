package io.github.jelilio.smbackend.commonutil.dto.response;

import io.github.jelilio.smbackend.commonutil.entity.enumeration.Action;
import io.github.jelilio.smbackend.commonutil.entity.enumeration.Severity;
import io.github.jelilio.smbackend.commonutil.utils.Tripple;

import java.util.List;

public record AnalysedObject (
    PostObject post,
    // <category, action>
    List<Tripple<String, Severity, Action>>results
) {

}
