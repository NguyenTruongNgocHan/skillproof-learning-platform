# Backend Module Boundaries

## 1. Module Structure

The backend is organized as a modular monolith with the following modules:

- `identity` - Owns user identity, authentication, authorization, and roles.
- `organization` - Owns organizers, organizations, and organization management.
- `learning` - Owns courses, learning paths, enrollments, and learning progress.
- `quiz` - Owns questions, quizzes, attempts, and assessments.
- `realtime` - Owns 1v1 realtime sessions, WebSocket communication, and server-authoritative state.
- `community` - Owns discussion and community interactions.
- `recommendation` - Owns content-based recommendation logic.
- `certification` - Owns certificate issuance and certificate lifecycle.
- `blockchain` - Owns the blockchain-based certificate verification layer only.

## 2. Ownership Rule

Each module owns its domain objects, business rules, repositories, and internal implementation details.

A module must not directly access another module's internal repositories, entities, or implementation classes.

## 3. Dependency Rule

Modules may depend on another module only through an explicitly defined module API, service, interface, or contract.

Direct access to another module's repository or internal implementation is forbidden.

## 4. Forbidden Dependencies

The following patterns are forbidden:

- Accessing another module's repository directly.
- Importing another module's internal entity for business logic.
- Sharing persistence implementation details between modules.
- Creating circular module dependencies without an explicit architectural decision.

## 5. Certification and Blockchain Boundary

The `certification` module owns certificate business data and certificate lifecycle.

The `blockchain` module is limited to the blockchain-based verification layer.

Blockchain must not become the owner of certificate business data.

## 6. Inter-Module Communication

When modules need to communicate, the dependency should target a stable module API or contract rather than internal implementation details.

The concrete communication mechanism may be introduced later as the corresponding business use cases are implemented.

## 7. Architectural Goal

These boundaries are intended to keep the modular monolith maintainable, testable, and ready for future evolution without prematurely introducing distributed services.
