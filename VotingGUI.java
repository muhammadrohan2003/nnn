import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;

public class VotingGUI extends JFrame {
    private DatabaseManager db;
    private JTextField voterIdField;
    private JComboBox<String> candidateBox;
    private JTextArea resultArea;
    
    public VotingGUI() {
        db = new DatabaseManager();
        setupGUI();
    }
    
    private void setupGUI() {
        setTitle("Online Voting System");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputPanel.add(new JLabel("Voter ID:"));
        voterIdField = new JTextField();
        inputPanel.add(voterIdField);
        
        inputPanel.add(new JLabel("Select Candidate:"));
        candidateBox = new JComboBox<>(new String[]{
            "Candidate A", "Candidate B", "Candidate C"
        });
        inputPanel.add(candidateBox);
        
        JButton voteBtn = new JButton("Cast Vote");
        voteBtn.addActionListener(e -> handleVote());
        inputPanel.add(voteBtn);
        
        JButton resultsBtn = new JButton("View Results");
        resultsBtn.addActionListener(e -> displayResults());
        inputPanel.add(resultsBtn);
        
        add(inputPanel, BorderLayout.NORTH);
        
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton registerBtn = new JButton("Register New Voter");
        registerBtn.addActionListener(e -> handleRegister());
        add(registerBtn, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void handleRegister() {
        String voterId = JOptionPane.showInputDialog(this, "Enter Voter ID:");
        if (voterId == null || voterId.trim().isEmpty()) return;
        
        if (db.registerVoter(voterId.trim())) {
            JOptionPane.showMessageDialog(this, "Voter registered successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Error: Voter ID already exists!");
        }
    }
    
    private void handleVote() {
        String voterId = voterIdField.getText().trim();
        String candidate = (String) candidateBox.getSelectedItem();
        
        if (voterId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Voter ID!");
            return;
        }
        
        if (!db.voterExists(voterId)) {
            JOptionPane.showMessageDialog(this, 
                "Voter ID not found! Please register first.");
            return;
        }
        
        if (db.hasVoted(voterId)) {
            JOptionPane.showMessageDialog(this, "You have already voted!");
            return;
        }
        
        if (db.castVote(voterId, candidate)) {
            JOptionPane.showMessageDialog(this, 
                "Vote cast successfully for " + candidate + "!");
            voterIdField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Error casting vote!");
        }
    }
    
    private void displayResults() {
        try {
            ResultSet rs = db.getResults();
            StringBuilder results = new StringBuilder();
            results.append("=== VOTING RESULTS ===\n\n");
            
            int total = 0;
            while (rs != null && rs.next()) {
                String candidate = rs.getString("candidate");
                int votes = rs.getInt("votes");
                total += votes;
                results.append(String.format("%-15s: %d votes\n", 
                    candidate, votes));
            }
            
            results.append("\nTotal Votes: ").append(total);
            resultArea.setText(results.toString());
        } catch (Exception e) {
            e.printStackTrace();
            resultArea.setText("Error loading results!");
        }
    }
}