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

_____________________________________________________
Assessment of my answers

This is a solid first review, Akshay — you did the real work, used the debugger, and found two of three flaws with genuine understanding. Now I'm going to review it the way a senior engineer would, because the gaps are as valuable as the hits.

**Q1 — the trace.** What you've captured is real and better than reading: those framework frames (`DispatcherServlet.doDispatch`, `InvocableHandlerMethod`, `DirectMethodHandleAccessor`) are the *debugger's call stack* — you watched the request happen. Two corrections:

*Shape:* you've listed it top-down, but the second half of your list is actually the bottom of the stack. The true order is: `HttpServlet.service` → `FrameworkServlet` → `DispatcherServlet.doDispatch` → `RequestMappingHandlerAdapter` → **reflection lands on your method** → `accountController.create` → `accountService.create` → `existsByEmail` (a SELECT) → `Account` constructor → `save` (an INSERT) → response serialized to JSON → 201. 

*Missing pieces:* two invisible steps do real work around your code. **Jackson deserialization** — the JSON string in your curl becomes a `CreateAccountRequest` object *before* your method runs. **`@Valid`** — runs after deserialization but *before* your method body; if validation fails, your controller never executes at all. That's why `@NotBlank`/`@Email` live on the DTO, not in your method. And one detail you saw without naming it: `DirectMethodHandleAccessor` means Spring invokes your controller **via reflection** — the framework doesn't know your class at compile time, it discovers it. Keep that mental image; it explains a lot of Spring's "magic."

**Q2 — good, with one telling miss.** You predicted `internalNotes` would be there — from reading the code, fair enough. But look at your access-control table: you wrote six rules about *who may see what*… and the API enforces **none of them**. Zero. There's no authentication at all, and no per-audience response shape. That table in your head — customer view vs. bank-employee view — is exactly what DTO-per-audience exists to implement, and it's the bridge to your second flaw.

**Q3 — the verdict:**

*Flaw 1 (BigDecimal) — found, and articulated correctly.* You even nailed the reason: values constructed from strings are exact, no binary rounding baked in. One extension: when you fix this, note it lives in **three places** — the DTO (`Double openingBalance`), the entity (`double balance`), and the migration (`DOUBLE PRECISION`). And here's the discipline rule: you **never edit V1** once it's run (Flyway recorded it in `flyway_schema_history` — you watched that table get created in your own boot log). You write a `V2__*.sql`. That's Q4's lesson landing in practice.

*Flaw 2 (the leak) — right idea, sharpen the mechanism.* Your instinct is correct: `notes`/`status` shouldn't reach customers. The *mechanism* is precise: the controller returns the **entity**, and Jackson dutifully serializes *every getter* — including `getInternalNotes()`. Fix: return a DTO containing exactly what each audience may see. One correction to your "encrypt all customer data" point: encryption-in-transit is a *different layer* — that's TLS, Dvara's job in Stage S. Here the problem isn't that the data is readable on the wire; it's that **fields are being sent to the wrong audience at all**. Two different diseases, two different cures. And complete your own review: fill in the line number you left blank.

*Flaw 3 — misdiagnosed, and it's the best teaching moment of the day.* Your semaphore idea treats the symptom at the wrong layer, and it would fail twice in production: once immediately (you'd serialize *all* account creation globally — every customer waits behind every other), and once subtly (the moment we run two instances of the service for redundancy, a semaphore in one JVM sees nothing of the other JVM's request). A JVM lock cannot guard data that lives outside the JVM. Hold that thought.

**Q5 — your prediction is where the flaw hides, so test it.** You wrote: "one call which is ahead will go through, the other will get stuck at `existsByEmail` and throw." Sometimes true — if the second request's check happens *after* the first one's insert commits. But the race is the other interleaving: **both** requests execute `existsByEmail` *before* either inserts → both see `false` → both insert → **two rows, same email**. Run the experiment from earlier — two backgrounded curls with the same email, then the `docker exec` psql query, several times — and count rows. Report what you see.

Then, one guiding question, no answer: when both requests race toward that table at the same millisecond, **who is the only participant that can see both of them?** Not the controller, not the service, not any JVM. Think about who is physically capable of witnessing the conflict… and what the database could have been told to refuse.

Fix plan, in order: (1) commit your `notes/walkthrough-answers.md` as-is, warts and all — the reasoning trail is the artifact; (2) run the race experiment and bring me the psql output; (3) then you fix flaws 1 and 2 yourself, in your own commit, and I'll review like a real PR. Flaw 3's fix waits until you've seen the smoking gun.
