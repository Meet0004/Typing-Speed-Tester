import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// entry 
public class TypingSpeedTester extends JFrame {
    // sab difficulty in hashmaps
    private static final Map<String, String[]> DIFFICULTY_TEXTS = new HashMap<String, String[]>() {{
        put("Beginner", new String[]{
            "The cat sits on the mat.",
            "I like to eat pizza.",
            "The sun is bright today.",
            "Dogs are very loyal pets.",
            "Water is essential for life."
        });
        put("Intermediate", new String[]{
            "The quick brown fox jumps over the lazy dog near the riverbank.",
            "Programming requires logical thinking and creative problem-solving skills.",
            "Technology continues to advance at an unprecedented rate in modern society.",
            "Learning new languages opens doors to different cultures and opportunities.",
            "The art of cooking combines science, creativity, and cultural traditions together."
        });
        put("Advanced", new String[]{
            "Artificial intelligence and machine learning algorithms are revolutionizing industries across the globe, fundamentally changing how we approach complex computational problems.",
            "The implementation of sophisticated data structures and algorithms requires not only theoretical understanding but also practical experience in optimizing performance characteristics.",
            "Quantum computing represents a paradigm shift in computational capabilities, potentially solving problems that are intractable for classical computers through quantum superposition and entanglement.",
            "Cybersecurity professionals must constantly adapt to emerging threats while implementing robust defense mechanisms that protect sensitive information from sophisticated adversaries.",
            "The intersection of biotechnology and artificial intelligence is creating unprecedented opportunities for medical breakthroughs and personalized treatment methodologies."
        });
        put("Expert", new String[]{
            "The epistemological foundations of contemporary philosophical discourse necessitate a comprehensive examination of phenomenological hermeneutics and post-structuralist critiques of traditional metaphysical paradigms.",
            "Neuroplasticity research demonstrates that synaptic connections undergo continuous reorganization throughout the lifespan, challenging previous assumptions about fixed neural architectures and cognitive limitations.",
            "Macroeconomic theory encompasses complex interdependencies between fiscal policy, monetary policy, international trade dynamics, and technological innovation cycles that influence long-term economic stability.",
            "Bioinformatics algorithms must efficiently process vast genomic datasets while accounting for evolutionary relationships, structural variations, and epigenetic modifications that influence gene expression patterns.",
            "Astrophysical simulations of galactic formation require sophisticated numerical methods to model gravitational interactions, dark matter distributions, and stellar nucleosynthesis processes across cosmological timescales."
        });
    }};
    
    // gUI Components
    private JTabbedPane mainTabs;
    private JPanel typingPanel, statsPanel, settingsPanel, leaderboardPanel;
    private JTextArea promptArea, inputArea;
    private JLabel timerLabel, wpmLabel, accuracyLabel, streakLabel, levelLabel;
    private JProgressBar accuracyBar, progressBar, levelProgressBar;
    private JButton startButton, pauseButton, resetButton, hintButton;
    private JComboBox<String> difficultyCombo, themeCombo, fontSizeCombo;
    private JCheckBox soundCheckbox, realTimeCheckbox, mistakeHighlightCheckbox;
    private JSlider volumeSlider;
    private JTable historyTable, leaderboardTable;
    private JLabel mistakeCountLabel, bestWpmLabel, totalTestsLabel;
    
    // game state
    private String currentText;
    private long startTime, pausedTime = 0;
    private boolean testStarted = false, testPaused = false;
    private javax.swing.Timer uiTimer, countdownTimer;
    private Random random;
    private List<TestResult> testHistory;
    private int currentDifficulty = 1;
    private int mistakeCount = 0;
    private int currentStreak = 0, bestStreak = 0;
    private int currentLevel = 1;
    private double totalXP = 0;
    private Map<String, Color> themes;
    private String currentTheme = "Dark";
    private boolean soundEnabled = true;
    private Font typingFont;
    
    private static final int TIMER_UPDATE_DELAY = 100; // milliseconds
    private static final int BASE_XP_PER_LEVEL = 1000;
    private int countdownSeconds = 60; // or whatever initial value you want

    // statssssssssssssssss
    private double bestWPM = 0;
    private int totalTests = 0;
    private long totalTypingTime = 0;
    
    public TypingSpeedTester() {
        random = new Random();
        testHistory = new ArrayList<>();
        initializeThemes();
        initializeGUI();
        loadNewText();
        applyTheme();
    }
    
    private void initializeThemes() {
        themes = new HashMap<>();
        themes.put("Dark", new Color(44, 62, 80));
        themes.put("Blue", new Color(52, 152, 219));
        themes.put("Green", new Color(46, 204, 113));
        themes.put("Purple", new Color(155, 89, 182));
        themes.put("Orange", new Color(230, 126, 34));
        themes.put("Red", new Color(231, 76, 60));
    }
    
