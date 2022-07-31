package com.chriscarini.jetbrains.locchangecountdetector.data;

import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The information related to the current state of changes.
 */
public class ChangeInfo {
    private final Integer loc;
    private final Integer files;
    private final Integer locInCommit;
    private final Integer filesInCommit;

    public ChangeInfo(
            @NotNull final Integer loc,
            @NotNull final Integer files,
            @NotNull final Integer locInCommit,
            @NotNull final Integer filesInCommit
    ) {
        this.loc = loc;
        this.files = files;
        this.locInCommit = locInCommit;
        this.filesInCommit = filesInCommit;
    }

    public Integer getLoc() {
        return loc;
    }

    public Integer getFiles() {
        return files;
    }

    public Integer getLocInCommit() {
        return locInCommit;
    }

    public Integer getFilesInCommit() {
        return filesInCommit;
    }

    public static class Builder {
        @Nullable
        private Integer loc;
        @Nullable
        private Integer files;
        @Nullable
        private Integer locInCommit;
        @Nullable
        private Integer filesInCommit;

        public Builder() {
        }

        @NotNull
        public Builder setModifiedStagedChanges(@NotNull final Pair<Integer, Integer> info) {
            this.loc = info.first;
            this.files = info.second;
            return this;
        }

        @NotNull
        public Builder setHeadCommitChanges(@NotNull final Pair<Integer, Integer> info) {
            this.locInCommit = info.first;
            this.filesInCommit = info.second;
            return this;
        }

        @NotNull
        public ChangeInfo build() {
            return new ChangeInfo(
                    Objects.requireNonNullElse(loc, 0),
                    Objects.requireNonNullElse(files, 0),
                    Objects.requireNonNullElse(locInCommit, 0),
                    Objects.requireNonNullElse(filesInCommit, 0)
            );
        }
    }
}