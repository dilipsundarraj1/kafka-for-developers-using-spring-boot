package com.learnkafka.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LibraryEventRecord(
        Integer libraryEventId,
        LibraryEventType libraryEventType,
        @NotNull
        @Valid
        BookRecord book
) {
}
