import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GasPumpUI extends JFrame {
    private JComboBox<String> fuelTypeCombo;
    private JTextField moneyField;
    private JTextArea resultArea;
    private JTextArea historyArea;
    private JButton pumpButton;
    private JButton stopButton;
    private Timer timer;

    private double amountEntered;
    private double currentGallons = 0.0;
    private double pricePerGallon = 0.0;

    private final double REGULAR_PRICE = 3.49;
    private final double PLUS_PRICE = 3.89;
    private final double PREMIUM_PRICE = 4.19;
    private final double DIESEL_PRICE = 4.49;

    private List<String> transactionHistory = new ArrayList<>();

    public GasPumpUI() {
        setTitle("⛽ Java Gas Pump");
        setSize(500, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 248, 255));

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Pump Info"));

        fuelTypeCombo = new JComboBox<>(new String[]{"Regular", "Plus", "Premium", "Diesel"});
        moneyField = new JTextField();

        pumpButton = new JButton("Start Pumping");
        stopButton = new JButton("Emergency Stop");
        stopButton.setEnabled(false);

        JButton showHistoryButton = new JButton("Show Transaction History");

        inputPanel.add(new JLabel("Fuel Type:"));
        inputPanel.add(fuelTypeCombo);
        inputPanel.add(new JLabel("Amount ($):"));
        inputPanel.add(moneyField);
        inputPanel.add(new JLabel());
        inputPanel.add(pumpButton);
        inputPanel.add(new JLabel());
        inputPanel.add(stopButton);
        inputPanel.add(new JLabel());
        inputPanel.add(showHistoryButton);

        add(inputPanel, BorderLayout.NORTH);

        // Result Area
        resultArea = new JTextArea(5, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setBorder(BorderFactory.createTitledBorder("Current Pumping Status"));

        JScrollPane resultScroll = new JScrollPane(resultArea);
        add(resultScroll, BorderLayout.CENTER);

        // History Area
        historyArea = new JTextArea(8, 30);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        JScrollPane historyScroll = new JScrollPane(historyArea);
        add(historyScroll, BorderLayout.SOUTH);
        historyArea.setVisible(false);

        // Timer logic
        timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                double rate = 0.005; // gallons per tick
                double nextGallons = currentGallons + rate;
                double nextCost = nextGallons * pricePerGallon;

                if (nextCost < amountEntered) {
                    currentGallons = Math.round(nextGallons * 1000.0) / 1000.0;
                    updateResult();
                } else {
                    // Final precision adjustment
                    currentGallons = Math.round((amountEntered / pricePerGallon) * 1000.0) / 1000.0;
                    updateResult();
                    stopPumping("Pumping complete!");
                }
            }
        });

        // Button listeners
        pumpButton.addActionListener(e -> startPumping());
        stopButton.addActionListener(e -> stopPumping("Emergency stop activated!"));
        showHistoryButton.addActionListener(e -> toggleHistory());
    }

    private void startPumping() {
        try {
            amountEntered = Double.parseDouble(moneyField.getText());
            String fuelType = (String) fuelTypeCombo.getSelectedItem();

            switch (fuelType) {
                case "Regular": pricePerGallon = REGULAR_PRICE; break;
                case "Plus": pricePerGallon = PLUS_PRICE; break;
                case "Premium": pricePerGallon = PREMIUM_PRICE; break;
                case "Diesel": pricePerGallon = DIESEL_PRICE; break;
            }

            currentGallons = 0.0;
            resultArea.setText("Starting pump...\n");
            pumpButton.setEnabled(false);
            stopButton.setEnabled(true);
            timer.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid dollar amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopPumping(String message) {
        timer.stop();
        updateResult();
        resultArea.append("\n\n" + message);
        saveTransaction();
        pumpButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void updateResult() {
        double cost = currentGallons * pricePerGallon;
        cost = Math.round(cost * 100.0) / 100.0;

        resultArea.setText(String.format(
                "Fuel Type: %s\nPrice per Gallon: $%.2f\nAmount Entered: $%.2f\nGallons Pumped: %.3f\nCost So Far: $%.2f",
                fuelTypeCombo.getSelectedItem(), pricePerGallon, amountEntered, currentGallons, cost
        ));
    }

    private void saveTransaction() {
        double cost = Math.round(currentGallons * pricePerGallon * 100.0) / 100.0;
        String record = String.format("Fuel: %s | Amount: $%.2f | Gallons: %.3f | Cost: $%.2f",
                fuelTypeCombo.getSelectedItem(), amountEntered, currentGallons, cost);
        transactionHistory.add(record);
    }

    private void toggleHistory() {
        if (historyArea.isVisible()) {
            historyArea.setVisible(false);
            historyArea.setText("");
        } else {
            StringBuilder history = new StringBuilder();
            for (String record : transactionHistory) {
                history.append(record).append("\n");
            }
            historyArea.setText(history.toString().isEmpty() ? "No transactions yet." : history.toString());
            historyArea.setVisible(true);
        }
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GasPumpUI().setVisible(true));
    }
}
