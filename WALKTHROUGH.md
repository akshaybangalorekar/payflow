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

Q2. Predict: run the GET endpoint. Write down EVERY field in the JSON response
    you expect — before you look at the actual output. Then compare. Any field
    in the response that surprises you? Think about who is allowed to see it.

Q3. There are exactly three flaws in this code:
    - one about how money is represented (correctness),
    - one about what the API leaks (security),
    - one about a race condition (concurrency).
    Find all three. For each: the file, the line, why it's wrong, and what the
    consequence would be in production. Don't fix anything yet.

Q4. In application.yml, `ddl-auto: validate` and Flyway work together.
    In one sentence each: what does Flyway do? What does validate do?
    What would go wrong if we used ddl-auto: update instead?

Q5. Two requests arrive at the same millisecond, both creating an account for
    priya@example.com. Walk through the code and predict what happens.
    How would you prove it (not guess it)?

Rules of engagement: read the code in the reading order above. When you don't
know a class or annotation, look it up — but write down what you THOUGHT it
did first, then what it actually does. That gap is where the learning is.
