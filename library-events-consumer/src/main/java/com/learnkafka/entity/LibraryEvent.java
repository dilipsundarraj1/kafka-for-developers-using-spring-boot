package com.learnkafka.entity;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
public class LibraryEvent {

    @Id
    private Integer libraryEventId;
    @Enumerated(EnumType.STRING)
    private LibraryEventStatusEnum eventStatus;
    @OneToOne(mappedBy = "libraryEvent", cascade = {CascadeType.ALL})
    @ToString.Exclude
    private Book book;
}


