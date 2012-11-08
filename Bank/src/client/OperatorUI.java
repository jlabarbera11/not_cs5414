// Removed my oracle and ghetto messaging. Thought the GUI looked nice so I left the barebones

public class OperatorUI extends javax.swing.JFrame 
{

    private javax.swing.JLabel branchLabel;
    private javax.swing.JTextField branchText;
    private javax.swing.JButton failure;
    private javax.swing.JPanel jPanel;
    private javax.swing.JTextField output;
    private javax.swing.JButton recovery;

    public OperatorUI() 
    {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    private void initComponents() 
    {

        jPanel = new javax.swing.JPanel();
        failure = new javax.swing.JButton();
        recovery = new javax.swing.JButton();
        branchLabel = new javax.swing.JLabel();
        branchText = new javax.swing.JTextField();
        output = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Operator", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Baskerville", 0, 14))); // NOI18N

        failure.setFont(new java.awt.Font("Baskerville", 1, 24));
        failure.setText("Failure");
        failure.setToolTipText("hit after forcing failure");
        failure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                failureActionPerformed(evt);
            }
        });

        recovery.setFont(new java.awt.Font("Baskerville", 1, 24));
        recovery.setText("Recovery");
        recovery.setToolTipText("hit after restarting processor");
        recovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recoveryActionPerformed(evt);
            }
        });

        branchLabel.setFont(new java.awt.Font("Baskerville", 1, 18));
        branchLabel.setText("Branch:");

        branchText.setFont(new java.awt.Font("Baskerville", 0, 18));
        branchText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                branchTextActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                        .addComponent(failure, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(recovery, javax.swing.GroupLayout.DEFAULT_SIZE, 181, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(branchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(branchText, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(87, 87, 87))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                        .addComponent(output)
                        .addContainerGap())))
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(branchText, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(branchLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(failure, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                    .addComponent(recovery, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(output, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void branchTextActionPerformed(java.awt.event.ActionEvent evt) 
    {
        branchText.setText("");
    }

    private boolean outputReply(String event) 
    {
        try {
            Integer branch = Integer.parseInt(branchText.getText());
            output.setText("Branch " + String.valueOf(branch) + " " + event);
            return true;
        } catch (NumberFormatException e) {
            output.setText("Input a valid branch");
            return false;
        }
    }

    private void failureActionPerformed(java.awt.event.ActionEvent evt) 
    {
        outputReply("failure");
        // Add Messaging response to Oracle
    }

    private void recoveryActionPerformed(java.awt.event.ActionEvent evt) 
    {
        outputReply("recovery");
        // Add Messaging response to Oracle
    }

    public static void main(String args[]) 
    {
        // TODO: Add Messaging
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new OperatorUI().setVisible(true);
            }
        });
    }
}