package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

// 1: Point of action: background job?
// 2: Analysis on change size Vs review time.
// 3: Can we show loc and review time while a user is coding?
// 4: Change Path
// 5: MP info
// 6:  it possible to see reviewTime as you're coding? or when does that pop up come up

public class LOCCountWidgetText implements StatusBarWidget, StatusBarWidget.TextPresentation {

    public static final String ID = "LoCCounter";

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public @NotNull @NlsContexts.Label String getText() {
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
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
            double reviewHoursS = 23400/3600;
            double reviewHoursM = 27000/3600;
            double reviewHoursL = 36000/3600;
            double reviewHoursXL = 45000/3600;
            double reviewTime = 0;

            if (loc >100 && loc <=500){
                reviewTime = reviewHoursL;
            }else if (loc > 15 && loc <= 100){
                reviewTime = reviewHoursM;
            }else if(loc >=0 && loc <=15) {
                reviewTime = reviewHoursS;
            } else {
                reviewTime = reviewHoursXL;
            }
            //Messages.showInfoMessage("Lines Counter", "You have " + loc + " LoC currently in " + filesChanged + " files. On average, it will take about " + reviewTime + " business hours to get this change reviewed!!");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return String.format("%s Files Changed; %s Lines Changed!!", filesChanged, loc);
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return null;
    }


    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }
}

