
create sequence arkivpostId_seq start with 1 increment by 1;

create table arkivpost (
    arkivpostId number(19, 0) not null,
    arkivertDato timestamp,
    mottattDato timestamp,
    utgaarDato timestamp,
    arkivertTema varchar(255),
    type varchar(255),
    referanseId number(19, 0),
    kanal varchar(255),
    aktoerType varchar(255),
    aktoerId varchar(255),
    eksternPartType varchar(255),
    eksternPartId varchar(255),
    beskrivelse varchar(255),
    journalfoerendeEnhet varchar(255),
    status varchar(255),
    signert number(1, 0),
    erOrganInternt number(1, 0),
    constraint arkivpost_pk primary key (arkivpostId)
);

create table vedlegg (
    uuid varchar(255) not null,
    arkivpostId number(19, 0) not null,
    tittel varchar(255),
    dokumentType varchar(255),
    kategoriKode varchar(255),
    brevKode varchar(255),
    begrensetPartInnsyn number(1, 0),
    sensitiv number(1, 0),
    dokument blob,
    constraint vedlegg_pk primary key (uuid),
    constraint vedlegg_arkivpost_fk
        foreign key (arkivpostId)
        references arkivpost(arkivpostId)
);