    private void initializeGUI() {
        setTitle("🚀 Advanced Typing Speed Tester Pro - Level " + currentLevel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        mainTabs = new JTabbedPane();
        mainTabs.setFont(new Font("Arial", Font.BOLD, 14));
        
        createTypingTab();
        createStatsTab();
        createSettingsTab();
        createLeaderboardTab();
        
        add(mainTabs);
        

       uiTimer = new javax.swing.Timer(TIMER_UPDATE_DELAY, e -> updateRealTimeStats());
countdownTimer = new javax.swing.Timer(1000, e -> updateCountdown());
        
        setupKeyboardShortcuts();
        pack();
        setLocationRelativeTo(null);
    }
    private void updateCountdown() {
        if (countdownSeconds > 0) {
            countdownSeconds--;
            timerLabel.setText("Countdown: " + countdownSeconds);
            if (countdownSeconds <= 10) {
                timerLabel.setForeground(Color.RED);
                playSound("countdown");
            }
        } else {
            countdownTimer.stop();
            timerLabel.setForeground(Color.BLACK);
            if (testStarted) {
                completeTest();
            }
        }
    }
    private void createTypingTab() {
        typingPanel = new JPanel(new BorderLayout());

        //thode main main panelssssssssss
        JPanel headerPanel = createHeaderPanel();

        JPanel mainPanel = createMainTypingPanel();

        JPanel controlPanel = createControlPanel();
        
        JPanel realTimeStatsPanel = createRealTimeStatsPanel();
        
        typingPanel.add(headerPanel, BorderLayout.NORTH);
        typingPanel.add(mainPanel, BorderLayout.CENTER);
        typingPanel.add(controlPanel, BorderLayout.SOUTH);
        typingPanel.add(realTimeStatsPanel, BorderLayout.EAST);
        
        mainTabs.addTab("⌨️ Typing Test", typingPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🏆 TYPING SPEED MASTER", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        
        // Game stats row
        JPanel gameStatsPanel = new JPanel(new FlowLayout());
        levelLabel = new JLabel("Level " + currentLevel);
        levelLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        levelProgressBar = new JProgressBar(0, 1000);
        levelProgressBar.setStringPainted(true);
        levelProgressBar.setString("XP: " + (int)totalXP + "/1000");
        levelProgressBar.setPreferredSize(new Dimension(200, 25));
        
        bestWpmLabel = new JLabel("Best: " + (int)bestWPM + " WPM");
        bestWpmLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        streakLabel = new JLabel("Streak: " + currentStreak + " (Best: " + bestStreak + ")");
        streakLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        gameStatsPanel.add(levelLabel);
        gameStatsPanel.add(levelProgressBar);
        gameStatsPanel.add(Box.createHorizontalStrut(20));
        gameStatsPanel.add(bestWpmLabel);
        gameStatsPanel.add(Box.createHorizontalStrut(20));
        gameStatsPanel.add(streakLabel);
        
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(gameStatsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMainTypingPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 10, 20));
        

        JPanel topPanel = new JPanel(new BorderLayout());
        
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        difficultyPanel.add(new JLabel("Difficulty:"));
        difficultyCombo = new JComboBox<>(new String[]{"Beginner", "Intermediate", "Advanced", "Expert"});
        difficultyCombo.setSelectedIndex(1);
        difficultyCombo.addActionListener(e -> changeDifficulty());
        difficultyPanel.add(difficultyCombo);
        
        timerLabel = new JLabel("Ready to Start!", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        topPanel.add(difficultyPanel, BorderLayout.WEST);
        topPanel.add(timerLabel, BorderLayout.CENTER);
        
        promptArea = new JTextArea(4, 0);
        promptArea.setFont(new Font("Monospace", Font.PLAIN, 18));
        promptArea.setEditable(false);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder("📝 Type this text:"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        inputArea = new JTextArea(4, 0);
        typingFont = new Font("Monospace", Font.PLAIN, 18);
        inputArea.setFont(typingFont);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder("✏️ Your typing:"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Add document listener for real-time feedback
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onTextChange(); }
            public void removeUpdate(DocumentEvent e) { onTextChange(); }
            public void changedUpdate(DocumentEvent e) { onTextChange(); }
        });

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Progress: 0%");
        progressBar.setPreferredSize(new Dimension(0, 30));
        
        JScrollPane promptScroll = new JScrollPane(promptArea);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(promptScroll, BorderLayout.CENTER);
        panel.add(inputScroll, BorderLayout.SOUTH);
        
        // progress barrrrrrrrrr
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        startButton = createStyledButton("🚀 Start Test", new Color(52, 152, 219));
        startButton.setForeground(Color.BLACK);
        startButton.addActionListener(e -> startTest());
        
        pauseButton = createStyledButton("⏸️ Pause", new Color(241, 196, 15));
        pauseButton.setForeground(Color.BLACK);
        pauseButton.addActionListener(e -> togglePause());
        pauseButton.setEnabled(false);
        
        resetButton = createStyledButton("🔄 Reset", new Color(231, 76, 60));
        resetButton.setForeground(Color.BLACK);
        resetButton.addActionListener(e -> resetTest());
        
        hintButton = createStyledButton("💡 Hint", new Color(155, 89, 182));
        hintButton.setForeground(Color.BLACK);
        hintButton.addActionListener(e -> showHint());
        
        JButton newTextButton = createStyledButton("📝 New Text", new Color(46, 204, 113));
        newTextButton.setForeground(Color.BLACK);
        newTextButton.addActionListener(e -> loadNewText());
        
        panel.add(startButton);
        panel.add(pauseButton);
        panel.add(resetButton);
        panel.add(hintButton);
        panel.add(newTextButton);
        
        return panel;
    }
    
    private JPanel createRealTimeStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder("📊 Live Stats"),
            new EmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(250, 0));
        
