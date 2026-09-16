create package body xxschema."xxpub2"
as
  /*

      Certain views, synonyms and privileges need to be in place to fully implement the "data-interface".  Before calling this
      data-interface for the first time, or if the structure of any data returned by any "ref-cursor" function changed, the dependant
      objects must be created / recreated.  The init() proc does the necessary initialization / re-initialization.  Have a privileged
      user or DBA execute this ::

          -- With :pConnectUser being the database user that will call the data-interface

          -- First try this as a DBA
          begin
            xxschema."xxpub2".init('Create', 'DefaultObjectsAndPrivs' , :pConnectUser); --:pConnectUser is user that will call the data-interface
          end;


          -- If public synonyms are not allowed
          begin
              xxschema."xxpub2".init('Create',  'Views');
              xxschema."xxpub2".init('Create',  'PrivateSynonyms' , :pConnectUser); --:pConnectUser is user that will call the data-interface
              xxschema."xxpub2".init('Create',  'Privs'           , :pConnectUser); --:pConnectUser is user that will call the data-interface
          end;


          -- If that still does not work, get the DDL printed through the dbms window and execute it manually
          begin
              xxschema."xxpub2".init('Create',  'Views'           , null, 'PrintDdl');
              xxschema."xxpub2".init('Create',  'PublicSynonyms'  , null, 'PrintDdl');
              xxschema."xxpub2".init('Create',  'PrivateSynonyms' , :pConnectUser, 'PrintDdl');
              xxschema."xxpub2".init('Create',  'Privs'           , :pConnectUser, 'PrintDdl');
          end;


          -- If the data-interface is to be dropped, to ensure no orphaned dependencies, try
          begin
            xxschema."xxpub2".init('Drop', 'DefaultObjectsAndPrivs' , 'xxschema');
          end;

  */

  gRowLimit number := 15;


  -- init
  procedure
    init (
      /*
        "OraDbCodeGen" housekeeping code.
      */
          pMode      varchar2              -- {"Create", "Drop"}
        , pEntity    varchar2 default null -- {"DefaultObjectsAndPrivs", "Views", "PublicSynonyms", "PrivateSynonyms", "Privs"}
        , pUser      varchar2 default null -- When pEntity is {"Privs"} then pUser should be set to the database user that will execute the
                                           -- data-interface
                                           -- When pEntity is {"DefaultObjectsAndPrivs", "Views", "PrivateSynonyms"}  then pUser should
                                           -- be set to the owner of the data-interface
                                           -- When pEntity is {"PubicSynonyms"}  pUser is ignored
        , pPrintDdl  varchar2 default null -- {"PrintDdl", ""}
                                           -- When set to "PrintDdl" any DDL that would usually be executed is printed instead
    )
  is

    vPackage                varchar2(32)  := 'xxpub2';
    vSchema                 varchar2(32)  := 'xxschema';

    vSynonymEnd             varchar2(2000);
    vViewEnd                varchar2(2000);

    vEntity                 varchar2(30);
    vMode                   varchar2(30);
    vPrintDdl               boolean;
    vUser                   varchar2(30)  := pUser;

    NotAValidMode           exception;
    NotAValidEntity         exception;
    NotAValidPrintCmd       exception;

    cursor
        DataInterfaceFunctions
    is
        select 'fn4' Func from dual  ;

    procedure
      RunDdl (
            /*
               Either Run the DDL, or print it to the DBMS Output
            */
            pCode   varchar2              -- The code that to be executed or printed
          , pPrint  boolean default false -- If set to true, prints the code instead of executing it
      )
    is
    begin
      if not pPrint then
        execute immediate(pCode);
      elsif pPrint then
        dbms_output.put_line(pCode || ';');
      end if;
    exception
      when others then
        raise_application_error(-20101, sqlerrm || ' when executing [' || pCode || ']');
    end;

  begin
    -- Check if either create or drop has been passed
    case upper(pMode)
      when 'CREATE' then
        vMode := 'create or replace';
      when 'DROP' then
        vMode := 'drop';
      else
        raise NotAValidMode;
    end case;
    case upper(pEntity)
      when 'DEFAULTOBJECTSANDPRIVS' then
        vEntity := 'DefaultObjectsAndPrivs';
      when 'VIEWS' then
        vEntity := 'Views';
      when 'PUBLICSYNONYMS' then
        vEntity := 'PublicSynonyms';
      when 'PRIVATESYNONYMS' then
        vEntity := 'PrivateSynonyms';
      when 'PRIVS' then
        vEntity := 'Privs';
      else
        raise NotAValidEntity;
    end case;
    -- Print DDL instead of executing it
    case upper(pPrintDdl)
      when 'PRINTDDL' then
        vPrintDdl := true;
      when 'DROP' then
        vPrintDdl := false;
      else
        if (pPrintDdl is null) then
          vPrintDdl := false;
        else
          raise NotAValidPrintCmd;
        end if;
    end case;

    -- If creating, add the extra information a create or replace statement needs to generate synonyms
    if vMode = 'create or replace' then
      vSynonymEnd := ' for ' || vSchema || '."' || vPackage || '"';
    end if;

    -- Public Synonyms
    if vEntity = 'PublicSynonyms' or vEntity = 'DefaultObjectsAndPrivs' then
      RunDdl(vMode || ' public synonym ' || vPackage || vSynonymEnd, vPrintDdl);
      RunDdl(vMode || ' public synonym "' || vPackage || '"' || vSynonymEnd, vPrintDdl);
    end if;

    -- Private Synonyms
    if vEntity = 'PrivateSynonyms' and vUser is not null then
      RunDdl(vMode || ' synonym ' || vUser || '.' || vPackage || vSynonymEnd, vPrintDdl);
      RunDdl(vMode || ' synonym ' || vUser || '."' || vPackage || '"' || vSynonymEnd, vPrintDdl);
    end if;

    -- Grant privileges
    if (vEntity = 'Privs' or vEntity = 'DefaultObjectsAndPrivs') and vUser is not null and vMode = 'create or replace' then
      RunDdl('grant execute on ' || vSchema || '."' || vPackage || '" to ' || vUser, vPrintDdl);
    end if;

    for r in DataInterfaceFunctions loop
      -- If creating, add the extra information a create or replace statement needs to generate synonyms
      if vMode = 'create or replace' then
        vSynonymEnd := ' for ' || vSchema || '."' || r.Func || '"';
        vViewEnd    := ' as select * from table(' || vSchema || '."' || vPackage || '".' || r.Func || ')';
      end if;
      -- Run DDL for Views
      if vEntity = 'Views' or vEntity = 'DefaultObjectsAndPrivs' then
        RunDdl(vMode || ' view ' || vSchema || '."' || r.Func || '"' || vViewEnd, vPrintDdl);
      end if;
      -- Run DDL for Public Synonyms
      if vEntity = 'PublicSynonyms' or vEntity = 'DefaultObjectsAndPrivs' then
        RunDdl(vMode || ' public synonym ' || r.Func || vSynonymEnd, vPrintDdl);
        RunDdl(vMode || ' public synonym "' || r.Func || '"' || vSynonymEnd, vPrintDdl);
      end if;
        -- Run DDL for Private Synonyms
      if vEntity = 'PrivateSynonyms' and vUser is not null then
        RunDdl(vMode || ' synonym ' || vUser || '.' || r.Func || vSynonymEnd, vPrintDdl);
        RunDdl(vMode || ' synonym ' || vUser || '."' || r.Func || '"' || vSynonymEnd, vPrintDdl);
      end if;
      -- Run DDL for granting privileges
      if (vEntity = 'Privs' or vEntity = 'DefaultObjectsAndPrivs') and vUser is not null and vMode = 'create or replace' then
        RunDdl('grant select on ' || vSchema || '."' || r.Func || '" to ' || vUser, vPrintDdl);
      end if;
    end loop;

  exception
    when NotAValidMode then
      dbms_output.put_line('The only acceptable values for pMode are : {"Create", "Drop"}');
    when NotAValidEntity then
      dbms_output.put_line('The only acceptable values for pEntity are : {"DefaultObjectsAndPrivs", "Views", "PublicSynonyms", "PrivateSynonyms", "Privs"}');
    when NotAValidPrintCmd then
      dbms_output.put_line('The only acceptable values for pPrintDdl are : {"PrintDdl"} or leave it null');

  end;



  -- fn4
  function
    fn4 (
        /*
          Developer NOTE: This function is generated to work closely with fn4RefC(). It is intended that only fn4RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          p1                                          number    --Description of parameter p1
        , p2                                          number    --Description of parameter p2
    )
      return
        fn4Tab pipelined
   is
     vRow fn4Rec;

     vCursor sys_refcursor;

  begin


    vCursor :=
          fn4RefC (
                p1 => p1
              , p2 => p2
          );

    loop
        fetch
              vCursor
        into
              vRow
        ;
        exit when vCursor%notfound;
        pipe row(vRow);
    end loop;
    close vCursor;
    return;
  end;


  -- fn4
  function
    fn4 (
        /*
          Developer NOTE: This function is generated to work closely with fn4RefC(). It is intended that only fn4RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          pCursor      sys_refcursor
    )
      return
        fn4Tab pipelined
   is
     vRow fn4Rec;
  begin
    loop
        fetch
              pCursor
        into
              vRow
        ;
        exit when pCursor%notfound;
        pipe row(vRow);
    end loop;
    close pCursor;
    return;
  end;



  -- fn4RefC
  function
    fn4RefC (
      /*
        functional description of fn3
      */

          p1                                          number    --Description of parameter p1
        , p2                                          number    --Description of parameter p2
    )
      return
        sys_refcursor
  is
    fn4_rectype sys_refcursor;
  begin
    -- TODO: Implementation goes here, replacing the stubbed implementation below.

    -- Initial stubbed implementation, generates random data.  Allows a database team to work on an actual
    -- implementation whilst in parallel, those depending on the package can start writing their code.

    open
        fn4_rectype
    for
        select
              round(dbms_random.value * power(10, 0), 0)
            , dbms_random.string('U', dbms_random.value(1,32))
            , dbms_random.string('U', dbms_random.value(1,1024))
        from
              dual
        connect by
              level <= gRowLimit
        ;

    return fn4_rectype;
  end;


  -- Setfn4
  function
    Setfn4 (
      /*
        Developer NOTE: This function is generated to work closely with fn4RefC(). It is intended that only fn4RefC() should be
        customized, and this function should not be modified from its generated form.
      */
          p1                                          number    --Description of parameter p1
        , p2                                          number    --Description of parameter p2
    )
      return
        number
  is

  begin
    g1                             := p1;
    g2                             := p2;

    return 1;
  end;


  -- fn5
  function
    fn5 (
      /*
        functional description of fn3
      */

          p1                                          number    --Description of parameter p1
    )
      return
        M01.v02.fn5_rectype
  is
  begin
    -- TODO: Implementation goes here
    return null;
  end;

end;
/

