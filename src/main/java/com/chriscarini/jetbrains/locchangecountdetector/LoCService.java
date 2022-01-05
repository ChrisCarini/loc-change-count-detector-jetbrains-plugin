package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
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
    private String files = "no";

    private final Project project;

    private final ScheduledFuture checkConnectionStatusClearJob;

    public static LoCService getInstance(@NotNull final Project project) {
        return project.getService(LoCService.class);
    }

    public LoCService(@NotNull final Project project) {
        this.project = project;
        this.checkConnectionStatusClearJob = JobScheduler.getScheduler()
                .schedule(() -> this.computeLoCInfo(), 10, TimeUnit.SECONDS);
    }

    public void computeLoCInfo() {
        ProgressManager.getInstance()
                .run(new Task.Backgroundable(this.project, "My background thing") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        //Sulabh's logic for git commands
                        Path directory = Paths.get("/Users/subansal/IdeaProjects/loc-change-count-detector-jetbrains-plugin");
                        ProcessBuilder processBuilder1 = new ProcessBuilder();
                        processBuilder1.command("bash", "-c", "git log --pretty=format:'%H' -1").directory(directory.toFile());
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
                        processBuilder2.command("bash", "-c", "git show --stat").directory(directory.toFile());
                        //processBuilder2.command("bash", "-c", "git log --stat").directory(directory.toFile());
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
                            String[] lastArray = lastLine.split(" ");
                            filesChanged = lastArray[1];
                            int additions = Integer.parseInt(lastArray[4]);
                            int deletions = 0;
                            loc = additions + deletions;

                            // Hard code these value by getting them from our GitHub dashboards
                            double reviewHoursS = 24150 / 3600;
                            double reviewHoursM = 24814 / 3600;
                            double reviewHoursL = 39266 / 3600;
                            double reviewHoursXL = 54911 / 3600;
                            double reviewTime = 0;

            /*S	24150.216213596937
            XL	54911.05688375927
            M	24814.412649501228
            L	39266.64985282312*/

            /*
            IF 0 <= [PR Size Count] AND [PR Size Count] <= 15 THEN 'Small'
ELSEIF 15 < [PR Size Count] AND [PR Size Count] <= 100 THEN 'Medium (>15)'
ELSEIF 100 < [PR Size Count] AND [PR Size Count] <= 500 THEN 'Large (>100)'
ELSEIF 500 < [PR Size Count] THEN 'X-Large (>500)'
END
             */
                            if (loc > 100 && loc <= 500) {
                                reviewTime = reviewHoursL;
                            } else if (loc > 15 && loc <= 100) {
                                reviewTime = reviewHoursM;
                            } else if (loc >= 0 && loc <= 15) {
                                reviewTime = reviewHoursS;
                            } else {
                                reviewTime = reviewHoursXL;
                            }
                            //Messages.showInfoMessage("Lines Counter", "You have " + loc + " LoC currently in " + filesChanged + " files. On average, it will take about " + reviewTime + " business hours to get this change reviewed!!");
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        LoCService.getInstance(myProject).setFiles(filesChanged);
                        LoCService.getInstance(myProject).setLoc(loc);
                    }
                });

    }

    @NotNull
    public Integer getChangeCount() {
        return Objects.requireNonNullElse(loc, 0);
    }

    @NotNull
    public String getFileCount() {
        return Objects.requireNonNullElse(files, "no");
    }

    public void setLoc(@NotNull final Integer loc) {
        this.loc = loc;
    }

    public void setFiles(@NotNull final String files) {
        this.files = files;
    }
}

