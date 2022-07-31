package com.chriscarini.jetbrains.locchangecountdetector.settings;

import com.chriscarini.jetbrains.locchangecountdetector.ChangeThresholdIconInfo;
import com.chriscarini.jetbrains.locchangecountdetector.ChangeThresholdService;
import com.chriscarini.jetbrains.locchangecountdetector.ChangeThresholdTimeInfo;
import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.CollapsiblePanel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


/**
 * A {@link Configurable} that provides the user the ability to configure the LoC plugin.
 */
public class SettingsConfigurable implements Configurable {
    private final Project myProject;
    private final JPanel mainPanel = new JBPanel<>();
    private final JTextPane timeInfoTable = new JTextPane();
    private final JTextPane iconInfoTable = new JTextPane();

    public SettingsConfigurable(@NotNull final Project myProject) {
        this.myProject = myProject;
        buildMainPanel();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return Messages.message("loc.settings.configurable.display.name");
    }

    private void buildMainPanel() {
        // Create a simple form to display the user-configurable options
        mainPanel.setLayout(new VerticalFlowLayout(true, false));

        buildChangeThresholdTimeInfoTable();
        buildChangeThresholdIconInfoTable();

        final JPanel infoTables = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel(Messages.message("loc.settings.configurable.label.loc.sizes.thresholds")), timeInfoTable, true)
                .addSeparator()
                .addLabeledComponent(new JBLabel(Messages.message("loc.settings.configurable.label.icon.thresholds")), iconInfoTable, true)
                .getPanel();

        final JPanel panel = FormBuilder.createFormBuilder()
                .addComponent(new CollapsiblePanel(infoTables, true, true, AllIcons.General.ArrowDown,
                        AllIcons.General.ArrowRight, Messages.message("loc.settings.configurable.title.threshold.tables")))
                .getPanel();

        mainPanel.add(panel);
    }

    private void buildChangeThresholdTimeInfoTable() {
        final StringBuilder rows = new StringBuilder();

        int prevThreshold = 0;
        for (final ChangeThresholdTimeInfo changeThresholdTimeInfo : ChangeThresholdService.getInstance(myProject).getChangeThresholdTimeInfos()) {
            final int curThreshold = changeThresholdTimeInfo.getThreshold();
            //noinspection HardCodedStringLiteral
            rows.append(
                    String.format("" +
                                    "<tr>" +
                                    "    <th style=\"text-align:center\">%s</th>" +
                                    "    <td style=\"text-align:center\">%s</td>" +
                                    "    <td style=\"text-align:right\">%s</td>" +
                                    "    <td style=\"text-align:right\">%s</td>" +
                                    "</tr>",
                            changeThresholdTimeInfo.getName(),
                            curThreshold == Integer.MAX_VALUE ?
                                    String.format("%s &lt; LoC", prevThreshold)
                                    : String.format("%s &lt; LoC &lt;= %s", prevThreshold, curThreshold),
                            changeThresholdTimeInfo.getReviewTimeBizHrs(),
                            changeThresholdTimeInfo.getApprovalTimeBizHrs()
                    )
            );
            prevThreshold = curThreshold;
        }

        timeInfoTable.setEnabled(false);
        timeInfoTable.setVisible(true);
        timeInfoTable.setContentType(ContentType.TEXT_HTML.getMimeType());
        //noinspection HardCodedStringLiteral
        timeInfoTable.setText(
                String.format("" +
                                "<html lang=\"en\">\n<body>\n<table>\n" +
                                "    <tr>\n" +
                                "        <th style=\"text-align:center\">Name</th>\n" +
                                "        <th style=\"text-align:center\">Threshold</th>\n" +
                                "        <th style=\"text-align:center\">Review Time</th>\n" +
                                "        <th style=\"text-align:center\">Approval Time</th>\n" +
                                "    </tr>\n" +
                                "    %s\n" +
                                "</table>\n</body>\n</html>",
                        rows
                )
        );
    }

    private void buildChangeThresholdIconInfoTable() {
        final ChangeThresholdIconInfo i = ChangeThresholdService.getInstance(myProject).getChangeThresholdIconInfo();

        iconInfoTable.setEnabled(false);
        iconInfoTable.setVisible(true);
        iconInfoTable.setContentType(ContentType.TEXT_HTML.getMimeType());
        //noinspection HardCodedStringLiteral
        iconInfoTable.setText(
                String.format("" +
                                "<html lang=\"en\">\n<body>\n<table>\n" +
                                "    <tr>\n" +
                                "        <th style=\"text-align:center\">Level</th>\n" +
                                "        <th style=\"text-align:center\">Threshold</th>\n" +
                                "    </tr>\n" +
                                "    <tr>\n" +
                                "        <th style=\"text-align:center\">Info</th>\n" +
                                "        <td style=\"text-align:right\">%s</td>\n" +
                                "    </tr>\n" +
                                "    <tr>\n" +
                                "        <th style=\"text-align:center\">Warn</th>\n" +
                                "        <td style=\"text-align:right\">%s</td>\n" +
                                "    </tr>\n" +
                                "    <tr>\n" +
                                "        <th style=\"text-align:center\">Error</th>\n" +
                                "        <td style=\"text-align:right\">%s</td>\n" +
                                "    </tr>\n" +
                                "</table>\n</body>\n</html>",
                        i.getInfoAbove(),
                        i.getWarnAbove(),
                        i.getErrorAbove()
                )
        );
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {
    }
}
