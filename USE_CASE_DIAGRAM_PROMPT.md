# Prompt: Generate a Correct UML Use Case Diagram for TechMateGod

Use this prompt in ChatGPT, Claude, Gemini, or any UML assistant.

---

You are a senior software architect.
Analyze the **TechMateGod** repository (Spring Boot attendance management system) and generate a **UML Use Case Diagram** (not a class diagram and not a DFD).

## Objective
Create a standards-compliant use case model that captures user goals and system responsibilities.

## Important UML rules
- Use **actors** as stick-figure roles (external to the system).
- Use **use cases** as oval names that represent user goals (verb phrases).
- Draw a **system boundary** named `TechMateGod System`.
- Do **not** place method names like `processLogin()` or `handleStudents()` inside rectangular classes.
- Do **not** model data stores in the use case diagram.

## Scope to include
### Primary actor
- **Teacher**

### Supporting external actor
- **Google OAuth Provider**

### Main use cases (inside system boundary)
1. Log in with Google
2. View dashboard
3. Create subject
4. View subject details
5. Delete subject
6. Add student manually
7. Import students (CSV/Excel)
8. Search student
9. Delete student
10. View attendance by date
11. Mark/update attendance
12. Import attendance (CSV/Excel)
13. Export attendance (CSV/Excel)
14. View subject analytics
15. Log out

## Relationship guidance
- `Log in with Google` should associate with both **Teacher** and **Google OAuth Provider**.
- Use `<<include>>` where behavior is always reused (for example, ownership/auth checks).
- Use `<<extend>>` only for optional/conditional behavior.
- Keep actor-to-use-case links simple and readable.

## Required outputs
1. A brief textual explanation of actor/use case choices.
2. A PlantUML use case diagram block.
3. A Mermaid `flowchart LR` approximation (optional fallback if PlantUML is unavailable).
4. A short note listing assumptions and out-of-scope items.

## Quality checklist
Before finalizing, verify:
- Every use case is written as a user goal (verb phrase).
- No class-diagram elements (attributes/method lists) appear.
- System boundary is present and correctly named.
- External authentication is represented as an external actor interaction.
