package Graphwar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HistoryWindow extends JFrame {
    private JList<String> historyList;
    private DefaultListModel<String> listModel;
    private JButton copyButton;
    private JButton deleteButton;
    private JButton clearButton;
    private FunctionHistory history;
    private GameScreen gameScreen;
    
    public HistoryWindow(GameScreen gameScreen, FunctionHistory history) {
        super("Function History");
        this.gameScreen = gameScreen;
        this.history = history;
        
        setSize(400, 400);
        setLocationRelativeTo(gameScreen);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        listModel = new DefaultListModel<>();
        historyList = new JList<>(listModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }
        });
        historyList.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClicked(MouseEvent e) {
                copySelected();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setPreferredSize(new Dimension(380, 320));
        add(scrollPane, BorderLayout.CENTER);
        
        copyButton = new JButton("Copy to Input");
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copySelected();
            }
        });
        
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        
        clearButton = new JButton("Clear All");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                history.clear();
                refreshList();
            }
        });
        
        javax.swing.JPanel buttonPanel = new javax.swing.JPanel();
        buttonPanel.add(copyButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateButtons();
        refreshList();
    }
    
    public void refreshList() {
        listModel.clear();
        List<String> functions = history.getAll();
        for (String func : functions) {
            listModel.addElement(func);
        }
        updateButtons();
    }
    
    private void updateButtons() {
        boolean hasSelection = historyList.getSelectedIndex() >= 0;
        copyButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        clearButton.setEnabled(history.size() > 0);
    }
    
    private void copySelected() {
        int index = historyList.getSelectedIndex();
        if (index >= 0) {
            String function = history.get(index);
            gameScreen.setFunctionText(function);
        }
    }
    
    private void deleteSelected() {
        int index = historyList.getSelectedIndex();
        if (index >= 0) {
            history.remove(index);
            refreshList();
        }
    }
    
    public void showWindow() {
        refreshList();
        setVisible(true);
        toFront();
    }
}