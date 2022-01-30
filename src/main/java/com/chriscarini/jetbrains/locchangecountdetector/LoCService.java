package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.SystemIndependent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoCService {

    private Integer loc = 0;
    private String files = "0";
    private Integer locInCommit = 0;
    private String filesInCommit = "0";

    private final Project project;

    public static LoCService getInstance(@NotNull final Project project) {
        return project.getService(LoCService.class);
    }

    public LoCService(@NotNull final Project project) {
        this.project = project;
    }

    private Pair<Integer, String> getGitShowStat(@NotNull final Path path) {
        String filesChanged = "";
        int loc = 0;
        List<VirtualFile> virtualFiles = new ArrayList<>();

        try {
            String lines = getDiffShowStat(this.project, this.project.getBasePath(), virtualFiles);
            String[] linesArray = lines.split("\n");
            String lastLine = linesArray[linesArray.length - 1];

            String[] lastArray = lastLine.split(",");
            if (lastArray.length == 0 || Objects.equals(lastArray[0], "")) {
                return new Pair<>(0, "0");
            }

            String[] filesAddDel = lastArray[0].split(" ");
            if (filesAddDel.length == 0) {
                return new Pair<>(0, "0");
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

        return new Pair<>(loc, filesChanged);
    }

    private Pair<Integer, Integer> getGitDiffNumstat(@NotNull final Path path) {

        int filesChanged = 0;
        int additions = 0;
        int deletions = 0;

        try {
            List<VirtualFile> virtualFiles = new ArrayList<>();
            String line = getDiffNumStat(this.project, this.project.getBasePath(), virtualFiles);

            final String[] lineSplit = line.split("\t");
            if (lineSplit.length == 0 || Objects.equals(lineSplit[0], "")) {
                return new Pair<>(additions + deletions, filesChanged);
            }
            filesChanged += 1;
            additions += Integer.parseInt(lineSplit[0]);
            deletions += Integer.parseInt(lineSplit[1]);
        } catch (VcsException e) {
            e.printStackTrace();
        }

        return new Pair<>(additions + deletions, filesChanged);
    }

    private static String getDiffNumStat(Project project, @Nullable @SystemIndependent @NonNls String root, List<VirtualFile> virtualFiles) throws VcsException {
        GitLineHandler handler = new GitLineHandler(project, new File(root), GitCommand.DIFF);
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("HEAD");
        handler.addParameters("--numstat");
        String output = Git.getInstance().runCommand(handler).getOutputOrThrow();

        return output;
    }

    private static String getDiffShowStat(Project project, @Nullable @SystemIndependent @NonNls String root, List<VirtualFile> virtualFiles) throws VcsException {
        GitLineHandler handler = new GitLineHandler(project, new File(root), GitCommand.SHOW);
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("--stat");
        String output = Git.getInstance().runCommand(handler).getOutputOrThrow();
        return output;
    }

    public void computeLoCInfo() {
        ProgressManager.getInstance()
                .run(new Task.Backgroundable(this.project, "My background thing") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        final String projectPath = myProject.getBasePath();
                        if (projectPath == null) {
                            return;
                        }
                        final Path projectDir = Paths.get(projectPath);

                        final Pair<Integer, Integer> info = getGitDiffNumstat(projectDir);
                        final Pair<Integer, String> infoInHeadCommit = getGitShowStat(projectDir);

                        LoCService.getInstance(myProject).setFiles(info.second.toString());
                        LoCService.getInstance(myProject).setLoc(info.first);

                        LoCService.getInstance(myProject).setFilesInCommit(infoInHeadCommit.second.toString());
                        LoCService.getInstance(myProject).setLocInCommit(infoInHeadCommit.first);
                    }
                });

    }

    @NotNull
    public Integer getChangeCount() {
        return Objects.requireNonNullElse(loc, 0);
    }

    @NotNull
    public String getFileCount() {
        return Objects.requireNonNullElse(files, "0");
    }

    @NotNull
    public Integer getChangeCountInCommit() {
        return Objects.requireNonNullElse(locInCommit, 0);
    }

    @NotNull
    public String getFileCountInCommit() {
        return Objects.requireNonNullElse(filesInCommit, "0");
    }


    public void setLoc(@NotNull final Integer loc) {
        this.loc = loc;
    }

    public void setFiles(@NotNull final String files) {
        this.files = files;
    }

    public void setLocInCommit(@NotNull final Integer loc) {
        this.locInCommit = loc;
    }

    public void setFilesInCommit(@NotNull final String files) {
        this.filesInCommit = files;
    }

    @NotNull
    public double getReviewTime(int loc) {
        // Hard code these value by getting them from our GitHub dashboards
        double reviewHoursXS = 25900 / 3600;
        double reviewHoursS = 24101 / 3600;
        double reviewHoursM = 25974 / 3600;
        double reviewHoursL = 40026 / 3600;
        double reviewHoursXL = 60097 / 3600;
        double reviewHoursXXL = 51675 / 3600;
        double reviewTime = 0;

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
    public double getApprovalTime(int loc) {
        // Hard code these value by getting them from our GitHub dashboards
        double approvalHoursXS = 31210 / 3600;
        double approvalHoursS = 35254 / 3600;
        double approvalHoursM = 50548 / 3600;
        double approvalHoursL = 88599 / 3600;
        double approvalHoursXL = 131256 / 3600;
        double approvalHoursXXL = 123000 / 3600;
        double approvalTime = 0;

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


