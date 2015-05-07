Declare
  COUNT_INDEXES INTEGER;
BEGIN
  select count(*)  INTO COUNT_INDEXES
  from USER_INDEXES
  where index_name='IDX$$_06FB0001';

  IF COUNT_INDEXES > 0 THEN
    EXECUTE IMMEDIATE 'Drop index IDX$$_06FB0001';
  END IF;
END;

create index aktor_dato_index on ARKIVPOST(aktoerId, mottattdato);
create index vedlegg_arkivpostid on VEDLEGG(arkivpostid);