
create sequence arkivpostId_seq start with 1 increment by 1;

create table arkivpost (
    arkivpostId number(19, 0) not null,
    arkivertDato timestamp,
    mottattDato timestamp,
    utgaarDato timestamp,
    tema varchar(255),
    arkivpostType varchar(255),
    dokumentType varchar(255),
    kryssreferanseId number(19, 0),
    kanal varchar(255),
    aktoerId varchar(255),
    fodselsnummer varchar(255),
    navIdent varchar(255),
    innhold varchar(255),
    journalfoerendeEnhet varchar(255),
    status varchar(255),
    kategorikode varchar(255),
    signert number(1, 0),
    erOrganInternt number(1, 0),
    begrensetPartInnsyn number(1, 0),
    sensitiv number(1, 0),
    constraint arkivpost_pk primary key (arkivpostId)
);

create table vedlegg (
    arkivpostId number(19, 0) not null,
    filnavn varchar(255),
    filtype varchar(255),
    variantformat varchar(255),
    tittel varchar(255),
    brevkode varchar(255),
    strukturert number(1, 0),
    dokument blob,
    constraint vedlegg_arkivpost_fk
        foreign key (arkivpostId)
        references arkivpost(arkivpostId)
);