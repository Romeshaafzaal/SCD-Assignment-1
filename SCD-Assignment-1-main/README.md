# Campus Management System (SE3005 Assignment 1)

## Run
Requires JDK 17 or newer (no preview features).
- Linux/Mac: `./run.sh`   Windows: `run.bat`   IntelliJ: run `Main`.
- Tests: `./run_tests.sh` (PersonBTest + AcademicPersistenceTest, temporary folders only).

## Data and logs
- `data/` holds every .txt file the application uses:
  users, courses, sections, enrollments, attendance (academic core) and
  assignments, submissions, requests, fypgroups (workflow).
- First run only: if `data/` has no saved academic records, sample data is created and saved.
  After that the saved files are loaded. **To reset to the sample data, delete the .txt files in `data/`.**
- `logs/campus_log.txt` is appended on every run (loading saved data does not re-log old events).
- Records that cannot be linked or parsed are skipped with a log warning but kept in the file.

## Menus
Pick a role, then a person. Sample users: students F001-F005, TAs (Omar S1, Zainab S4),
permanent instructors T001/T002, visiting instructor T003, admin A001.
Only a Permanent Instructor gets "Assign Teaching Assistant" and all FYP options.

## Assignment Compliance & Phase 3 Updates
- **FYP Group Size**: Unrestricted group size (supports 1..* members as specified in PDF UML diagram Figure 6).
- **Late Submissions**: Late submissions past deadline are accepted and correctly flagged with status `LATE` without an arbitrary cutoff.
- **FYP Evaluation Score**: Accepts any valid non-negative score without artificial cap.
- **Validation Rules**: Mandatory validations (null checks, duplicate prevention, timetable clash detection, room capacity, course credit hours 1-6, role authorization) are strictly enforced.
- **Exception Hierarchy**: Base exceptions (`CampusException`, `CourseException`, `AssessmentException`, `RequestException`, `FYPException`, `UserException`) are abstract as shown in Figure 8; concrete subclasses are thrown for specific errors.
