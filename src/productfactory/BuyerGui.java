package productfactory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BuyerGui extends JFrame {
    int ec;
    CustomerAgent c = new CustomerAgent();
    String title;

    private static final long serialVersionUID = 1L;

    private CustomerAgent customer;

    private JTextField titleField, creditField;

    BuyerGui(CustomerAgent c) {
        super(/*c.getLocalName()*/);

        customer = c;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 2));
        p.add(new JLabel("Credit"));
        creditField = new JTextField(15);
        p.add(creditField);
        getContentPane().add(p, BorderLayout.CENTER);
        JButton orderButton = new JButton("Order");
        orderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                String credit = creditField.getText().trim();
                ec = Integer.parseInt(credit);
                c.updateOrder(ec);
                creditField.setText("");
            }
        });
        p = new JPanel();
        p.add(orderButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes 
        // the GUI using the button on the upper right corner	
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //customer.doDelete();
            }
        });

        setResizable(false);
    }

    public void AgentshowGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public String getBookTitle() {
        String t = title;
        return t;
    }

    public int getCredit() {
        String cridet = creditField.getText();
        int cred = Integer.parseInt(cridet);
        return cred;
    }

    public void getMessage(String message) {
        JOptionPane p = new JOptionPane(message);
    }
}
