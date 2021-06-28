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

class FactorySellerGui extends JFrame {

    private static final long serialVersionUID = 1L;

    private FactorySellerAgent myAgent;
    private FactorySellerAgent f;
    private CustomerAgent c;
    public int pr;
    public int pc;
    private JTextField NameField, priceField;

    FactorySellerGui(FactorySellerAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Product name:"));
        NameField = new JTextField(15);
        p.add(NameField);
        p.add(new JLabel("Price:"));
        priceField = new JTextField(15);
        p.add(priceField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                try {
                    String name = NameField.getText().trim();
                    String price = priceField.getText().trim();
                    pr = Integer.parseInt(price);
                    myAgent.updateCatalogue(name, pr);
                    NameField.setText("");
                    priceField.setText("");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(FactorySellerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public int getPrice() {
        String cridet = priceField.getText();
        int price = Integer.parseInt(cridet);
        return price;
    }
}
