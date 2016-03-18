import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.text.JTextComponent;

/**
 * @author James Moore &lt;james.moore@maluuba.com&gt;
 * @version 3/18/2016.
 */
public class Main implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Main());
    }

    @Override
    public void run() {
        JFrame frame = new JFrame();

        JPanel contentPane = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea();

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        Dimension textSize = new Dimension(800, 600);
        scrollPane.setMinimumSize(textSize);
        scrollPane.setPreferredSize(textSize);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        JPanel selectionPane = new JPanel(new FlowLayout());
        selectionPane.add(new JLabel("Select file:"));

        JButton selectFileButton = new JButton(new LoadFileAction(frame, textArea));
        selectionPane.add(selectFileButton);

        contentPane.add(selectionPane, BorderLayout.PAGE_START);

        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(contentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static class LoadFileAction extends AbstractAction {
        private static final String BUTTON_LABEL = "Load File"; //i18n reserved for later

        private final JFrame frame;
        private final JTextComponent textComponent;

        public LoadFileAction(JFrame frame, JTextComponent textComponent) {
            super(BUTTON_LABEL);
            this.frame = frame;
            this.textComponent = textComponent;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            FileDialog fd = new FileDialog(frame, "Select File to Load", FileDialog.LOAD);
            fd.setMultipleMode(false);
            fd.setVisible(true);
            final File[] toLoad = fd.getFiles();
            if(toLoad == null || toLoad.length == 0) { //user cancelled
                return;
            }
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = Files.newBufferedReader(toLoad[0].toPath(), StandardCharsets.UTF_8)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                    }
                    return sb.toString();
                }

                @Override
                protected void done() {
                    try {
                        textComponent.setText(get());
                    } catch(ExecutionException | InterruptedException ex) {
                        JOptionPane.showMessageDialog(frame, "Could not load file: " + stackTraceToString(ex),
                                "Error loading file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        }
    }

    private static String stackTraceToString(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getClass().getName()).append(": ").append(t.getLocalizedMessage()).append("\nStack Trace:\n");
        for(StackTraceElement element : t.getStackTrace()) {
            sb.append("\t").append(element.getClassName()).append(".").append(element.getMethodName()).append(" (")
                    .append(element.getFileName()).append(":").append(element.getLineNumber())
                    .append(")\n");
        }
        return sb.toString();
    }
}
