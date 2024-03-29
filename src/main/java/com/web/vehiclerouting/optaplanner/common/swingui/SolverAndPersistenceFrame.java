/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.web.vehiclerouting.optaplanner.common.swingui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.optaplanner.core.impl.solution.Solution;
import com.web.vehiclerouting.optaplanner.common.business.SolutionBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolverAndPersistenceFrame extends JFrame {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public static final ImageIcon OPTA_PLANNER_ICON = new ImageIcon(
            SolverAndPersistenceFrame.class.getResource("optaPlannerIcon.png"));

    private final String titlePrefix;
    private final SolutionBusiness solutionBusiness;

    private SolutionPanel solutionPanel;
    private ConstraintMatchesDialog constraintMatchesDialog;

    private List<Action> quickOpenUnsolvedActionList;
    private List<Action> quickOpenSolvedActionList;
    private Action openAction;
    private Action saveAction;
    private Action importAction;
    private Action exportAction;
    private JCheckBox refreshScreenDuringSolvingCheckBox;
    private Action solveAction;
    private JButton solveButton;
    private Action terminateSolvingEarlyAction;
    private JButton terminateSolvingEarlyButton;
    private JPanel middlePanel;
    private JProgressBar progressBar;
    private JLabel resultLabel;
    private ShowConstraintMatchesDialogAction showConstraintMatchesDialogAction;

    public SolverAndPersistenceFrame(SolutionBusiness solutionBusiness, SolutionPanel solutionPanel,
            String titlePrefix) {
        super(titlePrefix);
        this.titlePrefix = titlePrefix;
        this.solutionBusiness = solutionBusiness;
        this.solutionPanel = solutionPanel;
        setIconImage(OPTA_PLANNER_ICON.getImage());
        solutionPanel.setSolutionBusiness(solutionBusiness);
        solutionPanel.setSolverAndPersistenceFrame(this);
        registerListeners();
        constraintMatchesDialog = new ConstraintMatchesDialog(this);
        constraintMatchesDialog.setSolutionBusiness(solutionBusiness);
    }

    private void registerListeners() {
        solutionBusiness.registerForBestSolutionChanges(this);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // This async, so it doesn't stop the solving immediately
                solutionBusiness.terminateSolvingEarly();
            }
        });
    }

    public void bestSolutionChanged() {
        Solution solution = solutionBusiness.getSolution();
        if (refreshScreenDuringSolvingCheckBox.isSelected()) {
            solutionPanel.updatePanel(solution);
            validate(); // TODO remove me?
        }
        resultLabel.setText("Latest best score: " + solution.getScore());
    }

    public void init(Component centerForComponent) {
        setContentPane(createContentPane());
        pack();
        setLocationRelativeTo(centerForComponent);
    }

    private JComponent createContentPane() {
        JComponent quickOpenPanel = createQuickOpenPanel();
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createToolBar(), BorderLayout.NORTH);
        mainPanel.add(createMiddlePanel(), BorderLayout.CENTER);
        mainPanel.add(createScorePanel(), BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, quickOpenPanel, mainPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.2);
        return splitPane;
    }

    private JComponent createQuickOpenPanel() {
        JSplitPane quickOpenSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                createQuickOpenUnsolvedPanel(), createQuickOpenSolvedPanel());
        quickOpenSplitPane.setResizeWeight(0.8);
        return quickOpenSplitPane;
    }

    private JComponent createQuickOpenUnsolvedPanel() {
        quickOpenUnsolvedActionList = new ArrayList<Action>();
        List<File> unsolvedFileList = solutionBusiness.getUnsolvedFileList();
        return createQuickOpenPanel(quickOpenUnsolvedActionList, unsolvedFileList, "Quick open (unsolved)");
    }

    private JComponent createQuickOpenSolvedPanel() {
        quickOpenSolvedActionList = new ArrayList<Action>();
        List<File> solvedFileList = solutionBusiness.getSolvedFileList();
        return createQuickOpenPanel(quickOpenSolvedActionList, solvedFileList, "Quick open (solved)");
    }

    private JComponent createQuickOpenPanel(List<Action> quickOpenActionList, List<File> fileList, String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        if (fileList.isEmpty()) {
            JLabel noneLabel = new JLabel("None");
            noneLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            panel.add(noneLabel);
        } else {
            for (File file : fileList) {
                Action quickOpenAction = new QuickOpenAction(file);
                quickOpenActionList.add(quickOpenAction);
                JButton quickOpenButton = new JButton(quickOpenAction);
                quickOpenButton.setHorizontalAlignment(SwingConstants.LEFT);
                quickOpenButton.setMargin(new Insets(0, 0, 0, 0));
                panel.add(quickOpenButton);
            }
        }
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(25);
        scrollPane.setMinimumSize(new Dimension(100, 80));
        // Size fits into screen resolution 1024*768
        scrollPane.setPreferredSize(new Dimension(180, 200));
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(scrollPane, BorderLayout.CENTER);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createTitledBorder(title)));
        return titlePanel;
    }

    private class QuickOpenAction extends AbstractAction {

        private File file;

        public QuickOpenAction(File file) {
            super(file.getName());
            this.file = file;
        }

        public void actionPerformed(ActionEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                solutionBusiness.openSolution(file);
                setSolutionLoaded();
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        }

    }

    private JComponent createToolBar() {
        JToolBar toolBar = new JToolBar("File operations");
        toolBar.setFloatable(false);

        importAction = new ImportAction();
        importAction.setEnabled(solutionBusiness.hasImporter());
        toolBar.add(new JButton(importAction));
        openAction = new OpenAction();
        openAction.setEnabled(true);
        toolBar.add(new JButton(openAction));
        saveAction = new SaveAction();
        saveAction.setEnabled(false);
        toolBar.add(new JButton(saveAction));
        exportAction = new ExportAction();
        exportAction.setEnabled(false);
        toolBar.add(new JButton(exportAction));
        toolBar.addSeparator();

        progressBar = new JProgressBar(0, 100);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolBar.add(progressBar);
        toolBar.addSeparator();

        solveAction = new SolveAction();
        solveAction.setEnabled(false);
        solveButton = new JButton(solveAction);
        terminateSolvingEarlyAction = new TerminateSolvingEarlyAction();
        terminateSolvingEarlyAction.setEnabled(false);
        terminateSolvingEarlyButton = new JButton(terminateSolvingEarlyAction);
        terminateSolvingEarlyButton.setVisible(false);
        toolBar.add(solveButton, "solveAction");
        toolBar.add(terminateSolvingEarlyButton, "terminateSolvingEarlyAction");
        solveButton.setMinimumSize(terminateSolvingEarlyButton.getMinimumSize());
        solveButton.setPreferredSize(terminateSolvingEarlyButton.getPreferredSize());
        return toolBar;
    }

    private class SolveAction extends AbstractAction {

        // TODO This should be replaced with a java 6 SwingWorker once drools's hudson is on JDK 1.6
        private ExecutorService solvingExecutor = Executors.newFixedThreadPool(1);

        public SolveAction() {
            super("Solve", new ImageIcon(SolverAndPersistenceFrame.class.getResource("solveAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            setSolvingState(true);
            final Solution planningProblem = solutionBusiness.getSolution(); // In event thread
            // TODO This should be replaced with a java 6 SwingWorker once drools's hudson is on JDK 1.6
            solvingExecutor.submit(new Runnable() {
                public void run() {
                    Solution bestSolution;
                    try {
                        bestSolution = solutionBusiness.solve(planningProblem); // Not in event thread
                    } catch (final Throwable e) {
                        // Otherwise the newFixedThreadPool will eat the exception...
                        logger.error("Solving failed.", e);
                        bestSolution = null;
                    }
                    final Solution newSolution = bestSolution;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (newSolution != null) {
                                solutionBusiness.setSolution(newSolution); // In event thread
                            }
                            setSolvingState(false);
                            resetScreen();
                        }
                    });
                }
            });
        }

    }

    private class TerminateSolvingEarlyAction extends AbstractAction {

        public TerminateSolvingEarlyAction() {
            super("Terminate solving early",
                    new ImageIcon(SolverAndPersistenceFrame.class.getResource("terminateSolvingEarlyAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            terminateSolvingEarlyAction.setEnabled(false);
            progressBar.setString("Terminating...");
            // This async, so it doesn't stop the solving immediately
            solutionBusiness.terminateSolvingEarly();
        }

    }

    private class OpenAction extends AbstractAction {

        private static final String NAME = "Open...";

        public OpenAction() {
            super(NAME, new ImageIcon(SolverAndPersistenceFrame.class.getResource("openAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getSolvedDataDir());
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xml");
                }

                public String getDescription() {
                    return "Solver xml files";
                }
            });
            fileChooser.setDialogTitle(NAME);
            int approved = fileChooser.showOpenDialog(SolverAndPersistenceFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    solutionBusiness.openSolution(fileChooser.getSelectedFile());
                    setSolutionLoaded();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }

    }

    private class SaveAction extends AbstractAction {

        private static final String NAME = "Save as...";

        public SaveAction() {
            super(NAME, new ImageIcon(SolverAndPersistenceFrame.class.getResource("saveAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getSolvedDataDir());
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || file.getName().endsWith(".xml");
                }

                public String getDescription() {
                    return "Solver xml files";
                }
            });
            fileChooser.setDialogTitle(NAME);
            fileChooser.setSelectedFile(new File(solutionBusiness.getSolvedDataDir(),
                    FilenameUtils.getBaseName(solutionBusiness.getSolutionFileName()) + ".xml"));
            int approved = fileChooser.showSaveDialog(SolverAndPersistenceFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    solutionBusiness.saveSolution(fileChooser.getSelectedFile());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }

    }

    private class ImportAction extends AbstractAction {

        private static final String NAME = "Import...";

        public ImportAction() {
            super(NAME, new ImageIcon(SolverAndPersistenceFrame.class.getResource("importAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getImportDataDir());
            fileChooser.setFileFilter(new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory() || solutionBusiness.acceptImportFile(file);
                }
                public String getDescription() {
                    return "Import files (*" + solutionBusiness.getImportFileSuffix() + ")";
                }
            });
            fileChooser.setDialogTitle(NAME);
            int approved = fileChooser.showOpenDialog(SolverAndPersistenceFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    solutionBusiness.importSolution(fileChooser.getSelectedFile());
                    setSolutionLoaded();
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }

    }

    private class ExportAction extends AbstractAction {

        private static final String NAME = "Export as...";

        public ExportAction() {
            super(NAME, new ImageIcon(SolverAndPersistenceFrame.class.getResource("exportAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(solutionBusiness.getExportDataDir());
//            fileChooser.setFileFilter(new FileFilter() {
//                public boolean accept(File file) {
//                    return file.isDirectory() || file.getName().endsWith(".xml"); // TODO Not all export files are xml
//                }
//                public String getDescription() {
//                    return "Export files";
//                }
//            });
            fileChooser.setDialogTitle(NAME);
            int approved = fileChooser.showSaveDialog(SolverAndPersistenceFrame.this);
            if (approved == JFileChooser.APPROVE_OPTION) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    solutionBusiness.exportSolution(fileChooser.getSelectedFile());
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }

    }

    private JPanel createMiddlePanel() {
        middlePanel = new JPanel(new CardLayout());
        ImageIcon usageExplanationIcon = new ImageIcon(getClass().getResource(solutionPanel.getUsageExplanationPath()));
        JLabel usageExplanationLabel = new JLabel(usageExplanationIcon);
        // Allow splitPane divider to be moved to the right
        usageExplanationLabel.setMinimumSize(new Dimension(100, 100));
        middlePanel.add(usageExplanationLabel, "usageExplanationPanel");
        JComponent wrappedSolutionPanel;
        if (solutionPanel.isWrapInScrollPane()) {
            wrappedSolutionPanel = new JScrollPane(solutionPanel);
        } else {
            wrappedSolutionPanel = solutionPanel;
        }
        middlePanel.add(wrappedSolutionPanel, "solutionPanel");
        return middlePanel;
    }

    private JPanel createScorePanel() {
        JPanel scorePanel = new JPanel(new BorderLayout());
        scorePanel.setBorder(BorderFactory.createEtchedBorder());
        showConstraintMatchesDialogAction = new ShowConstraintMatchesDialogAction();
        showConstraintMatchesDialogAction.setEnabled(false);
        scorePanel.add(new JButton(showConstraintMatchesDialogAction), BorderLayout.WEST);
        resultLabel = new JLabel("Score:");
        resultLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        scorePanel.add(resultLabel, BorderLayout.CENTER);
        refreshScreenDuringSolvingCheckBox = new JCheckBox("Refresh screen during solving",
                solutionPanel.isRefreshScreenDuringSolving());
        scorePanel.add(refreshScreenDuringSolvingCheckBox, BorderLayout.EAST);
        return scorePanel;
    }

    private class ShowConstraintMatchesDialogAction extends AbstractAction {

        public ShowConstraintMatchesDialogAction() {
            super("Constraint matches", new ImageIcon(SolverAndPersistenceFrame.class.getResource("showConstraintMatchesDialogAction.png")));
        }

        public void actionPerformed(ActionEvent e) {
            constraintMatchesDialog.resetContentPanel();
            constraintMatchesDialog.setVisible(true);
        }

    }

    private void setSolutionLoaded() {
        setTitle(titlePrefix + " - " + solutionBusiness.getSolutionFileName());
        ((CardLayout) middlePanel.getLayout()).show(middlePanel, "solutionPanel");
        solveAction.setEnabled(true);
        saveAction.setEnabled(true);
        exportAction.setEnabled(solutionBusiness.hasExporter());
        showConstraintMatchesDialogAction.setEnabled(true);
        resetScreen();
    }

    private void setSolvingState(boolean solving) {
        for (Action action : quickOpenUnsolvedActionList) {
            action.setEnabled(!solving);
        }
        for (Action action : quickOpenSolvedActionList) {
            action.setEnabled(!solving);
        }
        importAction.setEnabled(!solving && solutionBusiness.hasImporter());
        openAction.setEnabled(!solving);
        saveAction.setEnabled(!solving);
        exportAction.setEnabled(!solving && solutionBusiness.hasExporter());
        solveAction.setEnabled(!solving);
        solveButton.setVisible(!solving);
        terminateSolvingEarlyAction.setEnabled(solving);
        terminateSolvingEarlyButton.setVisible(solving);
        solutionPanel.setEnabled(!solving);
        progressBar.setIndeterminate(solving);
        progressBar.setStringPainted(solving);
        progressBar.setString(solving ? "Solving..." : null);
        showConstraintMatchesDialogAction.setEnabled(!solving);
        solutionPanel.setSolvingState(solving);
    }

    public void resetScreen() {
        solutionPanel.resetPanel(solutionBusiness.getSolution());
        validate();
        resultLabel.setText("Score: " + solutionBusiness.getScore());
    }

}
