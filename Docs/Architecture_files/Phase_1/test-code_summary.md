# Test code summary

## All tests & test category:

| Test name                        | Test Category                                      |
|----------------------------------|----------------------------------------------------|
| AllTestsSuite                    | Test suite (aggregator)                            |
| AuthApiTest                      | Unit test — API/controller (Mockito)               |
| OAuthAuthenticationProvidersTest | Unit test — auth handler/config (Mockito)          |
| AuthorControllerIntegrationTest  | Integration test — controller slice                |
| AuthorRepositoryIntegrationTest  | Integration test — repository slice                |
| AuthorServiceImplIntegrationTest | Integration test — service (SpringBootTest, DB)    |
| AuthorTest                       | Unit test — opaque-box (domain)                    |
| BioTest                          | Unit test — opaque-box (domain)                    |
| BirthDateTest                    | Unit test — transparent-box (white-box, domain)    |
| BookTest                         | Unit test — opaque-box (domain)                    |
| DescriptionTest                  | Unit test — opaque-box (domain)                    |
| GenreTest                        | Unit test — opaque-box (domain)                    |
| IsbnTest                         | Unit test — opaque-box (domain)                    |
| JsonHelper                       | Test support (config/util)                         |
| LendingNumberTest                | Unit test — opaque-box (domain)                    |
| LendingRepositoryIntegrationTest | Integration test — repository (SpringBootTest, DB) |
| LendingServiceImplTest           | Integration test — service (SpringBootTest, DB)    |
| LendingTest                      | Unit test — opaque-box (domain)                    |
| NameTest                         | Unit test — opaque-box (domain)                    |
| PhoneNumberTest                  | Unit test — opaque-box (domain)                    |
| PhotoTest                        | Unit test — opaque-box (domain)                    |
| PsoftG1ApplicationTests          | Integration test — application (context load)      |
| ReaderTest                       | Unit test — opaque-box (domain)                    |
| TestSecurityBeansConfig          | Test support (config/util)                         |
| TestSecurityConfig               | Test support (config/util)                         |
| TitleTest                        | Unit test — opaque-box (domain)                    |
| UserTestDataFactory              | Test support (config/util)                         |
| AgeRecommendationMutationTest    | Mutation test                                      |


## Number of tests per category

| Test Category                                                    | Count |
|------------------------------------------------------------------|------:|
| Test suite (aggregator)                                          |     1 |
| Unit test — black-box (opaque-box, domain + API/controller/auth) |    13 |
| Unit test — white-box (transparent-box, domain)                  |     1 |
| Integration test                                                 |     6 |
| Mutation test                                                    |     1 |
| Test support (config/util)                                       |     4 |

**Total tests files:** 22

**Total tests:** 111 (All passing successfully)

