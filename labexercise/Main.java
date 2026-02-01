

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

/**
 * Seminar Management System (Java Swing) - In-memory MVP
 * Roles: Student, Evaluator, Coordinator
 *
 * Run:
 *   javac -d out src/app/Main.java
 *   java -cp out app.Main
 */
public class Main {

    // ---------------------------
    // Main Boot
    // ---------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppContext ctx = new AppContext();
            seedDemoData(ctx);

            AppFrame frame = new AppFrame(ctx);
            frame.setVisible(true);
        });
    }

    private static void seedDemoData(AppContext ctx) {
        // Users
        ctx.userRepo.save(new Student("S1001", "Zarif", "Dr. Rahman"));
        ctx.userRepo.save(new Student("S1002", "Nafisa", "Dr. Karim"));

        ctx.userRepo.save(new Evaluator("E2001", "Eva One"));
        ctx.userRepo.save(new Evaluator("E2002", "Eva Two"));

        ctx.userRepo.save(new Coordinator("C3001", "Coord One"));

        // Example submission
        Submission sub = ctx.submissionService.register(
                "S1001",
                "AI for Seminar Scheduling",
                "We propose a simple heuristic to schedule seminar sessions.",
                "Dr. Rahman",
                PresentationType.ORAL,
                ""
        );

        // Example session + slots
        SeminarSession ses = ctx.sessionService.createSession(LocalDate.now().plusDays(3), "MMU Hall", PresentationType.ORAL);
        TimeSlot slot1 = ctx.sessionService.addSlot(ses.sessionId, LocalTime.of(10, 0), LocalTime.of(10, 15));
        TimeSlot slot2 = ctx.sessionService.addSlot(ses.sessionId, LocalTime.of(10, 15), LocalTime.of(10, 30));

        // Assign submission + evaluator
        ctx.assignmentService.assignSubmissionToSlot(ses.sessionId, slot1.slotId, sub.submissionId);
        ctx.assignmentService.assignEvaluatorToSlot(ses.sessionId, slot1.slotId, "E2001");
    }

    // ---------------------------
    // App Context (Repos + Services)
    // ---------------------------
    static class AppContext {
        final UserRepo userRepo = new UserRepo();
        final SubmissionRepo submissionRepo = new SubmissionRepo();
        final SessionRepo sessionRepo = new SessionRepo();
        final EvaluationRepo evaluationRepo = new EvaluationRepo();

        final AuthService authService = new AuthService(userRepo);
        final SubmissionService submissionService = new SubmissionService(submissionRepo, userRepo);
        final SessionService sessionService = new SessionService(sessionRepo);
        final AssignmentService assignmentService = new AssignmentService(sessionRepo, submissionRepo, userRepo);
        final EvaluationService evaluationService = new EvaluationService(evaluationRepo, sessionRepo, submissionRepo, userRepo);
        final ReportService reportService = new ReportService(sessionRepo, submissionRepo, evaluationRepo);
        final AwardService awardService = new AwardService(submissionRepo, evaluationRepo);
    }

    // ---------------------------
    // Models
    // ---------------------------
    enum Role { STUDENT, EVALUATOR, COORDINATOR }
    enum PresentationType { ORAL, POSTER }
    enum RubricCriterion { PROBLEM_CLARITY, METHODOLOGY, RESULTS, PRESENTATION }

    static abstract class User {
        final String userId;
        final String name;
        final Role role;

        User(String userId, String name, Role role) {
            this.userId = userId;
            this.name = name;
            this.role = role;
        }

        @Override public String toString() { return userId + " - " + name; }
    }

    static class Student extends User {
        final String supervisorName;
        Student(String id, String name, String supervisorName) {
            super(id, name, Role.STUDENT);
            this.supervisorName = supervisorName;
        }
    }

    static class Evaluator extends User {
        Evaluator(String id, String name) { super(id, name, Role.EVALUATOR); }
    }

    static class Coordinator extends User {
        Coordinator(String id, String name) { super(id, name, Role.COORDINATOR); }
    }

    static class Submission {
        final String submissionId;
        final String studentId;

        String title;
        String abstractText;
        String supervisorName;
        PresentationType preferredType;
        String materialPath;

        Submission(String submissionId, String studentId) {
            this.submissionId = submissionId;
            this.studentId = studentId;
        }

        @Override public String toString() { return submissionId + " - " + title; }
    }

    static class SeminarSession {
        final String sessionId;
        final LocalDate date;
        final String venue;
        final PresentationType type;

        final List<TimeSlot> slots = new ArrayList<>();

        SeminarSession(String sessionId, LocalDate date, String venue, PresentationType type) {
            this.sessionId = sessionId;
            this.date = date;
            this.venue = venue;
            this.type = type;
        }

        @Override public String toString() {
            return sessionId + " | " + date + " | " + venue + " | " + type;
        }
    }

    static class TimeSlot {
        final String slotId;
        final LocalTime start;
        final LocalTime end;

        String submissionId; // presenter
        final Set<String> evaluatorIds = new LinkedHashSet<>();

        TimeSlot(String slotId, LocalTime start, LocalTime end) {
            this.slotId = slotId;
            this.start = start;
            this.end = end;
        }

        @Override public String toString() {
            return slotId + " (" + start + " - " + end + ")";
        }
    }

    static class Evaluation {
        final String evaluationId;
        final String submissionId;
        final String evaluatorId;

        final EnumMap<RubricCriterion, Integer> scores = new EnumMap<>(RubricCriterion.class);
        String comments;

        Evaluation(String evaluationId, String submissionId, String evaluatorId) {
            this.evaluationId = evaluationId;
            this.submissionId = submissionId;
            this.evaluatorId = evaluatorId;
            // default scores
            for (RubricCriterion c : RubricCriterion.values()) scores.put(c, 0);
        }

        int total() {
            int sum = 0;
            for (int v : scores.values()) sum += v;
            return sum;
        }
    }

    // ---------------------------
    // Repositories (In-memory)
    // ---------------------------
    static class UserRepo {
        private final Map<String, User> map = new LinkedHashMap<>();
        void save(User u) { map.put(u.userId, u); }
        User find(String id) { return map.get(id); }
        List<User> all() { return new ArrayList<>(map.values()); }
        List<Evaluator> allEvaluators() {
            List<Evaluator> out = new ArrayList<>();
            for (User u : map.values()) if (u.role == Role.EVALUATOR) out.add((Evaluator) u);
            return out;
        }
    }

    static class SubmissionRepo {
        private final Map<String, Submission> map = new LinkedHashMap<>();
        void save(Submission s) { map.put(s.submissionId, s); }
        Submission find(String id) { return map.get(id); }
        List<Submission> all() { return new ArrayList<>(map.values()); }
        List<Submission> byStudent(String studentId) {
            List<Submission> out = new ArrayList<>();
            for (Submission s : map.values()) if (s.studentId.equals(studentId)) out.add(s);
            return out;
        }
    }

    static class SessionRepo {
        private final Map<String, SeminarSession> map = new LinkedHashMap<>();
        void save(SeminarSession s) { map.put(s.sessionId, s); }
        SeminarSession find(String id) { return map.get(id); }
        List<SeminarSession> all() { return new ArrayList<>(map.values()); }
    }

    static class EvaluationRepo {
        private final Map<String, Evaluation> map = new LinkedHashMap<>();
        void save(Evaluation e) { map.put(e.evaluationId, e); }
        Evaluation findBySubmissionAndEvaluator(String submissionId, String evaluatorId) {
            for (Evaluation e : map.values()) {
                if (e.submissionId.equals(submissionId) && e.evaluatorId.equals(evaluatorId)) return e;
            }
            return null;
        }
        List<Evaluation> bySubmission(String submissionId) {
            List<Evaluation> out = new ArrayList<>();
            for (Evaluation e : map.values()) if (e.submissionId.equals(submissionId)) out.add(e);
            return out;
        }
        List<Evaluation> all() { return new ArrayList<>(map.values()); }
    }

    // ---------------------------
    // Services
    // ---------------------------
    static class AuthService {
        private final UserRepo userRepo;
        AuthService(UserRepo userRepo) { this.userRepo = userRepo; }

        User login(String userId) {
            User u = userRepo.find(userId);
            if (u == null) throw new IllegalArgumentException("User not found: " + userId);
            return u;
        }
    }

    static class SubmissionService {
        private final SubmissionRepo submissionRepo;
        private final UserRepo userRepo;
        SubmissionService(SubmissionRepo submissionRepo, UserRepo userRepo) {
            this.submissionRepo = submissionRepo;
            this.userRepo = userRepo;
        }

        Submission register(String studentId, String title, String abstractText,
                            String supervisor, PresentationType type, String materialPath) {
            User u = userRepo.find(studentId);
            if (u == null || u.role != Role.STUDENT) throw new IllegalArgumentException("Invalid student: " + studentId);

            if (title == null || title.isBlank()) throw new IllegalArgumentException("Title required");
            if (abstractText == null || abstractText.isBlank()) throw new IllegalArgumentException("Abstract required");
            if (supervisor == null || supervisor.isBlank()) throw new IllegalArgumentException("Supervisor required");
            if (type == null) throw new IllegalArgumentException("Type required");

            String id = "SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Submission s = new Submission(id, studentId);
            s.title = title.trim();
            s.abstractText = abstractText.trim();
            s.supervisorName = supervisor.trim();
            s.preferredType = type;
            s.materialPath = materialPath == null ? "" : materialPath.trim();

            submissionRepo.save(s);
            return s;
        }
    }

    static class SessionService {
        private final SessionRepo sessionRepo;
        SessionService(SessionRepo sessionRepo) { this.sessionRepo = sessionRepo; }

        SeminarSession createSession(LocalDate date, String venue, PresentationType type) {
            if (date == null) throw new IllegalArgumentException("Date required");
            if (venue == null || venue.isBlank()) throw new IllegalArgumentException("Venue required");
            if (type == null) throw new IllegalArgumentException("Type required");

            String id = "SES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            SeminarSession s = new SeminarSession(id, date, venue.trim(), type);
            sessionRepo.save(s);
            return s;
        }

        TimeSlot addSlot(String sessionId, LocalTime start, LocalTime end) {
            SeminarSession s = sessionRepo.find(sessionId);
            if (s == null) throw new IllegalArgumentException("Session not found: " + sessionId);
            if (start == null || end == null) throw new IllegalArgumentException("Start/end required");
            if (!start.isBefore(end)) throw new IllegalArgumentException("Start must be before end");

            String slotId = "SL-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            TimeSlot slot = new TimeSlot(slotId, start, end);
            s.slots.add(slot);
            return slot;
        }
    }

    static class AssignmentService {
        private final SessionRepo sessionRepo;
        private final SubmissionRepo submissionRepo;
        private final UserRepo userRepo;

        AssignmentService(SessionRepo sessionRepo, SubmissionRepo submissionRepo, UserRepo userRepo) {
            this.sessionRepo = sessionRepo;
            this.submissionRepo = submissionRepo;
            this.userRepo = userRepo;
        }

        void assignSubmissionToSlot(String sessionId, String slotId, String submissionId) {
            SeminarSession ses = requireSession(sessionId);
            TimeSlot slot = requireSlot(ses, slotId);
            Submission sub = submissionRepo.find(submissionId);
            if (sub == null) throw new IllegalArgumentException("Submission not found: " + submissionId);

            // optional rule: session type matches submission preferredType
            if (ses.type != sub.preferredType) {
                throw new IllegalArgumentException("Type mismatch: session is " + ses.type + " but submission is " + sub.preferredType);
            }
            slot.submissionId = submissionId;
        }

        void assignEvaluatorToSlot(String sessionId, String slotId, String evaluatorId) {
            SeminarSession ses = requireSession(sessionId);
            TimeSlot slot = requireSlot(ses, slotId);
            User u = userRepo.find(evaluatorId);
            if (u == null || u.role != Role.EVALUATOR) throw new IllegalArgumentException("Invalid evaluator: " + evaluatorId);

            slot.evaluatorIds.add(evaluatorId);
        }

        private SeminarSession requireSession(String sessionId) {
            SeminarSession ses = sessionRepo.find(sessionId);
            if (ses == null) throw new IllegalArgumentException("Session not found: " + sessionId);
            return ses;
        }

        private TimeSlot requireSlot(SeminarSession ses, String slotId) {
            for (TimeSlot t : ses.slots) if (t.slotId.equals(slotId)) return t;
            throw new IllegalArgumentException("Slot not found: " + slotId);
        }
    }

    static class EvaluationService {
        private final EvaluationRepo evaluationRepo;
        private final SessionRepo sessionRepo;
        private final SubmissionRepo submissionRepo;
        private final UserRepo userRepo;

        EvaluationService(EvaluationRepo evaluationRepo, SessionRepo sessionRepo,
                          SubmissionRepo submissionRepo, UserRepo userRepo) {
            this.evaluationRepo = evaluationRepo;
            this.sessionRepo = sessionRepo;
            this.submissionRepo = submissionRepo;
            this.userRepo = userRepo;
        }

        List<AssignedItem> assignedToEvaluator(String evaluatorId) {
            User u = userRepo.find(evaluatorId);
            if (u == null || u.role != Role.EVALUATOR) throw new IllegalArgumentException("Invalid evaluator");

            List<AssignedItem> items = new ArrayList<>();
            for (SeminarSession ses : sessionRepo.all()) {
                for (TimeSlot slot : ses.slots) {
                    if (slot.submissionId != null && slot.evaluatorIds.contains(evaluatorId)) {
                        Submission sub = submissionRepo.find(slot.submissionId);
                        if (sub != null) items.add(new AssignedItem(ses, slot, sub));
                    }
                }
            }
            return items;
        }

        Evaluation getOrCreate(String submissionId, String evaluatorId) {
            Evaluation existing = evaluationRepo.findBySubmissionAndEvaluator(submissionId, evaluatorId);
            if (existing != null) return existing;

            String id = "EV-" + UUID.randomUUID().toString().substring(0, 7).toUpperCase();
            Evaluation e = new Evaluation(id, submissionId, evaluatorId);
            evaluationRepo.save(e);
            return e;
        }

        void saveScores(Evaluation e, int pc, int meth, int res, int pres, String comments) {
            validateScore(pc); validateScore(meth); validateScore(res); validateScore(pres);
            e.scores.put(RubricCriterion.PROBLEM_CLARITY, pc);
            e.scores.put(RubricCriterion.METHODOLOGY, meth);
            e.scores.put(RubricCriterion.RESULTS, res);
            e.scores.put(RubricCriterion.PRESENTATION, pres);
            e.comments = comments == null ? "" : comments.trim();
            evaluationRepo.save(e);
        }

        private void validateScore(int s) {
            if (s < 0 || s > 5) throw new IllegalArgumentException("Score must be 0..5");
        }
    }

    static class ReportService {
        private final SessionRepo sessionRepo;
        private final SubmissionRepo submissionRepo;
        private final EvaluationRepo evaluationRepo;

        ReportService(SessionRepo sessionRepo, SubmissionRepo submissionRepo, EvaluationRepo evaluationRepo) {
            this.sessionRepo = sessionRepo;
            this.submissionRepo = submissionRepo;
            this.evaluationRepo = evaluationRepo;
        }

        String scheduleReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SEMINAR SCHEDULE ===\n\n");
            for (SeminarSession ses : sessionRepo.all()) {
                sb.append(ses).append("\n");
                if (ses.slots.isEmpty()) {
                    sb.append("  (no slots)\n\n");
                    continue;
                }
                for (TimeSlot slot : ses.slots) {
                    sb.append("  ").append(slot.start).append("-").append(slot.end).append(" | ").append(slot.slotId);
                    if (slot.submissionId == null) {
                        sb.append(" | (no presenter)\n");
                    } else {
                        Submission sub = submissionRepo.find(slot.submissionId);
                        sb.append(" | ").append(sub == null ? slot.submissionId : sub.title)
                                .append(" | Evaluators: ").append(slot.evaluatorIds).append("\n");
                    }
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        String evaluationSummaryReport() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== EVALUATION SUMMARY ===\n\n");
            for (Submission s : submissionRepo.all()) {
                List<Evaluation> evals = evaluationRepo.bySubmission(s.submissionId);
                sb.append(s.submissionId).append(" | ").append(s.title).append(" | ").append(s.preferredType).append("\n");
                if (evals.isEmpty()) {
                    sb.append("  No evaluations yet.\n\n");
                    continue;
                }
                double avg = evals.stream().mapToInt(Evaluation::total).average().orElse(0);
                sb.append("  Evaluations: ").append(evals.size()).append(" | Avg Total: ").append(String.format("%.2f", avg)).append("\n");
                for (Evaluation e : evals) {
                    sb.append("   - ").append(e.evaluatorId)
                            .append(" total=").append(e.total())
                            .append(" scores=").append(e.scores)
                            .append(" comments=").append(e.comments == null ? "" : e.comments)
                            .append("\n");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    static class AwardService {
        private final SubmissionRepo submissionRepo;
        private final EvaluationRepo evaluationRepo;

        AwardService(SubmissionRepo submissionRepo, EvaluationRepo evaluationRepo) {
            this.submissionRepo = submissionRepo;
            this.evaluationRepo = evaluationRepo;
        }

        AwardResult computeBestByType(PresentationType type) {
            Submission best = null;
            double bestAvg = -1;

            for (Submission s : submissionRepo.all()) {
                if (s.preferredType != type) continue;
                List<Evaluation> evals = evaluationRepo.bySubmission(s.submissionId);
                if (evals.isEmpty()) continue;
                double avg = evals.stream().mapToInt(Evaluation::total).average().orElse(0);
                if (avg > bestAvg) { bestAvg = avg; best = s; }
            }

            return new AwardResult(type, best, bestAvg);
        }

        static class AwardResult {
            final PresentationType type;
            final Submission winner;
            final double avgScore;
            AwardResult(PresentationType type, Submission winner, double avgScore) {
                this.type = type;
                this.winner = winner;
                this.avgScore = avgScore;
            }

            @Override public String toString() {
                if (winner == null) return "Best " + type + ": (no winner yet - need evaluations)";
                return "Best " + type + ": " + winner.title + " (" + winner.submissionId + "), Avg=" + String.format("%.2f", avgScore);
            }
        }
    }

    static class AssignedItem {
        final SeminarSession session;
        final TimeSlot slot;
        final Submission submission;
        AssignedItem(SeminarSession session, TimeSlot slot, Submission submission) {
            this.session = session;
            this.slot = slot;
            this.submission = submission;
        }
    }

    // ---------------------------
    // UI Frame + Navigation
    // ---------------------------
    static class AppFrame extends JFrame {
        private final AppContext ctx;
        private final CardLayout cards = new CardLayout();
        private final JPanel root = new JPanel(cards);

        private User currentUser;

        private final LoginPanel loginPanel;
        private final StudentPanel studentPanel;
        private final EvaluatorPanel evaluatorPanel;
        private final CoordinatorPanel coordinatorPanel;

        AppFrame(AppContext ctx) {
            super("Seminar Management System (Swing)");
            this.ctx = ctx;

            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            setSize(1050, 650);
            setLocationRelativeTo(null);

            loginPanel = new LoginPanel(ctx, this::onLoginSuccess);
            studentPanel = new StudentPanel(ctx, this::logout);
            evaluatorPanel = new EvaluatorPanel(ctx, this::logout);
            coordinatorPanel = new CoordinatorPanel(ctx, this::logout);

            root.add(loginPanel, "LOGIN");
            root.add(studentPanel, "STUDENT");
            root.add(evaluatorPanel, "EVALUATOR");
            root.add(coordinatorPanel, "COORDINATOR");

            setContentPane(root);
            showLogin();
        }

        void showLogin() {
            currentUser = null;
            loginPanel.reset();
            cards.show(root, "LOGIN");
        }

        void onLoginSuccess(User user) {
            this.currentUser = user;

            if (user.role == Role.STUDENT) {
                studentPanel.load((Student) user);
                cards.show(root, "STUDENT");
            } else if (user.role == Role.EVALUATOR) {
                evaluatorPanel.load((Evaluator) user);
                cards.show(root, "EVALUATOR");
            } else {
                coordinatorPanel.load((Coordinator) user);
                cards.show(root, "COORDINATOR");
            }
        }

        void logout() {
            showLogin();
        }
    }

    // ---------------------------
    // UI: Login
    // ---------------------------
    static class LoginPanel extends JPanel {
        private final AppContext ctx;
        private final JTextField userIdField = new JTextField(18);
        private final JLabel hint = new JLabel("Demo IDs: S1001 | E2001 | C3001");
        private final Consumer<User> onSuccess;

        LoginPanel(AppContext ctx, Consumer<User> onSuccess) {
            this.ctx = ctx;
            this.onSuccess = onSuccess;

            setLayout(new GridBagLayout());
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel title = new JLabel("Seminar Management System");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

            JLabel sub = new JLabel("Enter User ID to login (role is determined by ID)");
            sub.setBorder(new EmptyBorder(0, 0, 10, 0));

            JButton loginBtn = new JButton(new AbstractAction("Login") {
                @Override public void actionPerformed(ActionEvent e) { doLogin(); }
            });

            card.add(title);
            card.add(Box.createVerticalStrut(8));
            card.add(sub);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(new JLabel("User ID:"));
            row.add(userIdField);
            row.add(loginBtn);

            card.add(row);
            card.add(Box.createVerticalStrut(10));
            card.add(hint);

            add(card);
        }

        void reset() {
            userIdField.setText("");
        }

        private void doLogin() {
            try {
                String id = userIdField.getText().trim();
                if (id.isEmpty()) throw new IllegalArgumentException("Enter a User ID");
                User u = ctx.authService.login(id);
                onSuccess.accept(u);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---------------------------
    // UI: Student
    // ---------------------------
    static class StudentPanel extends JPanel {
        private final AppContext ctx;
        private final Runnable onLogout;

        private Student student;

        private final JLabel header = new JLabel();
        private final JTextField titleField = new JTextField(28);
        private final JTextArea abstractArea = new JTextArea(5, 28);
        private final JTextField supervisorField = new JTextField(20);
        private final JComboBox<PresentationType> typeBox = new JComboBox<>(PresentationType.values());
        private final JTextField materialPathField = new JTextField(22);

        private final SubmissionTableModel tableModel = new SubmissionTableModel();
        private final JTable table = new JTable(tableModel);

        StudentPanel(AppContext ctx, Runnable onLogout) {
            this.ctx = ctx;
            this.onLogout = onLogout;

            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel top = new JPanel(new BorderLayout());
            header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
            top.add(header, BorderLayout.WEST);

            JButton logoutBtn = new JButton("Logout");
            logoutBtn.addActionListener(e -> onLogout.run());
            top.add(logoutBtn, BorderLayout.EAST);

            add(top, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
            center.add(buildRegisterCard());
            center.add(buildMySubsCard());
            add(center, BorderLayout.CENTER);
        }

        void load(Student s) {
            this.student = s;
            header.setText("Student Dashboard: " + s.name + " (" + s.userId + ")");
            supervisorField.setText(s.supervisorName);
            refreshTable();
        }

        private JPanel buildRegisterCard() {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createTitledBorder("Register Submission"));

            abstractArea.setLineWrap(true);
            abstractArea.setWrapStyleWord(true);

            card.add(row("Title:", titleField));
            card.add(Box.createVerticalStrut(6));
            card.add(row("Supervisor:", supervisorField));
            card.add(Box.createVerticalStrut(6));
            card.add(row("Type:", typeBox));
            card.add(Box.createVerticalStrut(6));

            JPanel absRow = new JPanel(new BorderLayout(8, 8));
            absRow.add(new JLabel("Abstract:"), BorderLayout.NORTH);
            absRow.add(new JScrollPane(abstractArea), BorderLayout.CENTER);
            card.add(absRow);

            card.add(Box.createVerticalStrut(6));

            JPanel fileRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            fileRow.add(new JLabel("Material:"));
            materialPathField.setEditable(false);
            fileRow.add(materialPathField);

            JButton browse = new JButton("Browse...");
            browse.addActionListener(e -> chooseFile());
            fileRow.add(browse);
            card.add(fileRow);

            card.add(Box.createVerticalStrut(10));
            JButton submit = new JButton("Register");
            submit.addActionListener(e -> doRegister());
            card.add(submit);

            return card;
        }

        private JPanel buildMySubsCard() {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBorder(BorderFactory.createTitledBorder("My Submissions"));
            table.setFillsViewportHeight(true);
            card.add(new JScrollPane(table), BorderLayout.CENTER);

            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> refreshTable());
            card.add(refresh, BorderLayout.SOUTH);

            return card;
        }

        private JPanel row(String label, JComponent comp) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel(label));
            p.add(comp);
            return p;
        }

        private void chooseFile() {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                materialPathField.setText(f.getAbsolutePath());
            }
        }

        private void doRegister() {
            try {
                Submission s = ctx.submissionService.register(
                        student.userId,
                        titleField.getText(),
                        abstractArea.getText(),
                        supervisorField.getText(),
                        (PresentationType) typeBox.getSelectedItem(),
                        materialPathField.getText()
                );
                JOptionPane.showMessageDialog(this, "Registered: " + s.submissionId);
                titleField.setText("");
                abstractArea.setText("");
                materialPathField.setText("");
                refreshTable();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void refreshTable() {
            List<Submission> subs = ctx.submissionRepo.byStudent(student.userId);
            tableModel.setData(subs);
        }
    }

    static class SubmissionTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Title", "Type", "Supervisor", "Material Path"};
        private List<Submission> data = new ArrayList<>();

        void setData(List<Submission> subs) {
            data = subs == null ? new ArrayList<>() : subs;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            Submission s = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> s.submissionId;
                case 1 -> s.title;
                case 2 -> s.preferredType;
                case 3 -> s.supervisorName;
                case 4 -> s.materialPath;
                default -> "";
            };
        }
    }

    // ---------------------------
    // UI: Evaluator
    // ---------------------------
    static class EvaluatorPanel extends JPanel {
        private final AppContext ctx;
        private final Runnable onLogout;

        private Evaluator evaluator;

        private final JLabel header = new JLabel();

        private final AssignedTableModel assignedModel = new AssignedTableModel();
        private final JTable assignedTable = new JTable(assignedModel);

        // Evaluation form
        private final JLabel selectedLabel = new JLabel("Selected: (none)");
        private final JSpinner pc = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        private final JSpinner meth = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        private final JSpinner res = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        private final JSpinner pres = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1));
        private final JTextArea comments = new JTextArea(5, 25);

        private AssignedItem selected;

        EvaluatorPanel(AppContext ctx, Runnable onLogout) {
            this.ctx = ctx;
            this.onLogout = onLogout;

            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel top = new JPanel(new BorderLayout());
            header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
            top.add(header, BorderLayout.WEST);

            JButton logoutBtn = new JButton("Logout");
            logoutBtn.addActionListener(e -> onLogout.run());
            top.add(logoutBtn, BorderLayout.EAST);

            add(top, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
            center.add(buildAssignedCard());
            center.add(buildEvaluationCard());
            add(center, BorderLayout.CENTER);
        }

        void load(Evaluator e) {
            evaluator = e;
            header.setText("Evaluator Dashboard: " + e.name + " (" + e.userId + ")");
            refreshAssigned();
            clearForm();
        }

        private JPanel buildAssignedCard() {
            JPanel card = new JPanel(new BorderLayout(8, 8));
            card.setBorder(BorderFactory.createTitledBorder("Assigned Presentations"));

            assignedTable.setFillsViewportHeight(true);
            card.add(new JScrollPane(assignedTable), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton select = new JButton("Select");
            select.addActionListener(e -> selectRow());
            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> refreshAssigned());
            bottom.add(select);
            bottom.add(refresh);

            card.add(bottom, BorderLayout.SOUTH);
            return card;
        }

        private JPanel buildEvaluationCard() {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createTitledBorder("Evaluation (0..5 each)"));

            comments.setLineWrap(true);
            comments.setWrapStyleWord(true);

            card.add(selectedLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(formRow("Problem Clarity:", pc));
            card.add(formRow("Methodology:", meth));
            card.add(formRow("Results:", res));
            card.add(formRow("Presentation:", pres));
            card.add(Box.createVerticalStrut(8));

            JPanel commRow = new JPanel(new BorderLayout(8, 8));
            commRow.add(new JLabel("Comments:"), BorderLayout.NORTH);
            commRow.add(new JScrollPane(comments), BorderLayout.CENTER);
            card.add(commRow);

            card.add(Box.createVerticalStrut(10));
            JButton save = new JButton("Save Evaluation");
            save.addActionListener(e -> saveEval());
            card.add(save);

            return card;
        }

        private JPanel formRow(String label, JComponent comp) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(new JLabel(label));
            p.add(comp);
            return p;
        }

        private void refreshAssigned() {
            List<AssignedItem> items = ctx.evaluationService.assignedToEvaluator(evaluator.userId);
            assignedModel.setData(items);
        }

        private void selectRow() {
            int r = assignedTable.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first.");
                return;
            }
            selected = assignedModel.getAt(r);
            selectedLabel.setText("Selected: " + selected.submission.title + " (" + selected.submission.submissionId + ")");
            loadExistingEval();
        }

        private void loadExistingEval() {
            if (selected == null) return;
            Evaluation e = ctx.evaluationService.getOrCreate(selected.submission.submissionId, evaluator.userId);
            pc.setValue(e.scores.get(RubricCriterion.PROBLEM_CLARITY));
            meth.setValue(e.scores.get(RubricCriterion.METHODOLOGY));
            res.setValue(e.scores.get(RubricCriterion.RESULTS));
            pres.setValue(e.scores.get(RubricCriterion.PRESENTATION));
            comments.setText(e.comments == null ? "" : e.comments);
        }

        private void clearForm() {
            selected = null;
            selectedLabel.setText("Selected: (none)");
            pc.setValue(0); meth.setValue(0); res.setValue(0); pres.setValue(0);
            comments.setText("");
        }

        private void saveEval() {
            try {
                if (selected == null) throw new IllegalArgumentException("Select an assigned presentation first.");
                Evaluation e = ctx.evaluationService.getOrCreate(selected.submission.submissionId, evaluator.userId);
                ctx.evaluationService.saveScores(
                        e,
                        (Integer) pc.getValue(),
                        (Integer) meth.getValue(),
                        (Integer) res.getValue(),
                        (Integer) pres.getValue(),
                        comments.getText()
                );
                JOptionPane.showMessageDialog(this, "Saved. Total = " + e.total());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class AssignedTableModel extends AbstractTableModel {
        private final String[] cols = {"Session", "Date", "Venue", "Slot", "Time", "Submission", "Type"};
        private List<AssignedItem> data = new ArrayList<>();

        void setData(List<AssignedItem> items) {
            data = items == null ? new ArrayList<>() : items;
            fireTableDataChanged();
        }

        AssignedItem getAt(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int col) { return cols[col]; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            AssignedItem it = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> it.session.sessionId;
                case 1 -> it.session.date;
                case 2 -> it.session.venue;
                case 3 -> it.slot.slotId;
                case 4 -> it.slot.start + "-" + it.slot.end;
                case 5 -> it.submission.title;
                case 6 -> it.submission.preferredType;
                default -> "";
            };
        }
    }

    // ---------------------------
    // UI: Coordinator
    // ---------------------------
    static class CoordinatorPanel extends JPanel {
        private final AppContext ctx;
        private final Runnable onLogout;

        private Coordinator coordinator;

        private final JLabel header = new JLabel();

        // Left: create session + slots
        private final JTextField venueField = new JTextField(18);
        private final JComboBox<PresentationType> sessionTypeBox = new JComboBox<>(PresentationType.values());
        private final JTextField dateField = new JTextField(10); // yyyy-mm-dd
        private final JTextField startField = new JTextField(6); // HH:mm
        private final JTextField endField = new JTextField(6);   // HH:mm

        private final DefaultListModel<SeminarSession> sessionListModel = new DefaultListModel<>();
        private final JList<SeminarSession> sessionList = new JList<>(sessionListModel);

        private final DefaultListModel<TimeSlot> slotListModel = new DefaultListModel<>();
        private final JList<TimeSlot> slotList = new JList<>(slotListModel);

        // Right: assignments + reports
        private final DefaultListModel<Submission> subListModel = new DefaultListModel<>();
        private final JList<Submission> submissionList = new JList<>(subListModel);

        private final DefaultListModel<Evaluator> evalListModel = new DefaultListModel<>();
        private final JList<Evaluator> evaluatorList = new JList<>(evalListModel);

        private final JTextArea reportArea = new JTextArea();

        CoordinatorPanel(AppContext ctx, Runnable onLogout) {
            this.ctx = ctx;
            this.onLogout = onLogout;

            setLayout(new BorderLayout(10, 10));
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel top = new JPanel(new BorderLayout());
            header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
            top.add(header, BorderLayout.WEST);

            JButton logoutBtn = new JButton("Logout");
            logoutBtn.addActionListener(e -> onLogout.run());
            top.add(logoutBtn, BorderLayout.EAST);
            add(top, BorderLayout.NORTH);

            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
            split.setDividerLocation(480);
            add(split, BorderLayout.CENTER);

            // selection listeners
            sessionList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) reloadSlots();
            });
        }

        void load(Coordinator c) {
            coordinator = c;
            header.setText("Coordinator Dashboard: " + c.name + " (" + c.userId + ")");
            dateField.setText(LocalDate.now().plusDays(1).toString());
            refreshAll();
        }

        private JPanel buildLeft() {
            JPanel left = new JPanel(new BorderLayout(8, 8));

            JPanel create = new JPanel();
            create.setLayout(new BoxLayout(create, BoxLayout.Y_AXIS));
            create.setBorder(BorderFactory.createTitledBorder("Create Session + Add Slot"));

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row1.add(new JLabel("Date (yyyy-mm-dd):"));
            row1.add(dateField);
            create.add(row1);

            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row2.add(new JLabel("Venue:"));
            row2.add(venueField);
            create.add(row2);

            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row3.add(new JLabel("Type:"));
            row3.add(sessionTypeBox);
            create.add(row3);

            JButton createBtn = new JButton("Create Session");
            createBtn.addActionListener(e -> createSession());
            create.add(createBtn);

            create.add(Box.createVerticalStrut(10));
            JPanel slotRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            slotRow.add(new JLabel("Slot Start (HH:mm):"));
            slotRow.add(startField);
            slotRow.add(new JLabel("End:"));
            slotRow.add(endField);
            create.add(slotRow);

            JButton addSlotBtn = new JButton("Add Slot to Selected Session");
            addSlotBtn.addActionListener(e -> addSlot());
            create.add(addSlotBtn);

            left.add(create, BorderLayout.NORTH);

            JPanel lists = new JPanel(new GridLayout(1, 2, 8, 8));
            JPanel sesCard = new JPanel(new BorderLayout());
            sesCard.setBorder(BorderFactory.createTitledBorder("Sessions"));
            sesCard.add(new JScrollPane(sessionList), BorderLayout.CENTER);

            JPanel slotCard = new JPanel(new BorderLayout());
            slotCard.setBorder(BorderFactory.createTitledBorder("Slots (selected session)"));
            slotCard.add(new JScrollPane(slotList), BorderLayout.CENTER);

            lists.add(sesCard);
            lists.add(slotCard);

            left.add(lists, BorderLayout.CENTER);

            JButton refresh = new JButton("Refresh Lists");
            refresh.addActionListener(e -> refreshAll());
            left.add(refresh, BorderLayout.SOUTH);

            return left;
        }

        private JPanel buildRight() {
            JPanel right = new JPanel(new BorderLayout(8, 8));

            JPanel assignPanel = new JPanel(new GridLayout(1, 2, 8, 8));

            JPanel subCard = new JPanel(new BorderLayout());
            subCard.setBorder(BorderFactory.createTitledBorder("Submissions (pick 1)"));
            subCard.add(new JScrollPane(submissionList), BorderLayout.CENTER);

            JPanel evalCard = new JPanel(new BorderLayout());
            evalCard.setBorder(BorderFactory.createTitledBorder("Evaluators (pick 1)"));
            evalCard.add(new JScrollPane(evaluatorList), BorderLayout.CENTER);

            assignPanel.add(subCard);
            assignPanel.add(evalCard);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton assignSub = new JButton("Assign Submission to Slot");
            assignSub.addActionListener(e -> assignSubmission());
            JButton assignEval = new JButton("Assign Evaluator to Slot");
            assignEval.addActionListener(e -> assignEvaluator());
            buttons.add(assignSub);
            buttons.add(assignEval);

            JPanel reports = new JPanel(new BorderLayout(8, 8));
            reports.setBorder(BorderFactory.createTitledBorder("Reports + Awards"));
            reportArea.setEditable(false);
            reportArea.setLineWrap(true);
            reportArea.setWrapStyleWord(true);
            reports.add(new JScrollPane(reportArea), BorderLayout.CENTER);

            JPanel repBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton sched = new JButton("Show Schedule");
            sched.addActionListener(e -> reportArea.setText(ctx.reportService.scheduleReport()));
            JButton evalSum = new JButton("Show Evaluation Summary");
            evalSum.addActionListener(e -> reportArea.setText(ctx.reportService.evaluationSummaryReport()));
            JButton awards = new JButton("Compute Awards");
            awards.addActionListener(e -> showAwards());
            repBtns.add(sched);
            repBtns.add(evalSum);
            repBtns.add(awards);

            reports.add(repBtns, BorderLayout.SOUTH);

            right.add(assignPanel, BorderLayout.NORTH);
            right.add(buttons, BorderLayout.CENTER);
            right.add(reports, BorderLayout.SOUTH);

            return right;
        }

        private void refreshAll() {
            // sessions
            sessionListModel.clear();
            for (SeminarSession s : ctx.sessionRepo.all()) sessionListModel.addElement(s);

            // submissions
            subListModel.clear();
            for (Submission s : ctx.submissionRepo.all()) subListModel.addElement(s);

            // evaluators
            evalListModel.clear();
            for (Evaluator e : ctx.userRepo.allEvaluators()) evalListModel.addElement(e);

            reloadSlots();
        }

        private void reloadSlots() {
            slotListModel.clear();
            SeminarSession ses = sessionList.getSelectedValue();
            if (ses == null) return;
            for (TimeSlot t : ses.slots) slotListModel.addElement(t);
        }

        private void createSession() {
            try {
                LocalDate d = LocalDate.parse(dateField.getText().trim());
                String venue = venueField.getText().trim();
                PresentationType type = (PresentationType) sessionTypeBox.getSelectedItem();

                SeminarSession s = ctx.sessionService.createSession(d, venue, type);
                JOptionPane.showMessageDialog(this, "Created: " + s.sessionId);
                refreshAll();
                sessionList.setSelectedValue(s, true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void addSlot() {
            try {
                SeminarSession ses = sessionList.getSelectedValue();
                if (ses == null) throw new IllegalArgumentException("Select a session first.");
                LocalTime st = LocalTime.parse(startField.getText().trim());
                LocalTime en = LocalTime.parse(endField.getText().trim());

                TimeSlot slot = ctx.sessionService.addSlot(ses.sessionId, st, en);
                JOptionPane.showMessageDialog(this, "Added slot: " + slot.slotId);
                reloadSlots();
                slotList.setSelectedValue(slot, true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void assignSubmission() {
            try {
                SeminarSession ses = sessionList.getSelectedValue();
                TimeSlot slot = slotList.getSelectedValue();
                Submission sub = submissionList.getSelectedValue();
                if (ses == null || slot == null) throw new IllegalArgumentException("Select session + slot.");
                if (sub == null) throw new IllegalArgumentException("Select a submission.");

                ctx.assignmentService.assignSubmissionToSlot(ses.sessionId, slot.slotId, sub.submissionId);
                JOptionPane.showMessageDialog(this, "Assigned submission to slot.");
                reloadSlots();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void assignEvaluator() {
            try {
                SeminarSession ses = sessionList.getSelectedValue();
                TimeSlot slot = slotList.getSelectedValue();
                Evaluator ev = evaluatorList.getSelectedValue();
                if (ses == null || slot == null) throw new IllegalArgumentException("Select session + slot.");
                if (ev == null) throw new IllegalArgumentException("Select an evaluator.");

                ctx.assignmentService.assignEvaluatorToSlot(ses.sessionId, slot.slotId, ev.userId);
                JOptionPane.showMessageDialog(this, "Assigned evaluator to slot.");
                reloadSlots();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showAwards() {
            AwardService.AwardResult oral = ctx.awardService.computeBestByType(PresentationType.ORAL);
            AwardService.AwardResult poster = ctx.awardService.computeBestByType(PresentationType.POSTER);

            String msg = "=== AWARDS ===\n" + oral + "\n" + poster + "\n\n" +
                    "(People's Choice not implemented here — can be a simple vote counter if you want.)";
            reportArea.setText(msg);
        }
    }

    // ---------------------------
    // Small helper
    // ---------------------------
    interface Consumer<T> { void accept(T t); }
}
