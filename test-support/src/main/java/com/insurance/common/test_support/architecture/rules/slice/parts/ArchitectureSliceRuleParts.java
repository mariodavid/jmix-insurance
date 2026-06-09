package com.insurance.common.test_support.architecture.rules.slice.parts;

import com.insurance.common.test_support.architecture.project.ArchitectureSlice;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import java.util.Arrays;

/** Predicates and rule parts for validating slices and their dependencies. */
public final class ArchitectureSliceRuleParts {

  private ArchitectureSliceRuleParts() {}

  /**
   * Creates a predicate that permits dependencies only to the current slice, whitelisted slices, or
   * packages outside all defined business slices.
   */
  public static DescribedPredicate<JavaClass> resideInAllowedSlices(
      ArchitectureSlice currentSlice, ArchitectureSlice... allowedSlices) {
    return DescribedPredicate.describe(
        "reside in allowed slices ("
            + currentSlice
            + " and "
            + Arrays.toString(allowedSlices)
            + ")",
        javaClass ->
            !residesInAnySlice(javaClass)
                || residesInSlice(javaClass, currentSlice)
                || Arrays.stream(allowedSlices)
                    .anyMatch(allowed -> residesInSlice(javaClass, allowed)));
  }

  /** Checks if a class resides in any of the defined business slices. */
  public static boolean residesInAnySlice(JavaClass javaClass) {
    return Arrays.stream(ArchitectureSlice.values())
        .anyMatch(slice -> residesInSlice(javaClass, slice));
  }

  /** Checks if a class resides in a specific business slice. */
  public static boolean residesInSlice(JavaClass javaClass, ArchitectureSlice slice) {
    String pkg = javaClass.getPackageName();
    String prefix = slice.packagePrefix();
    return pkg.equals(prefix) || pkg.startsWith(prefix + ".");
  }
}
