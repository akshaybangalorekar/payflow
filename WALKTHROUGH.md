# PayFlow — Session 1 Reading Assignment: accounts service

This is starter code. It works, it runs, and it contains **exactly three planted flaws**.
Your job this session is NOT to fix anything — it is to read, trace, and hunt.

## Get it running

1. Start the database (from the repo root):
   docker compose -f docker/compose.yml up -d
2. Install Maven if you don't have it (one-time):
   brew install maven          (or download the binary tarball from maven.apache.org,
                                unzip it, and put its bin/ folder on your PATH)
   Verify with: mvn -version
3. Run the service (from `services/accounts`) — first run downloads the world; be patient:
   mvn spring-boot:run
4. Smoke test:
   curl -X POST http://localhost:8081/api/accounts \
     -H "Content-Type: application/json" \
     -d '{"customerName":"Priya Sharma","email":"priya@example.com","openingBalance":2500.0}'
5. Then fetch it with the id from the response:
   curl http://localhost:8081/api/accounts/<id>

## Reading order (follow the request, not the folders)

1. services/accounts/pom.xml        — what's on your classpath
2. AccountsApplication.java        — the three lines that boot Spring
3. AccountController.java          — the front door: routes in, responses out
4. CreateAccountRequest.java       — the contract for what a client may send
5. AccountService.java             — the business rules (thin today; they will grow)
6. Account.java                    — the entity: what we persist
7. AccountRepository.java          — one interface, zero implementation. Why?
8. V1__create_accounts.sql         — the source of truth for the schema
9. application.yml                 — port, datasource, ddl-auto: validate
10. GlobalExceptionHandler.java    — how errors become clean JSON, not stack traces

## Questions to answer BEFORE next session (write your answers down)

Q1. Trace the POST /api/accounts request through the code. List, in order,
    every class and method it touches, from HTTP request to SQL and back.

    accountController.create
    accountservice.create
    accountRepository.existsByEmail
    CreateAccountRequest.CreateAcccountRequest (@NotBlank, @Email)
    Account Constructor
    accountRepository.save(Account)
    RepositoryEntity.status
    Assert.notNull - http status should not be null
    DirectMethodHandleAccessor.invokeImpl case 1 with two parameters
    InvocableHandlerMethod.java.invokeForRequest
    ServletInvocableHandlerMethod.invokeAndHandle
    RequestMappingHandlerAdapter.invokeHandlerMethod
    AbstractHandlerMethodAdapter.handle()
    DispatcherServlet.doDispatch()
    FrameworkServlet.processRequest
    HttpServlet.service
    FrameworkServlet.service()

Q2. Predict: run the GET endpoint. Write down EVERY field in the JSON response
    you expect — before you look at the actual output. Then compare. Any field
    in the response that surprises you? Think about who is allowed to see it.

    The get should provide customer name, email, openingBalance, createdAt, ID, internalNotes, Status
    1. Notes - not the shown to the customer, only to bank employees
    2. Balance - only for customer and bank employee
    3. ID - only for customer and bank employee
    4. Email - only for customer and bank employee
    5. createdAt - not the shown to the customer, only to bank employees
    6. Status - not the shown to the customer, only to bank employees

Q3. There are exactly three flaws in this code:
    - one about how money is represented (correctness),
    - one about what the API leaks (security),
    - one about a race condition (concurrency).
    Find all three. For each: the file, the line, why it's wrong, and what the
    consequence would be in production. Don't fix anything yet.

    - Money should be represented by BigDecimal instead of Double as this will help in - every decimal value you construct from a String is represented precisely, with no silent binary rounding error baked in from the start. - in file CreateAccountReqeust.CreateAccountRequest() - line number 11
    - In get method it returns notes, status which should be only for bankers consumption and all customer data should be encrypted it is sent out just like that in plain text - in file AccountController - line number 
    - The create method in AccountController method should be a semaphore enabled so at a time only one caller can call the method. - file account controller - line 29

Q4. In application.yml, `ddl-auto: validate` and Flyway work together.
    In one sentence each: what does Flyway do? What does validate do?
    What would go wrong if we used ddl-auto: update instead?

    Flyway makes schema changes deliberately and keeps a version history; ddl-auto: validate makes zero changes and only confirms your code and the database still agree — using them together means schema drift gets caught at startup, loudly, instead of discovered mid-transaction in production. - I read this online.. didnt know anything about this. Had no background

Q5. Two requests arrive at the same millisecond, both creating an account for
    priya@example.com. Walk through the code and predict what happens.
    How would you prove it (not guess it)?

    If two requests come at the same time with priya@example.com then accountController.Create will be invoked -> this will invoke accountService.Create -> one call which is ahead will go thru but the other will get stuck at existsByEmail and throw exception


Rules of engagement: read the code in the reading order above. When you don't
know a class or annotation, look it up — but write down what you THOUGHT it
did first, then what it actually does. That gap is where the learning is.