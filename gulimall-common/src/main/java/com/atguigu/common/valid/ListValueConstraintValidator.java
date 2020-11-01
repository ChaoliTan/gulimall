package com.atguigu.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Self-defined annotation validator
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> set = new HashSet<>();

    // Initialization method
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    // Decide whether validate success
    /**
     *
     * @param value : need validate
     * @param constraintValidatorContext
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {

        return set.contains(value);
    }
}
