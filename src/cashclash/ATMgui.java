package cashclash;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** A simple Swing front end. All banking logic stays in ATM / Account. */
public class ATMGui extends JFrame {

    private static final String LOGIN = "login";
    private static final String SESSION = "session";
    private static final Color OK = new Color(0, 110, 40);
    private static final Color BAD = new Color(180, 0, 0);

    private final ATM atm;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // login screen
    private final JTextField accountField = new JTextField(12);
    private final JPasswordField passwordField = new JPasswordField(12);
    private final JLabel loginStatus = new JLabel(" ");

    // session screen
    private final JLabel headerLabel = new JLabel();
    private final JLabel alertLabel = new JLabel(" ");
    private final JTextArea summaryArea = new JTextArea(4, 24);
    private final JTextField amountField = new JTextField(10);
    private final JComboBox<String> typeBox =
        new JComboBox<>(new String[] {Account.CHECKING, Account.SAVINGS});
    private final JTextArea historyArea = new JTextArea();
    private final JLabel sessionStatus = new JLabel(" ");

    public ATMGui(ATM atm) {
        super("Cash Clash ATM");
        this.atm = atm;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        root.add(buildLoginPanel(), LOGIN);
        root.add(buildSessionPanel(), SESSION);
        setContentPane(root);
        setSize(760, 470);
        setLocationRelativeTo(null);
        cards.show(root, LOGIN);
    }

    // ---------- screen construction ----------

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        JLabel title = new JLabel("Cash Clash ATM");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        p.add(title, c);

        c.gridwidth = 1;
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;
        p.add(new JLabel("Account number:"), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        p.add(accountField, c);

        if (atm.isSecurityEnabled()) {
            c.gridx = 0;
            c.gridy = 2;
            c.anchor = GridBagConstraints.EAST;
            p.add(new JLabel("Password (if set):"), c);
            c.gridx = 1;
            c.anchor = GridBagConstraints.WEST;
            p.add(passwordField, c);
        }

        JButton loginButton = new JButton("Log in");
        JButton createButton = new JButton("Create account");
        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.add(loginButton);
        buttons.add(createButton);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        p.add(buttons, c);

        c.gridy = 4;
        p.add(loginStatus, c);
        c.gridy = 5;
        p.add(new JLabel("Demo account: 12345 (no password)"), c);

        loginButton.addActionListener(e -> doLogin());
        accountField.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        createButton.addActionListener(e -> doCreateAccount());
        getRootPane().setDefaultButton(loginButton);
        return p;
    }

    private JPanel buildSessionPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // header + alert banner
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18f));
        alertLabel.setForeground(BAD);
        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(e -> doLogout());
        JPanel top = new JPanel(new BorderLayout());
        top.add(headerLabel, BorderLayout.WEST);
        top.add(logoutButton, BorderLayout.EAST);
        JPanel north = new JPanel(new GridLayout(0, 1));
        north.add(top);
        north.add(alertLabel);
        p.add(north, BorderLayout.NORTH);

        // left column: balances + deposit/withdraw form
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createTitledBorder("Balances"));

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        depositButton.addActionListener(e -> doTransaction(true));
        withdrawButton.addActionListener(e -> doTransaction(false));
        amountField.addActionListener(e -> doTransaction(true));
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonRow.add(depositButton);
        buttonRow.add(withdrawButton);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel(atm.isCurrencyEnabled()
            ? "Amount (e.g. 25.50 or 20 EUR)" : "Amount in USD (e.g. 25.50)"));
        form.add(amountField);
        form.add(typeBox);
        form.add(buttonRow);
        JPanel formWrap = new JPanel(new BorderLayout());
        formWrap.add(form, BorderLayout.NORTH);

        JPanel west = new JPanel(new BorderLayout(0, 10));
        west.add(summaryScroll, BorderLayout.NORTH);
        west.add(formWrap, BorderLayout.CENTER);
        p.add(west, BorderLayout.WEST);

        // center: history
        historyArea.setEditable(false);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Transaction history"));
        p.add(historyScroll, BorderLayout.CENTER);

        p.add(sessionStatus, BorderLayout.SOUTH);
        return p;
    }

    // ---------- actions ----------

    private void doLogin() {
        String number = accountField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            if (atm.authenticate(number, password.isEmpty() ? null : password)) {
                headerLabel.setText("Account " + atm.getCurrentAccount().getAccountNumber());
                accountField.setText("");
                passwordField.setText("");
                amountField.setText("");
                sessionStatus.setText(" ");
                refresh();
                cards.show(root, SESSION);
            } else {
                setStatus(loginStatus, "Incorrect or missing password.", false);
            }
        } catch (AccountNotFoundException ex) {
            setStatus(loginStatus, ex.getMessage() + " Use \"Create account\" to open one.", false);
        }
    }

    private void doLogout() {
        atm.logout();
        setStatus(loginStatus, "Logged out.", true);
        cards.show(root, LOGIN);
    }

    private void doTransaction(boolean isDeposit) {
        String type = (String) typeBox.getSelectedItem();
        try {
            long cents = atm.parseAmount(amountField.getText());
            String message = isDeposit
                ? atm.handleDeposit(cents, type)
                : atm.handleWithdrawal(cents, type);
            amountField.setText("");
            refresh();
            setStatus(sessionStatus, message, true);
        } catch (InvalidAmountException | UnsupportedCurrencyException
                 | InsufficientFundsException | DailyLimitExceededException ex) {
            refresh();
            setStatus(sessionStatus, ex.getMessage(), false);
        } catch (IllegalStateException ex) {
            // session ended unexpectedly: send the user back to log in
            setStatus(loginStatus, ex.getMessage(), false);
            cards.show(root, LOGIN);
        }
    }

    private void doCreateAccount() {
        JTextField number = new JTextField(10);
        JTextField initial = new JTextField(10);
        JPasswordField password = new JPasswordField(10);
        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Account number (5 digits):"));
        form.add(number);
        form.add(new JLabel("Initial deposit (optional):"));
        form.add(initial);
        if (atm.isSecurityEnabled()) {
            form.add(new JLabel("Password (optional):"));
            form.add(password);
        }
        int choice = JOptionPane.showConfirmDialog(this, form, "Create account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            String accountNumber = number.getText().trim();
            if (!InputValidator.isValidAccountNumber(accountNumber)) {
                setStatus(loginStatus, "Account numbers must be exactly 5 digits.", false);
                return;
            }
            long cents = initial.getText().trim().isEmpty() ? 0 : atm.parseAmount(initial.getText());
            atm.createAccount(accountNumber, cents, new String(password.getPassword()));
            accountField.setText(accountNumber);
            setStatus(loginStatus, "Account " + accountNumber + " created. You can log in now.", true);
        } catch (InvalidAmountException | UnsupportedCurrencyException | DuplicateAccountException ex) {
            setStatus(loginStatus, ex.getMessage(), false);
        }
    }

    // ---------- helpers ----------

    private void refresh() {
        summaryArea.setText(atm.showAccountSummary());
        historyArea.setText(atm.showHistory());
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
        alertLabel.setText(atm.isCurrentAccountFlagged()
            ? "WARNING: this account is flagged for repeated suspicious activity." : " ");
    }

    private static void setStatus(JLabel label, String message, boolean ok) {
        label.setForeground(ok ? OK : BAD);
        label.setText(message);
    }
}