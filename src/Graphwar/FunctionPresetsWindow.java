package Graphwar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class FunctionPresetsWindow extends JFrame {
    private JTable presetTable;
    private DefaultTableModel tableModel;
    private JLabel unexpandedLabel;
    private JButton cancelButton;
    private JButton copyButton;
    private JButton okButton;
    private JPanel slidersPanel;
    private JScrollPane slidersScrollPane;
    private FunctionPresets functionPresets;
    private GameScreen gameScreen;
    private List<FunctionPresets.PresetFunction> presets;
    private int selectedRow = -1;
    private Map<String, JSlider> sliderMap;
    private Map<String, JTextField> textFieldMap;
    private Map<String, Double> paramValues;
    private Map<String, Double> minValues;
    private Map<String, Double> maxValues;
    private Font chineseFont;

    private String baseTemplate;
    private boolean isReady = false;
    private boolean suppressUpdates = false;

    private static final double SLIDER_MIN = -50.0;
    private static final double SLIDER_MAX = 50.0;

    public FunctionPresetsWindow(GameScreen gameScreen, FunctionPresets functionPresets) {
        super("函数预设");
        this.gameScreen = gameScreen;
        this.functionPresets = functionPresets;
        this.presets = functionPresets.getAllPresets();
        this.sliderMap = new HashMap<>();
        this.textFieldMap = new HashMap<>();
        this.paramValues = new HashMap<>();
        this.minValues = new HashMap<>();
        this.maxValues = new HashMap<>();
        this.baseTemplate = "\\f";
        this.chineseFont = new Font("Dialog", Font.PLAIN, 12);

        setSize(780, 600);
        setLocationRelativeTo(gameScreen);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 顶部显示区：显示未展开式
        JPanel topDisplayPanel = new JPanel(new BorderLayout());
        javax.swing.border.TitledBorder topBorder = BorderFactory.createTitledBorder("未展开式");
        topBorder.setTitleFont(chineseFont);
        topDisplayPanel.setBorder(topBorder);
        unexpandedLabel = new JLabel(" ");
        unexpandedLabel.setFont(chineseFont);
        topDisplayPanel.add(unexpandedLabel, BorderLayout.CENTER);
        add(topDisplayPanel, BorderLayout.NORTH);

        // 滑块面板
        slidersPanel = new JPanel();
        slidersPanel.setLayout(new BoxLayout(slidersPanel, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder sliderBorder = BorderFactory.createTitledBorder("参数调节");
        sliderBorder.setTitleFont(chineseFont);
        slidersPanel.setBorder(sliderBorder);
        slidersScrollPane = new JScrollPane(slidersPanel);
        slidersScrollPane.setPreferredSize(new Dimension(760, 160));

        // 函数列表
        String[] columnNames = {"名称", "参数", "展开式"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (FunctionPresets.PresetFunction preset : presets) {
            Object[] row = {
                preset.getName(),
                preset.getParametersString(),
                preset.getExpression()
            };
            tableModel.addRow(row);
        }

        presetTable = new JTable(tableModel);
        presetTable.setFont(chineseFont);
        presetTable.getTableHeader().setFont(chineseFont);
        presetTable.setRowHeight(20);
        presetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        presetTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        presetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        presetTable.getColumnModel().getColumn(2).setPreferredWidth(500);
        presetTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateSelection();
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(presetTable);

        // 中心面板
        JPanel centerContentPanel = new JPanel(new BorderLayout(5, 5));
        centerContentPanel.add(slidersScrollPane, BorderLayout.NORTH);
        centerContentPanel.add(tableScrollPane, BorderLayout.CENTER);
        add(centerContentPanel, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cancelButton = new JButton("取消");
        cancelButton.setFont(chineseFont);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        leftPanel.add(cancelButton);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        copyButton = new JButton("复制");
        copyButton.setFont(chineseFont);
        copyButton.setEnabled(false);
        copyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commitSelection();
            }
        });
        centerPanel.add(copyButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        okButton = new JButton("确定");
        okButton.setFont(chineseFont);
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commitSelection();
                setVisible(false);
            }
        });
        rightPanel.add(okButton);

        buttonPanel.add(leftPanel, BorderLayout.WEST);
        buttonPanel.add(centerPanel, BorderLayout.CENTER);
        buttonPanel.add(rightPanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        updateButtons();
    }

    private void updateSelection() {
        isReady = false;
        selectedRow = presetTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < presets.size()) {
            rebuildSliders();
            isReady = true;
            refreshAll();
        } else {
            slidersPanel.removeAll();
            sliderMap.clear();
            textFieldMap.clear();
            paramValues.clear();
            minValues.clear();
            maxValues.clear();
            unexpandedLabel.setText(" ");
            slidersPanel.revalidate();
            slidersPanel.repaint();
        }
        updateButtons();
    }

    private void rebuildSliders() {
        slidersPanel.removeAll();
        sliderMap.clear();
        textFieldMap.clear();
        paramValues.clear();
        minValues.clear();
        maxValues.clear();

        FunctionPresets.PresetFunction preset = presets.get(selectedRow);
        String[] params = preset.getParameters();
        Map<String, String> defaults = preset.getDefaultValues();

        for (String param : params) {
            double initVal = 0.0;
            if (defaults != null && defaults.containsKey(param)) {
                try {
                    initVal = Double.parseDouble(defaults.get(param));
                } catch (NumberFormatException e) {
                    initVal = 0.0;
                }
            }
            paramValues.put(param, initVal);
            minValues.put(param, SLIDER_MIN);
            maxValues.put(param, SLIDER_MAX);
        }

        for (String param : params) {
            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setMaximumSize(new Dimension(760, 32));

            // 参数名
            JLabel paramLabel = new JLabel(param);
            paramLabel.setFont(chineseFont);
            paramLabel.setPreferredSize(new Dimension(25, 25));
            paramLabel.setMinimumSize(new Dimension(25, 25));
            row.add(paramLabel);
            row.add(Box.createRigidArea(new Dimension(5, 0)));

            // -50 标签
            JLabel leftLabel = new JLabel("-50");
            leftLabel.setFont(chineseFont);
            leftLabel.setPreferredSize(new Dimension(30, 25));
            leftLabel.setMinimumSize(new Dimension(30, 25));
            row.add(leftLabel);

            // 滑块
            double initVal = paramValues.get(param);
            int sliderInit = sliderValue(initVal);
            final JSlider slider = new JSlider(-50, 50, sliderInit);
            slider.setFont(chineseFont);
            slider.setPreferredSize(new Dimension(260, 25));
            slider.setMinimumSize(new Dimension(200, 25));
            slider.setMajorTickSpacing(25);
            slider.setPaintTicks(true);
            slider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (suppressUpdates) return;
                    int sVal = slider.getValue();
                    paramValues.put(param, (double) sVal);
                    JTextField tf = textFieldMap.get(param);
                    if (tf != null && !tf.hasFocus()) {
                        suppressUpdates = true;
                        tf.setText(formatNumber(sVal));
                        suppressUpdates = false;
                    }
                    refreshAll();
                }
            });
            sliderMap.put(param, slider);
            row.add(slider);

            // 50 标签
            JLabel rightLabel = new JLabel("50");
            rightLabel.setFont(chineseFont);
            rightLabel.setPreferredSize(new Dimension(25, 25));
            rightLabel.setMinimumSize(new Dimension(25, 25));
            row.add(rightLabel);
            row.add(Box.createRigidArea(new Dimension(5, 0)));

            // 文本框
            final JTextField textField = new JTextField(formatNumber(initVal));
            textField.setFont(chineseFont);
            textField.setPreferredSize(new Dimension(60, 25));
            textField.setMaximumSize(new Dimension(60, 25));
            textField.getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) { syncFromTextField(param); }
                public void insertUpdate(DocumentEvent e) { syncFromTextField(param); }
                public void removeUpdate(DocumentEvent e) { syncFromTextField(param); }
            });
            textFieldMap.put(param, textField);
            row.add(textField);
            row.add(Box.createRigidArea(new Dimension(5, 0)));

            // 上调 +1
            JButton upBtn = new JButton("+");
            upBtn.setFont(chineseFont);
            upBtn.setPreferredSize(new Dimension(40, 25));
            upBtn.setMaximumSize(new Dimension(40, 25));
            upBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double cur = paramValues.get(param);
                    double newVal = Math.ceil(cur) + 1.0;
                    if (newVal > SLIDER_MAX) newVal = SLIDER_MAX;
                    paramValues.put(param, newVal);
                    suppressUpdates = true;
                    slider.setValue(sliderValue(newVal));
                    textField.setText(formatNumber(newVal));
                    suppressUpdates = false;
                    refreshAll();
                }
            });
            row.add(upBtn);

            // 下调 -1
            JButton downBtn = new JButton("-");
            downBtn.setFont(chineseFont);
            downBtn.setPreferredSize(new Dimension(40, 25));
            downBtn.setMaximumSize(new Dimension(40, 25));
            downBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double cur = paramValues.get(param);
                    double newVal = Math.floor(cur) - 1.0;
                    if (newVal < SLIDER_MIN) newVal = SLIDER_MIN;
                    paramValues.put(param, newVal);
                    suppressUpdates = true;
                    slider.setValue(sliderValue(newVal));
                    textField.setText(formatNumber(newVal));
                    suppressUpdates = false;
                    refreshAll();
                }
            });
            row.add(downBtn);

            row.add(Box.createHorizontalGlue());
            slidersPanel.add(row);
        }

        slidersPanel.revalidate();
        slidersPanel.repaint();
    }

    private int sliderValue(double val) {
        if (val < SLIDER_MIN) val = SLIDER_MIN;
        if (val > SLIDER_MAX) val = SLIDER_MAX;
        return (int) Math.round(val);
    }

    private void syncFromTextField(String param) {
        if (suppressUpdates) return;
        JTextField tf = textFieldMap.get(param);
        if (tf == null) return;
        String text = tf.getText().trim();
        if (text.isEmpty()) return;
        try {
            double val = Double.parseDouble(text);
            paramValues.put(param, val);
            int clamped = sliderValue(val);
            JSlider sl = sliderMap.get(param);
            if (sl != null) {
                int currentSliderVal = sl.getValue();
                if (currentSliderVal != clamped) {
                    suppressUpdates = true;
                    sl.setValue(clamped);
                    suppressUpdates = false;
                }
            }
            refreshAll();
        } catch (NumberFormatException ex) {
        }
    }

    private String formatNumber(double val) {
        if (val == Math.floor(val) && !Double.isInfinite(val)) {
            return String.valueOf((int) val);
        }
        return String.valueOf(val);
    }

    private String buildFunctionCall() {
        if (selectedRow < 0 || selectedRow >= presets.size()) return "";
        FunctionPresets.PresetFunction preset = presets.get(selectedRow);
        String alias = preset.getSecondAlias();
        String[] params = preset.getParameters();
        StringBuilder sb = new StringBuilder();
        sb.append(alias).append("(");
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(",");
            Double v = paramValues.get(params[i]);
            if (v != null) {
                sb.append(formatNumber(v));
            } else {
                sb.append(params[i]);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private String buildFullExpression() {
        String funcCall = buildFunctionCall();
        if (funcCall.isEmpty()) return "";
        if (baseTemplate == null) return funcCall;
        if (!baseTemplate.contains("\\f")) return funcCall;
        return baseTemplate.replace("\\f", funcCall);
    }

    private void refreshAll() {
        if (!isReady || selectedRow < 0) return;

        // 更新顶部显示的未展开式
        unexpandedLabel.setText("  " + buildFunctionCall());

        // 更新游戏输入框 + 刷新预览
        try {
            String fullExpr = buildFullExpression();
            if (fullExpr != null && !fullExpr.isEmpty() && gameScreen != null) {
                gameScreen.setFunctionTextAndPreview(fullExpr);
            }
        } catch (Exception e) {
            // 预览出错不影响继续调节
        }
    }

    private void updateButtons() {
        boolean hasSelection = selectedRow >= 0;
        copyButton.setEnabled(hasSelection);
        okButton.setEnabled(hasSelection);
    }

    private void commitSelection() {
        if (selectedRow >= 0 && selectedRow < presets.size()) {
            String fullExpr = buildFullExpression();
            if (fullExpr != null && !fullExpr.isEmpty() && gameScreen != null) {
                gameScreen.setFunctionTextAndPreview(fullExpr);
            }
        }
    }

    public void showWindow() {
        // 打开时把输入框内容保存为 base template
        String current = "";
        try {
            current = gameScreen.getFunctionText();
            if (current == null) current = "";
        } catch (Exception e) {
            current = "";
        }
        baseTemplate = current + "\\f";

        isReady = false;
        presetTable.clearSelection();
        selectedRow = -1;
        slidersPanel.removeAll();
        sliderMap.clear();
        textFieldMap.clear();
        paramValues.clear();
        minValues.clear();
        maxValues.clear();
        unexpandedLabel.setText(" ");
        slidersPanel.revalidate();
        slidersPanel.repaint();
        updateButtons();
        setVisible(true);
        toFront();
    }
}