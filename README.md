# Name Sorter
This is a simple implementation of Name Sorter application using Java and Spring Boot.

---
## Assumptions (Behaviors to Expect)
- Duplicate names are allowed in the input and will be preserved in the output.
- Input names cases are not formatted and validated, the application will sort them as-is.
- Names are sorted in case-insensitive manner, i.e. possible order: a, A, B, b, ...
- Leading and trailing spaces are trimmed before sorting and will not be preserved in the output.
- On invalid name input, the application will log an error message and skip the invalid entry.

---
## Purposes

### 1. To demonstrate the implementation of Object-Oriented Principles

- Single Responsibility Principle:
  - CommandLineApplication: Responsible for application startup and command line argument handling.
  - FileContentSortingEvaluationService: Responsible for evaluating sorting algorithms based on file content
  - Name: Responsible for representing a name entity
  - IterativeBinarySearchTree: Responsible for binary search tree data structure implementation
  - Node: Responsible for representing a node in the binary search tree
  

- Open/Closed Principle:
    - BinaryTreeNameSortingService: Responsible for sorting names at insertion using a custom implementation of binary tree data structure
    - CollectionNameSortingService: Responsible for sorting names on demand using Java built-in Collections sorting


- Liskov Substitution Principle: 
  - FileContentSortingEvaluationService is the base type, 
  and BinaryTreeNameSortingService and CollectionNameSortingService are subtypes that can be used interchangeably,
  based on @ConditionalOnProperty configuration.


- Interface Segregation Principle: 
  -  FileContentSortingEvaluationService interface defines a method for evaluating sorting algorithms based on file content.


- Dependency Injection:
  - Spring Framework is used for dependency injection, allowing for loose coupling between components.
  - Sorting services are injected into the evaluation service via @Autowired annotation.


### 2. To demonstrate the use of build automation tools
- Gradle is used as the build automation tool to manage dependencies, build the project, and run tests.
- Gradle Wrapper is included to ensure consistent build environment across different machines.
- Gradle tasks are defined for building, testing (JUnit + Cucumber), and running the application.
- Gradle `pmd` plugin is used for code quality checks.
- Gradle configuration files are included to define project settings and dependencies.
- GitHub Action `./.github/workflows/ci-pipeline.yml` trigger the build pipeline upon pushing changes to both `main` and `dev` branches.  
- Test reports created in GitHub Action are uploaded as artifacts, such that developers may download them for inspection 

### 3. (Optional) To have some fun with data structures and algorithms

Leverage this coding exercise to explore the performance of self-implemented binary search tree is differ from the Java built-in Collections sorting
with various sizes of input data. Preserving duplicated entries also make it differ from Java built-in Tree Map(Key) / Set.
To switch between the two sorting implementations, change the property `app.service.type` in `src/main/resources/application.yml`
By default, the application is configured to use the self-implemented binary search tree `binaryTree`.


Sample performance metrics found in `logs/nameSorter.log` will look like: <br/>
########### START OF PERFORMANCE METRICS ###########<br/>
PERFORMANCE: Read 112104 names in 209 ms<br/>
PERFORMANCE: Wrote sorted names in 15 ms<br/>
PERFORMANCE: Entire Process completed in 229 ms<br/>
PERFORMANCE: JVM Memory Usage BEFORE - Used: 7 MB, Free: 100 MB, Total: 108 MB, Max: 8152 MB<br/>
PERFORMANCE: JVM Memory Usage AFTER - Used: 43 MB, Free: 268 MB, Total: 312 MB, Max: 8152 MB<br/>
########### END OF PERFORMANCE METRICS ###########<br/>

---
## Adjust Application Settings
You can adjust the log level and other settings in the `src/main/resources/application.yml` file

---
## How to Build
Pre-requisite: JDK 17 installed and have the root folder (for example only, jbr-17.0.14) configured as JAVA_HOME in your PATH.
And the build tool is Gradle.
```
./gradlew build
```
Test Cases: Test cases are implemented and will be executed during the build process.
Or run test cases only:
```
./gradlew test
```

---
## How to Run

Arguments below are optional, default values are located in the `src/main/resources/application.yml` file.
- app.input.file: default is files/unsorted-names-list.txt
- app.output.file: default is files/sorted-names-list.txt


- 1a. Run as gradle bootRun task:
```
./gradlew bootRun
```
- 1b. Or Optionally:
```
./gradlew bootRun --args="files/sorted-names-list.txt files/unsorted-names-list.txt"
```
- 2a. Run the jar file that has already been built directly, use the command below:
```
java -jar build/libs/NameSorter-0.0.1-SNAPSHOT.jar
```
- 2b. Or Optionally:
```
java -jar build/libs/NameSorter-0.0.1-SNAPSHOT.jar "files/sorted-names-list.txt" "files/unsorted-names-list.txt"
```

---
## Testing Notes
- Simple input validation tests `CommandLineApplicationTests` are created with Junit
- Behavioral Driven tests `NameSortingCore` are created with Cucumber (*.feature supported by *Steps.java)
- Cucumber tests covers all scenario examples in the features with the same single set of test steps, improve maintainability  
- Predefined test data files (input and expected results) are stored under `src/test/resources/files`
- Output directory is `files`


<br/>
