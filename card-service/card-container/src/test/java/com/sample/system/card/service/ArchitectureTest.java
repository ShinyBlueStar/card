package com.sample.system.card.service;

import com.sample.system.card.service.application.rest.BaseController;
import com.sample.system.card.service.domain.ports.input.service.BaseService;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;

/**
 * Architecture rules for the whole service (all modules are on this module's classpath).
 */
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.sample.system.card.service");
    }

    @Test
    void domainDoesNotDependOnOuterLayers() {
        noClasses().that().resideInAPackage("..service.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..service.dataaccess..", "..service.application..")
                .because("the domain and its ports must not know about persistence or the web layer")
                .check(classes);
    }

    @Test
    void dataAccessDoesNotDependOnWebLayer() {
        noClasses().that().resideInAPackage("..service.dataaccess..")
                .should().dependOnClassesThat().resideInAPackage("..service.application..")
                .check(classes);
    }

    @Test
    void jpaEntitiesLiveInDataAccess() {
        classes().that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..service.dataaccess..")
                .check(classes);
    }

    @Test
    void controllersExtendBaseController() {
        classes().that().areAnnotatedWith(RestController.class)
                .should().beAssignableTo(BaseController.class)
                .check(classes);
    }

    @Test
    void serviceInterfacesExtendBaseService() {
        classes().that().resideInAPackage("..domain.ports.input.service")
                .and().areInterfaces()
                .and().haveSimpleNameEndingWith("Service")
                .should().beAssignableTo(BaseService.class)
                .check(classes);
    }

    @Test
    void constructorInjectionOnly() {
        noFields().that().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("MapperImpl")
                .and().areDeclaredInClassesThat().haveSimpleNameNotEndingWith("MappingsImpl")
                .should().beAnnotatedWith(Autowired.class)
                .because("dependencies should be final and injected through the constructor (MapStruct output excluded)")
                .check(classes);
    }

    @Test
    void noSystemOutOrJavaUtilLogging() {
        NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(classes);
        NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(classes);
    }
}
