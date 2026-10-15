# cs2114-project1-group82
A bank / ATM simulator for transactions and withdrawals

# The Hokie ATM

The Hokie ATM is a Java Swing application (a small window, not a console
program) that simulates basic ATM operations, including account login,
deposits, withdrawals, transaction history, and withdrawal-limit validation.
Stretch features: password double-verification and suspicious-activity alerts.

## How to Run

1. Clone the repository with GitHub Desktop.
2. In Eclipse, select **File > Import > Existing Projects into Workspace**.
3. Select the cloned project folder and finish the import.
4. Make sure the Problems tab (Window > Show View > Problems) shows no errors.
5. In the Package Explorer, expand `src/cashclash`.
6. Right-click `Main.java` and select **Run As > Java Application**.

The main class is `cashclash.Main`. A checked-in Eclipse launch configuration is
also available in the project root; double-click `cashclash.Main.launch` if you
want to run it directly.

Demo login: account number `12345` (no password). Use "Create account" to make
more accounts; account numbers are exactly 5 digits.

## Running Tests

1. Expand the `test/cashclash` package in Eclipse.
2. Right-click the `test` folder (all tests) or one test class.
3. Select Run As > JUnit Test (JUnit 5).

## Requirements

- Java 17 or newer
- Eclipse IDE
- JUnit 5
