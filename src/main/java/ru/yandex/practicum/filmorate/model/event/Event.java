package ru.yandex.practicum.filmorate.model.event;

import lombok.*;

@AllArgsConstructor
@Builder
@Data
public class Event {

    long eventId;
    @NonNull
    final long userId;
    @NonNull
    final long entityId;
    @NonNull
    final long timestamp;
    @NonNull
    final String eventType;
    @NonNull
    final String operation;
}

