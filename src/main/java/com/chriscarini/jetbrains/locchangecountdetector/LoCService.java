package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.concurrency.JobScheduler;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
        ScheduledFuture<?> checkChangesStatusJob = JobScheduler.getScheduler()
                .scheduleWithFixedDelay(() -> this.computeLoCInfo(), 0, 10, TimeUnit.SECONDS);

        // Subscribe to appWillBeClosed event to emit shutdown metric
        // we cannot do this disposeComponent as it seems services get killed too fast (before dispose but after appClosing)
        final Application app = ApplicationManager.getApplication();
        final MessageBusConnection connection = app.getMessageBus().connect(app);
        connection.subscribe(AppLifecycleListener.TOPIC, new LoCServiceLifecycleListener(checkChangesStatusJob));
    }

    protected static class LoCServiceLifecycleListener implements AppLifecycleListener {
        private final ScheduledFuture<?> heartBeatJob;

        LoCServiceLifecycleListener(final ScheduledFuture<?> heartBeatJob) {
            this.heartBeatJob = heartBeatJob;
        }

        @Override
        public void appWillBeClosed(final boolean isRestart) {
            heartBeatJob.cancel(false);
        }
    }

    private static Pair<Integer, String> getGitShowStat(@NotNull final Path path) {
        ProcessBuilder processBuilder1 = new ProcessBuilder();
        processBuilder1.command("bash", "-c", "git log --pretty=format:'%H' -1").directory(path.toFile());
        String headCommit = "";

        try {
            Process process = processBuilder1.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            String l = null;

            while ((line = reader.readLine()) != null) {
                l = line;
                output.append(line + "\n");
            }

            headCommit = l;

            int exitVal = process.waitFor();
            /*if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
                System.exit(0);
            } else {
                //abnormal...
            }*/
            //Messages.showInfoMessage("Commit", output.toString());
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

        // Find out the LOC from this head commit
        System.out.println("headCommit is :: " + headCommit);
        ProcessBuilder processBuilder2 = new ProcessBuilder();
        processBuilder2.command("bash", "-c", "git show --stat").directory(path.toFile());
        String filesChanged = "";
        int loc = 0;

        try {

            Process process = processBuilder2.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            String lastLine = "";
            while ((line = reader.readLine()) != null) {
                lastLine = line;
                System.out.println(lastLine);
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            /*if (exitVal == 0) {
                System.out.println("Success!");
                System.out.println(output);
                System.exit(0);
            } else {
                //abnormal...
            }*/
//                            String[] lastArray = lastLine.split(" ");
//                            if (lastArray.length == 0 || Objects.equals(lastArray[0], "")) {
//                                return;
//                            }
//                            filesChanged = lastArray[1];
//                            int additions = Integer.parseInt(lastArray[4]);
//                            int deletions = 0;
//                            loc = additions + deletions;
            String[] lastArray = lastLine.split(",");
            /*if (lastArray.length == 0 || Objects.equals(lastArray[0], "")) {
                continue;
            }
            */
            String[] firstLine = lastArray[0].split(" ");
            if (firstLine.length == 0) {
                return new Pair<>(0, "0");
            }
            filesChanged = firstLine[1];
            System.out.println("dfasdfasdf:::" + filesChanged);
            int additions = 0;
            int deletions = 0;
            String[] secondLine = lastArray[1].split(" ");
            if (secondLine[1].equals("insertions(+)")) {
                additions = Integer.parseInt(secondLine[1]);
                if (lastArray.length == 3) {
                    String[] thirdLine = lastArray[2].split(" ");
                    deletions = Integer.parseInt(thirdLine[1]);
                }
            } else {
                deletions = Integer.parseInt(secondLine[1]);
            }

            loc = additions + deletions;
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

        return new Pair<>(loc, filesChanged);
    }

    private static Pair<Integer, Integer> getGitDiffNumstat(@NotNull final Path path) {
        final ProcessBuilder processBuilder2 = new ProcessBuilder();
        processBuilder2.command("bash", "-c", "git diff HEAD --numstat").directory(path.toFile());

        int filesChanged = 0;
        int additions = 0;
        int deletions = 0;

        try {
            Process process = processBuilder2.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                final String[] lineSplit = line.split("\t");
                if (lineSplit.length == 0 || Objects.equals(lineSplit[0], "")) {
                    continue;
                }
                filesChanged += 1;
                additions += Integer.parseInt(lineSplit[0]);
                deletions += Integer.parseInt(lineSplit[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Pair<>(additions + deletions, filesChanged);
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

                        final Pair<Integer, Integer> info = LoCService.getGitDiffNumstat(projectDir);
                        final Pair<Integer, String> infoInHeadCommit = LoCService.getGitShowStat(projectDir);

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


