package com.chriscarini.jetbrains.locchangecountdetector;

import com.chriscarini.jetbrains.locchangecountdetector.messages.Messages;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class LoCService {

    private Integer loc = 0;
    private Integer files = 0;
    private Integer locInCommit = 0;
    private Integer filesInCommit = 0;

    private final Project project;

    public static LoCService getInstance(@NotNull final Project project) {
        return project.getService(LoCService.class);
    }

    public LoCService(@NotNull final Project project) {
        this.project = project;
    }

    private Pair<Integer, Integer> getGitShowStat() {
        String filesChanged = "";
        int loc = 0;

        try {
            final String lines = getDiffShowStat();
            final String[] linesArray = lines.split("\n");
            final String lastLine = linesArray[linesArray.length - 1];

            final String[] lastArray = lastLine.split(",");
            if (lastArray.length == 0 || Objects.equals(lastArray[0], "")) {
                return new Pair<>(0, 0);
            }

            final String[] filesAddDel = lastArray[0].split(" ");
            if (filesAddDel.length == 0) {
                return new Pair<>(0, 0);
            }
            filesChanged = filesAddDel[1];
            int additions = 0;
            int deletions = 0;
            String[] secondLine = lastArray[1].split(" ");
            if (secondLine[2].equals("insertions(+)")) {
                additions = Integer.parseInt(secondLine[1]);
                if (lastArray.length == 3) {
                    String[] thirdLine = lastArray[2].split(" ");
                    deletions = Integer.parseInt(thirdLine[1]);
                }
            } else {
                deletions = Integer.parseInt(secondLine[1]);
            }

            loc = additions + deletions;
        } catch (VcsException e) {
            e.printStackTrace();
        }

        return new Pair<>(loc, Integer.valueOf(filesChanged));
    }

    private Pair<Integer, Integer> getGitDiffNumStat() {

        int filesChanged = 0;
        int additions = 0;
        int deletions = 0;

        try {
            final String line = getDiffNumStat();

            final String[] lineSplit = line.split("\t");
            if (lineSplit.length == 0 || Objects.equals(lineSplit[0], "")) {
                return new Pair<>(0, 0);
            }
            filesChanged += 1;
            additions += Integer.parseInt(lineSplit[0]);
            deletions += Integer.parseInt(lineSplit[1]);
        } catch (VcsException e) {
            e.printStackTrace();
        }

        return new Pair<>(additions + deletions, filesChanged);
    }

    private String getDiffNumStat() throws VcsException {
        final String basePath = project.getBasePath();
        if (basePath == null) {
            return "0";
        }
        GitLineHandler handler = new GitLineHandler(project, new File(basePath), GitCommand.DIFF);
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("HEAD");
        //noinspection SpellCheckingInspection
        handler.addParameters("--numstat");

        return Git.getInstance().runCommand(handler).getOutputOrThrow();
    }

    private String getDiffShowStat() throws VcsException {
        final String basePath = project.getBasePath();
        if (basePath == null) {
            return "0";
        }
        GitLineHandler handler = new GitLineHandler(project, new File(basePath), GitCommand.SHOW);
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("--stat");
        return Git.getInstance().runCommand(handler).getOutputOrThrow();
    }

    public void computeLoCInfo() {
        ProgressManager.getInstance()
                .run(new Task.Backgroundable(this.project, Messages.message("loc.service.compute.progress.backgroundable.title")) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        if (myProject == null) {
                            return;
                        }
                        final String projectPath = myProject.getBasePath();
                        if (projectPath == null) {
                            return;
                        }

                        final Pair<Integer, Integer> info = getGitDiffNumStat();
                        final Pair<Integer, Integer> infoInHeadCommit = getGitShowStat();

                        LoCService.getInstance(myProject).setFiles(info.second);
                        LoCService.getInstance(myProject).setLoc(info.first);

                        LoCService.getInstance(myProject).setFilesInCommit(infoInHeadCommit.second);
                        LoCService.getInstance(myProject).setLocInCommit(infoInHeadCommit.first);
                    }
                });

    }

    @NotNull
    public Integer getChangeCount() {
        return Objects.requireNonNullElse(loc, 0);
    }

    @NotNull
    public Integer getFileCount() {
        return Objects.requireNonNullElse(files, 0);
    }

    @NotNull
    public Integer getChangeCountInCommit() {
        return Objects.requireNonNullElse(locInCommit, 0);
    }

    @NotNull
    public Integer getFileCountInCommit() {
        return Objects.requireNonNullElse(filesInCommit, 0);
    }


    public void setLoc(@NotNull final Integer loc) {
        this.loc = loc;
    }

    public void setFiles(@NotNull final Integer files) {
        this.files = files;
    }

    public void setLocInCommit(@NotNull final Integer loc) {
        this.locInCommit = loc;
    }

    public void setFilesInCommit(@NotNull final Integer files) {
        this.filesInCommit = files;
    }

    @NotNull
    public Double getReviewTime(int loc) {
        // Hard code these value by getting them from our GitHub dashboards
        double reviewHoursXS = 21600 / 3600;
        double reviewHoursS = 23400 / 3600;
        double reviewHoursM = 27000 / 3600;
        double reviewHoursL = 36000 / 3600;
        double reviewHoursXL = 45000 / 3600;
        double reviewHoursXXL = 54000 / 3600;
        double reviewTime;

        if (loc >= 0 && loc <= 9) {
            reviewTime = reviewHoursXS;
        } else if (loc >= 10 && loc <= 29) {
            reviewTime = reviewHoursS;
        } else if (loc >= 30 && loc <= 99) {
            reviewTime = reviewHoursM;
        } else if (loc >= 100 && loc <= 499) {
            reviewTime = reviewHoursL;
        } else if (loc >= 500 && loc <= 999) {
            reviewTime = reviewHoursXL;
        } else {
            reviewTime = reviewHoursXXL;
        }
        return reviewTime;
    }

    @NotNull
    public Double getApprovalTime(int loc) {
        // Hard code these value by getting them from our GitHub dashboards
        double approvalHoursXS = 28800 / 3600;
        double approvalHoursS = 36000 / 3600;
        double approvalHoursM = 54000 / 3600;
        double approvalHoursL = 90000 / 3600;
        double approvalHoursXL = 108000 / 3600;
        double approvalHoursXXL = 126000 / 3600;
        double approvalTime;

        if (loc >= 0 && loc <= 9) {
            approvalTime = approvalHoursXS;
        } else if (loc >= 10 && loc <= 29) {
            approvalTime = approvalHoursS;
        } else if (loc >= 30 && loc <= 99) {
            approvalTime = approvalHoursM;
        } else if (loc >= 100 && loc <= 499) {
            approvalTime = approvalHoursL;
        } else if (loc >= 500 && loc <= 999) {
            approvalTime = approvalHoursXL;
        } else {
            approvalTime = approvalHoursXXL;
        }
        return approvalTime;
    }
}