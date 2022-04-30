package com.chriscarini.jetbrains.locchangecountdetector.git;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsException;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class GitNumStat {
    private static final @NonNls Logger LOG = Logger.getInstance(GitNumStat.class);
    private final GitCommand myGitCommand;

    public GitNumStat(@NotNull final GitCommand gitCommand) {
        myGitCommand = gitCommand;
    }

    /**
     * Get the `git <command> HEAD --numstat --format=oneline` output for the provided project.
     *
     * @param project The project for which to run the command upon.
     * @return The output from the `git` command
     * @throws VcsException with message from {@link git4idea.commands.GitCommandResult#getErrorOutputAsJoinedString()}
     */
    @NonNls
    @NotNull
    private String getGitCommandOutput(@NotNull final Project project) throws VcsException {
        final String basePath = project.getBasePath();
        if (basePath == null) {
            return "0";
        }
        final GitLineHandler handler = new GitLineHandler(project, new File(basePath), myGitCommand);
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("HEAD");
        //noinspection SpellCheckingInspection
        handler.addParameters("--numstat");
        handler.addParameters("--format=oneline");
        LOG.debug(String.format("Running %s command on %s", handler, project.getName()));
        return Git.getInstance().runCommand(handler).getOutputOrThrow();
    }

    /**
     * Compute the {@link GitNumStat.Info} for the provided project.
     *
     * @param project The project for which to run the command upon.
     * @return The {@link GitNumStat.Info} object representing the additions, deletions, and count of changed files.
     */
    @NotNull
    public GitNumStat.Info compute(@NotNull final Project project) {
        int filesChanged = 0;
        int additions = 0;
        int deletions = 0;

        final String output;

        try {
            output = this.getGitCommandOutput(project);
        } catch (Exception e) {
            return new Info(0, 0, 0);
        }

        final List<String> lines = List.of(output.split("\n"));
        for (final String line : lines) {
            final List<String> lineSplit = List.of(line.split("\t"));
            if (lineSplit.size() == 0 || Objects.equals(lineSplit.get(0), "")) {
                continue;
            }
            filesChanged += 1;
            try {
                additions += Integer.parseInt(lineSplit.get(0));
            } catch (NumberFormatException e) {
                // We are likely trying to parse a git hash, and need to just skip to the next line.
                continue;
            }
            deletions += Integer.parseInt(lineSplit.get(1));
        }

        return new Info(additions, deletions, filesChanged);
    }

    public static class Info {
        private final Integer addedLines;
        private final Integer deletedLines;
        private final Integer filesChanged;

        public Info(@NotNull final Integer addedLines, @NotNull final Integer deletedLines, @NotNull final Integer filesChanged) {
            this.addedLines = addedLines;
            this.deletedLines = deletedLines;
            this.filesChanged = filesChanged;
        }

        public Integer getAddedLines() {
            return addedLines;
        }

        public Integer getDeletedLines() {
            return deletedLines;
        }

        public Integer getFilesChanged() {
            return filesChanged;
        }

        /**
         * Provides a {@link Pair} object, the first being the summation of added and deleted lines, and the second being the number of files changed.
         *
         * @return a {@link Pair} object
         */
        public Pair<Integer, Integer> toPair() {
            return new Pair<>(this.getAddedLines() + this.getDeletedLines(), this.getFilesChanged());
        }
    }
}
