package com.chriscarini.jetbrains.locchangecountdetector;

import com.intellij.icons.AllIcons;
import com.intellij.ide.PowerSaveMode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class VCSFileChangeCountWidgetFactory implements StatusBarWidgetFactory {
    private static final String ID = "VCSFileChangeCount";

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Nls
    @Override
    public @NotNull String getDisplayName() {
        return "VCS File Change Count";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(PowerSaveMode.TOPIC, () -> statusBar.updateWidget(getId()));
        }
//        return new VCSFileChangeCountWidgetIcon(project);
        return new VCSFileChangeCountWidgetText();
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    private static class VCSFileChangeCountWidgetText implements StatusBarWidget, StatusBarWidget.TextPresentation {
        @Override
        public @NotNull @NlsContexts.Label String getText() {
            /*
            IJ plugin stuff has factory pattern all over


            this is good example of hot-reload not working - adding a new static class typically doesn't work, but changes to it should be fine after i think..


            naa, this was me killing and restarting the sandbox

            i'll do one more time to show

            so what's nice is you will get exception to show in the debug window of your main ide, so you can debug from there

             */


            return String.format("%s Files Changed; %s Lines Changed - Hi Sulabh!", 123, 456);
        }

        @Override
        public float getAlignment() {
            return 0;
        }

        @Override
        public @NonNls @NotNull String ID() {
            return "NOT NULL.";
        }

        @Override
        public void install(@NotNull StatusBar statusBar) {

            /*

            what's likely is the other overridden methods need some basic impl - can probably just check the other plugin source to see what they do and tweak

            yeah for sure - poke around, i'll be able to actually talk tomorrow so that'll help, but i think you get the idea :)

            yeah, i really like the idea tbh. my only thing that i think of that's like "ehh, maybe not great idea, but still good" is that when i did analysis on LoC and change time, there wasn't really any strong correlation / relation between the two

            like, i was hoping to have *some* sort of number to back it up and say "hey, if you keep your PR under X LoC, you'll have about 60% faster time-to-approval" or something like that.

            ik Ben did some analysis on this as well, and i think you've looked at GH stuff long enough, I'd really love for the 3 of us to sit down and chat more about details on analysis here, because i'm not fullyconvinced the findings are a 'solid' as i'd like (ie, there are no findings, and i find that hard to believe.)

            yup yup, exactly!

            yeah, i mean i think we all know as developers "give me a long PR, it's going to take me longer to review"

            I *did* just have an "AH HA!" moment tho... perhaps we look at it different way

            like, let's not just say "hey, keep your change under X LoC". maybe instead we calculate LoC changed and show something like:

            "you have X LoC currently. On average, it will take about Y hours to get this change reviewed"

            yup!
            that data we have readily avail.... *AND* for LI specific, we could do analysis based on MP too, so diff MPs might be faster/slower... depends on data quantity, of course :)

            but theres tons we could go from there

            yup!

            either way, i think just getting some basics into plugin for PoC would be amazing... like if we could just compute the LoC changed and then have some lame stuff hard-coded "hey you are above 10 LoC, you'll take about 15 min for review" or "hey you are beyond 500 LoC, consider trimming down" w/ some basic visual indicators, i think that'd be killer to demo :)

            sure sure, sgtm! feel free to push remote branch and what not and we can play around w/ this :)

            i'm going to ping ben on the side and see if he has some time to chat about his analysis that he did for LoC to PR time, i want to see what he found, share w/ him what i found, and i'd love your perspective on it too, i didn't go super deep, bu ti'm sure theres some dimensions we could look at that'd show something more useful for IDE plugin project :D

            yeah, that' be awesome! :)

            alright dude, well, play around, I have a shit ton of links in `MOTES.md` file that you might find useful-ish; no guarentee tho :)

            let's defo plan to chat tomorrow too :)

            no problem! :) i'm excited!

            plugin dev is a huge rabbit hole; it's fun :D

            maybe you become so good you can help me deveop other ones too ;) lol

            haha

            alright man, ttyl!


             */

        }

        @Override
        public void dispose() {

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
    private static class VCSFileChangeCountWidgetIcon implements StatusBarWidget, StatusBarWidget.IconPresentation {
        private final Project project;

        public VCSFileChangeCountWidgetIcon(@NotNull Project project) {
            this.project = project;
        }

        @Override
        public @NotNull String ID() {
            return ID;
        }



        @Override
        public void install(@NotNull StatusBar statusBar) {
        }

        @Override
        public @Nullable WidgetPresentation getPresentation() {
            return this;
        }

        @Override
        public @Nullable String getTooltipText() {
            final AtomicInteger fileCount = new AtomicInteger();
            final AtomicInteger lineCount = new AtomicInteger();

            /*
            The code below was my attempt to find the right SDK API to get LoC changes, but i couldn't figure it out.
             */

            ChangeListManager clm = ChangeListManager.getInstance(this.project);
            clm.getAllChanges().parallelStream().forEach(change -> {
                fileCount.getAndIncrement();

                final VirtualFile virtualFile = Objects.requireNonNull(change.getVirtualFile());

//                final PsiElement firstElement = PsiManager.getInstance(project).findFile(virtualFile).getFirstChild();

                final int changedFileLineCount = LineUtil.countLines(PsiManager.getInstance(project).findFile(virtualFile));

                lineCount.getAndAdd(changedFileLineCount);

                final String fileContents = Objects.requireNonNull(PsiManager.getInstance(project).findFile(virtualFile)).getText();
            });

            // you can kinda see what i was going for here tho..
            return String.format("%s Files Changed; %s Lines Changed - Hi Sulabh!", fileCount.get(), lineCount.get());
        }

        @Override
        public @Nullable Consumer<MouseEvent> getClickConsumer() {
            return __ -> PowerSaveMode.setEnabled(!PowerSaveMode.isEnabled());
        }

        @Override
        public @Nullable Icon getIcon() {
            // so get tooltip text and this getIcon - this is what i will show :)


            // essentially, i wanted some feedback that the code i was typing was actually doing something
            /*
            this code below essentially says 'change the icon depending on if 'power save mode' is enabled or not - this is an API i am familiar w/ as one of my other plugins heavily relies on it :)

            i will show you now in the sandbox :)


            so because sandbox is it's own IDE< it also needs to index JDK lol

            there is 'smart mode and dumb mode of IDE - IDE is in dumb mode when it's indexing, so many plugin hooks are disabled at that point / part of lifecycle.


            yup, exactly!

            so the left IDE is the 'actual' ide you are develping in, and the right one is the sandbox

            you can tell because the process is called 'main' (LOL java) and it has the dummy java icon

            yeah

            these two icons are , from AllIcons, which is a IJ provided class of a bunch of icons - there is way to open that file and see what they look like

            yup!

            just simple way for me to show toggle (ie, 'my code works' sanity check), the tooltip would show the LoC, again, basic PoC :)


            no, that's not it, it should be bottom right near the '4 spaces' - trying to figure out why not showing

             */
            return PowerSaveMode.isEnabled() ? AllIcons.General.BalloonInformation : AllIcons.General.BalloonWarning;
        }

        @Override
        public void dispose() {
        }
    }
}
