# Prompt: Generate a Level-1 Data Flow Diagram (DFD) for TechMateGod

Use this prompt in ChatGPT, Gemini, Claude, or any architecture assistant to generate a **Level-1 DFD** for this repository.

---

You are a software architect. Analyze the **TechMateGod** project (Spring Boot attendance management app) and create a **Level-1 Data Flow Diagram (DFD)**.

## Application context
- The system is a teacher-focused platform with Google OAuth2 login.
- Main capabilities:
  1. Teacher authentication and dashboard access.
  2. Subject creation, listing, and deletion.
  3. Student management per subject (add, search, delete, bulk import).
  4. Attendance management per subject/date (view, save, bulk import).
  5. Attendance export (CSV/Excel) for a date range.
  6. Analytics per subject.

## Processes to model (Level-1)
Break the system into at least these processes:
1.0 **Authentication & Session Management**
2.0 **Subject Management**
3.0 **Student Management**
4.0 **Attendance Management**
5.0 **Reporting & Analytics**

## External entities
Include at minimum:
- **Teacher (User)**
- **OAuth Provider (Google)**

## Data stores
Include logical stores equivalent to:
- **D1 Teacher Store**
- **D2 Subject Store**
- **D3 Student Store**
- **D4 Attendance Store**

## Required outputs
1. A concise textual description of the Level-1 DFD.
2. A list of all data flows, formatted as:
   - `Source -> Flow Name -> Destination`
3. A **Mermaid diagram** (`flowchart LR`) showing entities, processes, data stores, and flows.
4. Assumptions section (if any behavior is inferred).

## Quality constraints
- Keep DFD notation consistent: entities, processes, data stores, and directional arrows.
- Do not mix UI components with DFD processes.
- Keep process names verb-first and business-focused.
- Ensure every process has at least one input and one output.
- Ensure every data store has at least one incoming and one outgoing flow.

## Optional enhancement
After the Level-1 DFD, suggest one improved Level-2 decomposition for process 4.0 (Attendance Management).

