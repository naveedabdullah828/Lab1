package org.phase.one.lab1;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "org.phase.one.lab1",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule services_should_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..service..")
                    .should().dependOnClassesThat().resideInAPackage("..infra..");

    @ArchTest
    static final ArchRule packages_should_be_free_of_cycles =
            slices()
                    .matching("org.phase.one.lab1.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule services_must_be_stateless_with_final_fields =
            fields()
                    .that().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
                    .should().beFinal();
}