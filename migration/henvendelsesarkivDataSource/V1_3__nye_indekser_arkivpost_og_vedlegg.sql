drop index IDX$$_06FB0001;

create index aktor_dato_index on ARKIVPOST(aktoerId, mottattdato);
create index vedlegg_arkivpostid on VEDLEGG(arkivpostid);