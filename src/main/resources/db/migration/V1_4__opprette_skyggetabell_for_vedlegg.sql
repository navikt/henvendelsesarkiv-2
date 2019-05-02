create table skygge_vedlegg (
    arkivpostId number(19, 0) not null,
    opprettet timestamp,
    dokument blob,
    constraint vedlegg_arkivpost_fk
        foreign key (arkivpostId)
        references arkivpost(arkivpostId)
);
