package pt.psoft.g1.psoftg1;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import pt.psoft.g1.psoftg1.auth.api.AuthApiTest;
import pt.psoft.g1.psoftg1.authormanagement.api.AuthorControllerIntegrationTest;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorIdGenerationFunctionsTest;
import pt.psoft.g1.psoftg1.authormanagement.model.AuthorTest;
import pt.psoft.g1.psoftg1.authormanagement.model.BioTest;
import pt.psoft.g1.psoftg1.authormanagement.repository.AuthorRepositoryIntegrationTest;
import pt.psoft.g1.psoftg1.authormanagement.services.AuthorServiceImplIntegrationTest;
import pt.psoft.g1.psoftg1.bookmanagement.model.*;
import pt.psoft.g1.psoftg1.configuration.OAuthAuthenticationProvidersTest;
import pt.psoft.g1.psoftg1.genremanagement.model.GenreTest;
import pt.psoft.g1.psoftg1.lendingmanagement.model.AgeRecommendationMutationTest;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingIdGenerationTest;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingNumberTest;
import pt.psoft.g1.psoftg1.lendingmanagement.model.LendingTest;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepositoryIntegrationTest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingServiceImplTest;
import pt.psoft.g1.psoftg1.readermanagement.model.BirthDateTest;
import pt.psoft.g1.psoftg1.readermanagement.model.PhoneNumberTest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderTest;
import pt.psoft.g1.psoftg1.shared.model.NameTest;
import pt.psoft.g1.psoftg1.shared.model.PhotoTest;
import pt.psoft.g1.psoftg1.testutils.JsonHelper;
import pt.psoft.g1.psoftg1.testutils.UserTestDataFactory;

@Suite
@SelectClasses({
        AuthApiTest.class,
        AuthorControllerIntegrationTest.class,
        AuthorTest.class,
        BioTest.class,
        AuthorRepositoryIntegrationTest.class,
        AuthorServiceImplIntegrationTest.class,
        BookTest.class,
        DescriptionTest.class,
        IsbnTest.class,
        TitleTest.class,
        GenreTest.class,
        LendingNumberTest.class,
        LendingTest.class,
        LendingRepositoryIntegrationTest.class,
        LendingServiceImplTest.class,
        BirthDateTest.class,
        PhoneNumberTest.class,
        ReaderTest.class,
        NameTest.class,
        PhotoTest.class,
        JsonHelper.class,
        UserTestDataFactory.class,
        BookRecommendationTest.class,
        TopBooksTest.class,
        //OAuthAuthenticationProvidersTest.class,
        AuthorIdGenerationFunctionsTest.class,
        LendingIdGenerationTest.class,
        AgeRecommendationMutationTest.class

})
public class AllTestsSuite {
}