        wpmLabel = createStatLabel("WPM: 0");
        accuracyLabel = createStatLabel("Accuracy: 100%");
        mistakeCountLabel = createStatLabel("Mistakes: 0");
        
        accuracyBar = new JProgressBar(0, 100);
        accuracyBar.setValue(100);
        accuracyBar.setStringPainted(true);
        accuracyBar.setString("Perfect!");
        
        // Add achievement notifications area
        JTextArea achievementsArea = new JTextArea(5, 20);
        achievementsArea.setEditable(false);
        achievementsArea.setBorder(BorderFactory.createTitledBorder("🏆 Achievements"));
        JScrollPane achievementsScroll = new JScrollPane(achievementsArea);
        
        panel.add(wpmLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(accuracyLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(mistakeCountLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(accuracyBar);
        panel.add(Box.createVerticalStrut(20));
        panel.add(achievementsScroll);
        
        return panel;
    }
    
    private void createStatsTab() {
        statsPanel = new JPanel(new BorderLayout());
        
        // stats panel (popup)
        JPanel summaryPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        summaryPanel.setBorder(new CompoundBorder(
            BorderFactory.createTitledBorder("📈 Performance Summary"),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        totalTestsLabel = new JLabel("Total Tests: 0", JLabel.CENTER);
        totalTestsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel avgWpmLabel = new JLabel("Avg WPM: 0", JLabel.CENTER);
        avgWpmLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel avgAccuracyLabel = new JLabel("Avg Accuracy: 0%", JLabel.CENTER);
        avgAccuracyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel totalTimeLabel = new JLabel("Total Time: 0 min", JLabel.CENTER);
        totalTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        JLabel improvementLabel = new JLabel("Improvement: +0%", JLabel.CENTER);
        improvementLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        summaryPanel.add(totalTestsLabel);
        summaryPanel.add(avgWpmLabel);
        summaryPanel.add(avgAccuracyLabel);
        summaryPanel.add(totalTimeLabel);
        summaryPanel.add(bestWpmLabel);
        summaryPanel.add(improvementLabel);
        
        String[] columnNames = {"Test #", "Date", "WPM", "Accuracy", "Time", "Difficulty", "Level"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        historyTable = new JTable(model);
        historyTable.setFont(new Font("Monospace", Font.PLAIN, 12));
        JScrollPane tableScroll = new JScrollPane(historyTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("📋 Test History"));
        
        // features of stats 
        JPanel statsControlPanel = new JPanel(new FlowLayout());
        JButton exportButton = createStyledButton("📤 Export Data", new Color(52, 152, 219));
        exportButton.setForeground(Color.BLACK);
        JButton clearHistoryButton = createStyledButton("🗑️ Clear History", new Color(231, 76, 60));
        clearHistoryButton.setForeground(Color.BLACK);
        JButton generateReportButton = createStyledButton("📊 Generate Report", new Color(46, 204, 113));
        generateReportButton.setForeground(Color.BLACK);
        
        exportButton.addActionListener(e -> exportData());
        clearHistoryButton.addActionListener(e -> clearHistory());
        generateReportButton.addActionListener(e -> generateReport());
        
        statsControlPanel.add(exportButton);
        statsControlPanel.add(clearHistoryButton);
        statsControlPanel.add(generateReportButton);
        
        statsPanel.add(summaryPanel, BorderLayout.NORTH);
        statsPanel.add(tableScroll, BorderLayout.CENTER);
        statsPanel.add(statsControlPanel, BorderLayout.SOUTH);
        
        mainTabs.addTab("📊 Statistics", statsPanel);
    }
    
    private void createSettingsTab() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // thmeseeeeeee
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.setBorder(BorderFactory.createTitledBorder("🎨 Appearance"));
        themePanel.add(new JLabel("Theme:"));
        themeCombo = new JComboBox<>(themes.keySet().toArray(new String[0]));
        themeCombo.setSelectedItem(currentTheme);
        themeCombo.addActionListener(e -> changeTheme());
        themePanel.add(themeCombo);
        
        themePanel.add(Box.createHorizontalStrut(20));
        themePanel.add(new JLabel("Font Size:"));
        fontSizeCombo = new JComboBox<>(new String[]{"12", "14", "16", "18", "20", "22", "24"});
        fontSizeCombo.setSelectedItem("18");
        fontSizeCombo.addActionListener(e -> changeFontSize());
        themePanel.add(fontSizeCombo);
        
        JPanel audioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        audioPanel.setBorder(BorderFactory.createTitledBorder("🔊 Audio"));
        soundCheckbox = new JCheckBox("Enable Sound Effects", soundEnabled);
        soundCheckbox.addActionListener(e -> soundEnabled = soundCheckbox.isSelected());
        audioPanel.add(soundCheckbox);
        
        audioPanel.add(Box.createHorizontalStrut(20));
        audioPanel.add(new JLabel("Volume:"));
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(100, 30));
        audioPanel.add(volumeSlider);

        JPanel gameplayPanel = new JPanel();
        gameplayPanel.setLayout(new BoxLayout(gameplayPanel, BoxLayout.Y_AXIS));
        gameplayPanel.setBorder(BorderFactory.createTitledBorder("🎮 Gameplay"));
        
        realTimeCheckbox = new JCheckBox("Real-time Statistics", true);
        mistakeHighlightCheckbox = new JCheckBox("Highlight Mistakes", true);
        JCheckBox autoAdvanceCheckbox = new JCheckBox("Auto-advance Difficulty", false);
        JCheckBox showKeyboardCheckbox = new JCheckBox("Show Virtual Keyboard", false);
        
        gameplayPanel.add(realTimeCheckbox);
        gameplayPanel.add(mistakeHighlightCheckbox);
        gameplayPanel.add(autoAdvanceCheckbox);
        gameplayPanel.add(showKeyboardCheckbox);
        
        // not working wait -- need to work on it
        JPanel customTextPanel = new JPanel(new BorderLayout());
        customTextPanel.setBorder(BorderFactory.createTitledBorder("📝 Custom Text"));
        JTextArea customTextArea = new JTextArea(5, 40);
        JButton loadCustomButton = createStyledButton("Load Custom Text", new Color(155, 89, 182));
  
        loadCustomButton.setForeground(Color.BLACK);
        customTextPanel.add(new JScrollPane(customTextArea), BorderLayout.CENTER);
        customTextPanel.add(loadCustomButton, BorderLayout.SOUTH);
        
        settingsPanel.add(themePanel);
        settingsPanel.add(Box.createVerticalStrut(10));
        settingsPanel.add(audioPanel);
        settingsPanel.add(Box.createVerticalStrut(10));
        settingsPanel.add(gameplayPanel);
        settingsPanel.add(Box.createVerticalStrut(10));
        settingsPanel.add(customTextPanel);
        
        mainTabs.addTab("⚙️ Settings", settingsPanel);
    }
    
    private void createLeaderboardTab() {
        leaderboardPanel = new JPanel(new BorderLayout());
        
        String[] leaderColumnNames = {"Rank", "Date", "WPM", "Accuracy", "Difficulty", "Achievement"};
        DefaultTableModel leaderModel = new DefaultTableModel(leaderColumnNames, 0);
        leaderboardTable = new JTable(leaderModel);
        leaderboardTable.setFont(new Font("Monospace", Font.PLAIN, 12));
        
        JScrollPane leaderScroll = new JScrollPane(leaderboardTable);
        leaderScroll.setBorder(BorderFactory.createTitledBorder("🏆 Personal Best Records"));
        
        JPanel achievementPanel = new JPanel(new GridLayout(3, 3, 10, 10));
        achievementPanel.setBorder(BorderFactory.createTitledBorder("🎖️ Achievements"));
        
        String[] achievements = {
            "First Steps", "Speed Demon", "Accuracy Master",
            "Consistency King", "Marathon Typist", "Perfect Game",
            "Level Up", "Mistake Free", "Lightning Fast"
        };
        
        for (String achievement : achievements) {
            JPanel achPanel = new JPanel(new BorderLayout());
            achPanel.setBorder(BorderFactory.createEtchedBorder());
            achPanel.add(new JLabel(achievement, JLabel.CENTER), BorderLayout.CENTER);
            achPanel.add(new JLabel("🔒", JLabel.CENTER), BorderLayout.SOUTH);
            achievementPanel.add(achPanel);
        }
        
        leaderboardPanel.add(leaderScroll, BorderLayout.CENTER);
        leaderboardPanel.add(achievementPanel, BorderLayout.SOUTH);
        
        mainTabs.addTab("🏆 Leaderboard", leaderboardPanel);
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(140, 35));
        return button;
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setBorder(BorderFactory.createEtchedBorder());
        label.setPreferredSize(new Dimension(200, 30));
        return label;
    }
    
    private void setupKeyboardShortcuts() {
        KeyStroke startKey = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        KeyStroke resetKey = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        KeyStroke pauseKey = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(startKey, "start");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(resetKey, "reset");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pauseKey, "pause");
        
        getRootPane().getActionMap().put("start", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { startTest(); }
        });
        getRootPane().getActionMap().put("reset", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { resetTest(); }
        });
        getRootPane().getActionMap().put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { togglePause(); }
        });
    }
    
    private void loadNewText() {
        String difficulty = (String) difficultyCombo.getSelectedItem();
        String[] texts = DIFFICULTY_TEXTS.get(difficulty);
        currentText = texts[random.nextInt(texts.length)];
        promptArea.setText(currentText);
        highlightText();
    }
    
    private void highlightText() {
        StyledDocument doc = new DefaultStyledDocument();
        Style style = doc.addStyle("highlight", null);
        StyleConstants.setBackground(style, new Color(255, 255, 200));
        
        try {
            doc.insertString(0, currentText, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void startTest() {
        if (currentText == null || currentText.isEmpty()) {
            loadNewText();
        }
        
        inputArea.setText("");
        inputArea.setEnabled(true);
        inputArea.requestFocus();
        testStarted = false;
        testPaused = false;
        mistakeCount = 0;
        pausedTime = 0;
        
        startButton.setEnabled(false);
        pauseButton.setEnabled(true);
        timerLabel.setText("Start typing to begin...");
        updateProgress(0);
        
        playSound("start");
    }
    
    private void togglePause() {
        if (!testStarted) return;
        
        if (testPaused) {
            // Resume
            testPaused = false;
            pausedTime += System.currentTimeMillis() - startTime;
            startTime = System.currentTimeMillis();
            inputArea.setEnabled(true);
            inputArea.requestFocus();
            pauseButton.setText("⏸️ Pause");
            uiTimer.start();
            timerLabel.setText("Resumed - Keep typing!");
        } else {
            // Pause
            testPaused = true;
            uiTimer.stop();
            inputArea.setEnabled(false);
            pauseButton.setText("▶️ Resume");
            timerLabel.setText("⏸️ PAUSED - Click Resume to continue");
        }
    }
    
    private void resetTest() {
        uiTimer.stop();
        countdownTimer.stop();
        testStarted = false;
        testPaused = false;
        mistakeCount = 0;
        pausedTime = 0;
        
        inputArea.setText("");
        inputArea.setEnabled(true);
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        pauseButton.setText("⏸️ Pause");
        
        timerLabel.setText("Ready to Start!");
        updateStats(0, 100, "Ready");
        updateProgress(0);
        loadNewText();
    }
    
    private void onTextChange() {
        if (!testStarted && !inputArea.getText().isEmpty()) {
            testStarted = true;
            startTime = System.currentTimeMillis();
            uiTimer.start();
            timerLabel.setText("Timer started! 🔥");
            playSound("type");
        }
        
        if (testStarted && !testPaused) {
            String userText = inputArea.getText();
            
            // khatam hua?
            if (userText.equals(currentText)) {
                completeTest();
                return;
            }
            
            // Update progress
            double progress = (double) userText.length() / currentText.length() * 100;
            updateProgress(Math.min(100, progress));
            
            // Highlight mistakes in real-time
            if (mistakeHighlightCheckbox.isSelected()) {
                highlightMistakes(userText);
            }
        }
    }
    
    private void highlightMistakes(String userText) {
        // in real time
        int mistakes = 0;
        for (int i = 0; i < Math.min(userText.length(), currentText.length()); i++) {
            if (userText.charAt(i) != currentText.charAt(i)) {
                mistakes++;
            }
        }
        
        if (mistakes != mistakeCount) {
            mistakeCount = mistakes;
            playSound("error");
        }
    }
    
    private void updateRealTimeStats() {
        if (!testStarted || testPaused) return;
        
        String userText = inputArea.getText();
        double timeInSeconds = (System.currentTimeMillis() - startTime - pausedTime) / 1000.0;
        
        if (timeInSeconds <= 0) return;
        
        int wordsTyped = userText.isEmpty() ? 0 : userText.split("\\s+").length;
        double wpm = (wordsTyped / timeInSeconds) * 60;
        int accuracy = calculateAccuracy(currentText, userText);
        String level = getSkillLevel(wpm, accuracy);
        
        updateStats(wpm, accuracy, level);
        updateTimer(timeInSeconds);
        
        // xp kese badegi
        if (wpm > 0) {
            double xpGain = wpm * 0.1 + (accuracy - 90) * 0.2;
            if (xpGain > 0) {
                totalXP += xpGain;
                updateLevel();
            }
        }
    }
    
    private void updateTimer(double seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        timerLabel.setText(String.format("⏱️ Time: %02d:%02d", minutes, secs));
    }
    
    private void updateProgress(double progress) {
        progressBar.setValue((int) progress);
        progressBar.setString(String.format("Progress: %.1f%%", progress));
        
        // 3 colors for levels of progression
        if (progress < 30) {
            progressBar.setForeground(new Color(231, 76, 60));
        } else if (progress < 70) {
            progressBar.setForeground(new Color(241, 196, 15));
        } else {
            progressBar.setForeground(new Color(46, 204, 113));
        }
    }
    
      private void updateLevel() {
        int newLevel = (int) (totalXP / BASE_XP_PER_LEVEL) + 1;
        if (newLevel > currentLevel) {
            currentLevel = newLevel;
            updateLevelDisplay();
            showLevelUpAnimation();
            playSound("levelup");
        }
        
        double xpInCurrentLevel = totalXP % BASE_XP_PER_LEVEL;
        levelProgressBar.setValue((int) xpInCurrentLevel);
        levelProgressBar.setString(String.format("XP: %.0f/%d", xpInCurrentLevel, BASE_XP_PER_LEVEL));
    }
    private void updateLevelDisplay() {
        setTitle("🚀 Advanced Typing Speed Tester Pro - Level " + currentLevel);
        levelLabel.setText("Level " + currentLevel);
    }
    private void showLevelUpAnimation() {
        JDialog levelUpDialog = new JDialog(this, "Level Up!", true);
        levelUpDialog.setLayout(new BorderLayout());
        
        JLabel levelUpLabel = new JLabel("🎉 LEVEL UP! 🎉", JLabel.CENTER);
        levelUpLabel.setFont(new Font("Arial", Font.BOLD, 24));
        levelUpLabel.setForeground(new Color(46, 204, 113));
        
        JLabel newLevelLabel = new JLabel("You are now Level " + currentLevel + "!", JLabel.CENTER);
        newLevelLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton okButton = new JButton("Awesome!");
        okButton.addActionListener(e -> levelUpDialog.dispose());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(20));
        panel.add(levelUpLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(newLevelLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(okButton);
        panel.add(Box.createVerticalStrut(20));
        
        levelUpDialog.add(panel);
        levelUpDialog.pack();
        levelUpDialog.setLocationRelativeTo(this);
        levelUpDialog.setVisible(true);
    }
    
    private void completeTest() {
        uiTimer.stop();
        testStarted = false;
        inputArea.setEnabled(false);
        
        double timeInSeconds = (System.currentTimeMillis() - startTime - pausedTime) / 1000.0;
        int wordsTyped = inputArea.getText().split("\\s+").length;
        double wpm = (wordsTyped / timeInSeconds) * 60;
        int accuracy = calculateAccuracy(currentText, inputArea.getText());
        String difficulty = (String) difficultyCombo.getSelectedItem();
        
        // Update statistics
        totalTests++;
        totalTypingTime += (long) timeInSeconds;
        if (wpm > bestWPM) {
            bestWPM = wpm;
            bestWpmLabel.setText("Best: " + (int) bestWPM + " WPM");
        }
        
        // streak ka kaam
        if (accuracy >= 95) {
            currentStreak++;
            if (currentStreak > bestStreak) {
                bestStreak = currentStreak;
            }
        } else {
            currentStreak = 0;
        }
        streakLabel.setText("Streak: " + currentStreak + " (Best: " + bestStreak + ")");
        
        // to history
        TestResult result = new TestResult(wpm, accuracy, timeInSeconds, difficulty, currentLevel);
        testHistory.add(result);
        updateHistoryTable();
        
        // XP reward
        double xpReward = wpm * 0.5 + accuracy * 0.3 + (accuracy >= 100 ? 50 : 0);
        totalXP += xpReward;
        updateLevel();
        
        // Check achievements
        checkAchievements(wpm, accuracy, timeInSeconds);
        
        updateStats(wpm, accuracy, getSkillLevel(wpm, accuracy));
        timerLabel.setText(String.format("✅ Completed in %.1f seconds!", timeInSeconds));
        
        startButton.setEnabled(true);
        pauseButton.setEnabled(false);
        
        playSound("complete");
        showCompletionDialog(wpm, accuracy, timeInSeconds, xpReward);
    }
    
    private void showCompletionDialog(double wpm, int accuracy, double time, double xpGained) {
        JDialog completionDialog = new JDialog(this, "Test Completed!", true);
        completionDialog.setLayout(new BorderLayout());
        
        JPanel resultsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        resultsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        resultsPanel.add(new JLabel("⚡ Words Per Minute:"));
        resultsPanel.add(new JLabel(String.format("%.1f WPM", wpm)));
        
        resultsPanel.add(new JLabel("🎯 Accuracy:"));
        resultsPanel.add(new JLabel(accuracy + "%"));
        
        resultsPanel.add(new JLabel("⏱️ Time Taken:"));
        resultsPanel.add(new JLabel(String.format("%.1f seconds", time)));
        
        resultsPanel.add(new JLabel("❌ Mistakes:"));
        resultsPanel.add(new JLabel(String.valueOf(mistakeCount)));
        
        resultsPanel.add(new JLabel("🌟 XP Gained:"));
        resultsPanel.add(new JLabel(String.format("+%.0f XP", xpGained)));
        
        resultsPanel.add(new JLabel("🏆 Skill Level:"));
        resultsPanel.add(new JLabel(getSkillLevel(wpm, accuracy)));
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton newTestButton = createStyledButton("🚀 New Test", new Color(0,0,0));
              newTestButton.setForeground(Color.BLACK);
        JButton viewStatsButton = createStyledButton("📊 View Stats", new Color(0, 0, 0));
              viewStatsButton.setForeground(Color.BLACK);
        JButton closeButton = createStyledButton("✖️ Close", new Color(0, 0, 0));
              closeButton.setForeground(Color.BLACK);
        
        newTestButton.addActionListener(e -> {
            completionDialog.dispose();
            startTest();
        });
        viewStatsButton.addActionListener(e -> {
            completionDialog.dispose();
            mainTabs.setSelectedIndex(1); // Switch to stats tab
        });
        closeButton.addActionListener(e -> completionDialog.dispose());
        
        buttonPanel.add(newTestButton);
        buttonPanel.add(viewStatsButton);
        buttonPanel.add(closeButton);
        
        completionDialog.add(resultsPanel, BorderLayout.CENTER);
        completionDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        completionDialog.pack();
        completionDialog.setLocationRelativeTo(this);
        completionDialog.setVisible(true);
    }
    
    private void checkAchievements(double wpm, int accuracy, double time) {
        // Implementation for achievement checking
        if (totalTests == 1) {
            showAchievement("🏁 First Steps", "Completed your first typing test!");
        }
        if (wpm >= 80) {
            showAchievement("⚡ Speed Demon", "Typed at 80+ WPM!");
        }
        if (accuracy == 100) {
            showAchievement("🎯 Perfect Game", "100% accuracy achieved!");
        }
        if (currentStreak >= 5) {
            showAchievement("🔥 On Fire", "5 accurate tests in a row!");
        }
    }
    
    private void showAchievement(String title, String description) {
        JOptionPane.showMessageDialog(this,
            description,
            "🏆 Achievement Unlocked: " + title,
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showHint() {
        String userText = inputArea.getText();
        if (userText.length() < currentText.length()) {
            char nextChar = currentText.charAt(userText.length());
            JOptionPane.showMessageDialog(this,
                "Next character: '" + nextChar + "'",
                "💡 Hint",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void changeDifficulty() {
        loadNewText();
        resetTest();
    }
    
    private void changeTheme() {
        currentTheme = (String) themeCombo.getSelectedItem();
        applyTheme();
    }
    
    private void applyTheme() {
        Color themeColor = themes.get(currentTheme);
        
        // Apply theme to main components
        mainTabs.setBackground(themeColor);
        if (promptArea != null) {
            promptArea.setBackground(themeColor.brighter());
        }
        
        // Update all panels
        updatePanelTheme(typingPanel, themeColor);
        updatePanelTheme(statsPanel, themeColor);
        updatePanelTheme(settingsPanel, themeColor);
        updatePanelTheme(leaderboardPanel, themeColor);
        
        repaint();
    }
    
    private void updatePanelTheme(JPanel panel, Color color) {
        if (panel != null) {
            panel.setBackground(color.brighter());
        }
    }
    
    private void changeFontSize() {
        int size = Integer.parseInt((String) fontSizeCombo.getSelectedItem());
        typingFont = new Font("Monospace", Font.PLAIN, size);
        if (inputArea != null) {
            inputArea.setFont(typingFont);
            promptArea.setFont(typingFont);
        }
    }
    
    private void updateStats(double wpm, int accuracy, String level) {
        wpmLabel.setText(String.format("WPM: %.1f", wpm));
        accuracyLabel.setText(String.format("Accuracy: %d%%", accuracy));
        mistakeCountLabel.setText("Mistakes: " + mistakeCount);
        
        accuracyBar.setValue(accuracy);
        accuracyBar.setString(String.format("%d%% Accurate", accuracy));
        
        // Color coding for accuracy
        if (accuracy >= 95) {
            accuracyBar.setForeground(new Color(46, 204, 113));
        } else if (accuracy >= 85) {
            accuracyBar.setForeground(new Color(241, 196, 15));
        } else {
            accuracyBar.setForeground(new Color(231, 76, 60));
        }
        
        totalTestsLabel.setText("Total Tests: " + totalTests);
    }
    
    private void updateHistoryTable() {
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0); // Clear existing data
        
        for (int i = 0; i < testHistory.size(); i++) {
            TestResult result = testHistory.get(i);
            model.addRow(new Object[]{
                i + 1,
                result.date,
                String.format("%.1f", result.wpm),
                result.accuracy + "%",
                String.format("%.1fs", result.timeSeconds),
                result.difficulty,
                "Level " + result.level
            });
        }
    }
    
    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Typing Data");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                writer.println("Test,Date,WPM,Accuracy,Time,Difficulty,Level");
                for (int i = 0; i < testHistory.size(); i++) {
                    TestResult result = testHistory.get(i);
                    writer.printf("%d,%s,%.1f,%d,%.1f,%s,%d%n",
                        i + 1, result.date, result.wpm, result.accuracy,
                        result.timeSeconds, result.difficulty, result.level);
                }
                JOptionPane.showMessageDialog(this, "Data exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearHistory() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all test history?",
            "Clear History", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            testHistory.clear();
            updateHistoryTable();
            totalTests = 0;
            bestWPM = 0;
            totalTypingTime = 0;
            currentStreak = 0;
            bestStreak = 0;
            updateStats(0, 100, "Ready");
        }
    }
    
    private void generateReport() {
        if (testHistory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available for report generation.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StringBuilder report = new StringBuilder();
        report.append("📊 TYPING PERFORMANCE REPORT\n");
        report.append("================================\n\n");
        
        double avgWpm = testHistory.stream().mapToDouble(r -> r.wpm).average().orElse(0);
        double avgAccuracy = testHistory.stream().mapToDouble(r -> r.accuracy).average().orElse(0);
        double maxWpm = testHistory.stream().mapToDouble(r -> r.wpm).max().orElse(0);
        double minWpm = testHistory.stream().mapToDouble(r -> r.wpm).min().orElse(0);
        
        report.append(String.format("Total Tests Completed: %d\n", totalTests));
        report.append(String.format("Total Typing Time: %.1f minutes\n", totalTypingTime / 60.0));
        report.append(String.format("Current Level: %d\n", currentLevel));
        report.append(String.format("Total XP: %.0f\n\n", totalXP));
        
        report.append("PERFORMANCE METRICS:\n");
        report.append(String.format("• Average WPM: %.1f\n", avgWpm));
        report.append(String.format("• Best WPM: %.1f\n", maxWpm));
        report.append(String.format("• Lowest WPM: %.1f\n", minWpm));
        report.append(String.format("• Average Accuracy: %.1f%%\n", avgAccuracy));
        report.append(String.format("• Best Streak: %d tests\n", bestStreak));
        
        JTextArea reportArea = new JTextArea(report.toString());
        reportArea.setFont(new Font("Monospace", Font.PLAIN, 12));
        reportArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "Performance Report", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void playSound(String soundType) {
        if (!soundEnabled) return;
        
        // beeeeeeeeeeeeeps
        switch (soundType) {
            case "start":
                Toolkit.getDefaultToolkit().beep();
                break;
            case "complete":
                //double beep
                Toolkit.getDefaultToolkit().beep();
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                Toolkit.getDefaultToolkit().beep();
                break;
            case "error":
                // low pitch
                Toolkit.getDefaultToolkit().beep();
                break;
            case "levelup":
                // 3 beep for next nevel in 150mil gap
                for (int i = 0; i < 3; i++) {
                    Toolkit.getDefaultToolkit().beep();
                    try { Thread.sleep(150); } catch (InterruptedException e) {}
                }
                break;
        }
    }
    
    private int calculateAccuracy(String original, String typed) {
        if (typed.isEmpty()) return 100;
        
        int matches = 0;
        int minLength = Math.min(original.length(), typed.length());
        
        for (int i = 0; i < minLength; i++) {
            if (original.charAt(i) == typed.charAt(i)) {
                matches++;
            }
        }
        
        // penalty for extra characters
        int penalty = Math.abs(original.length() - typed.length());
        int totalCorrect = matches - penalty;
        
        return Math.max(0, (int) ((totalCorrect * 100.0) / original.length()));
    }
    
    private String getSkillLevel(double wpm, int accuracy) {
        if (accuracy < 70) return "Practice Accuracy";
        if (wpm < 20) return "Beginner";
        if (wpm < 30) return "Novice";
        if (wpm < 40) return "Intermediate";
        if (wpm < 50) return "Good";
        if (wpm < 60) return "Advanced";
        if (wpm < 70) return "Expert";
        if (wpm < 80) return "Master";
        if (wpm < 100) return "Lightning";
        return "Typing God";
    }
    
    // Test Result class for storing history
    private static class TestResult {
        final double wpm;
        final int accuracy;
        final double timeSeconds;
        final String difficulty;
        final int level;
        final String date;
        
        TestResult(double wpm, int accuracy, double timeSeconds, String difficulty, int level) {
            this.wpm = wpm;
            this.accuracy = accuracy;
            this.timeSeconds = timeSeconds;
            this.difficulty = difficulty;
            this.level = level;
            this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        }
    }
    
    // Main method - yahase start
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new TypingSpeedTester().setVisible(true);
        });
    }
}