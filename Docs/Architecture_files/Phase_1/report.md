# Phase 1
## Implemented Requirements
Since we are with only 2 students, we decided to implement 2/3 of the requirements, except for requirement 3, that was completly implemented. Here is a list of the implemented requirements:
1. Persisting data in different data models (e.g. relational, document) and SGBD:
    - Relacional data model: e.g. H2, MySQL, SQL Server
    - Document data model (NoSQL): e.g. MongoDB, CouchDB
   
2. Adopting different IAM (Identity and Access Management) providers:
    - Google
    - Facebook
   
3. Generating Lending and Authors ID in different formats according to the following specifications:
    - 24 hexadecimal characters
    - 20 alphanumeric characters as business entity business id hash
    - Integer incremental (NB: it should be independent from the Ids of the database)
   
4. Recommending Lendings according to following specifications:
    - X books most lent from the Y most lent genre
    - Based on the age of the reader:
      - age<10: X books of genre “children”
      - 10=<age<18: X books of genre “juvenile”
      - 18=<age: X books of the most lent genre of the reader
      - NB: preferably, 10 and 18 would be configurable.

## Kanban board
| Not addressed                                                                               | Work-in-progress   | Addressed | Tested | Completed |
|---------------------------------------------------------------------------------------------|--------------------|-----------|--------|-----------|
|                                                                                             | MySQL server setup |           |        |           |
| MySQL DB implementation                                                                     |                    |           |        |           |
| Mongo DB setup                                                                              |                    |           |        |           |
| Mongo DB implementation                                                                     |                    |           |        |           |
| Google IAM                                                                                  |                    |           |        |           |
| Facebook IAM                                                                                |                    |           |        |           |
| Frontend login UI                                                                           |                    |           |        |           |
| Lending and Authors ID:<br/> 24 hexadecimal characters                                      |                    |           |        |           |
| Lending and Authors ID:<br/> 20 alphanumeric characters as business entity business id hash |                    |           |        |           |
| Integer Incremental                                                                         |                    |           |        |           |
| Recommending Lendings: X books most lent from the Y most lent genre                         |                    |           |        |           |
| Recommending Lendings: age<10: X books of genre “children”                                  |                    |           |        |           |
| Recommending Lendings: 10=<age<18: X books of genre “juvenile”                              |                    |           |        |           |
| Recommending Lendings: 18=<age: X books of the most lent genre of the reader                |                    |           |        |           |
| Recommending Lendings: NB: preferably, 10 and 18 would be configurable.                     |                    |           |        |           |
|                                                                                             | Write report       |           |        |           |




## Attribute-Driven Design (ADD)