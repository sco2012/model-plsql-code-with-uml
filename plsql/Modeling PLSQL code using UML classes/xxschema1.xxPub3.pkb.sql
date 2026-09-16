create package body xxschema1."xxPub3"
as
  /*

      Certain views, synonyms and privileges need to be in place to fully implement the "data-interface".  Before calling this
      data-interface for the first time, or if the structure of any data returned by any "ref-cursor" function changed, the dependant
      objects must be created / recreated.  The init() proc does the necessary initialization / re-initialization.  Have a privileged
      user or DBA execute this ::

          -- With :pConnectUser being the database user that will call the data-interface

          -- First try this as a DBA
          begin
            xxschema1."xxPub3".init('Create', 'DefaultObjectsAndPrivs' , :pConnectUser); --:pConnectUser is user that will call the data-interface
          end;


          -- If public synonyms are not allowed
          begin
              xxschema1."xxPub3".init('Create',  'Views');
              xxschema1."xxPub3".init('Create',  'PrivateSynonyms' , :pConnectUser); --:pConnectUser is user that will call the data-interface
              xxschema1."xxPub3".init('Create',  'Privs'           , :pConnectUser); --:pConnectUser is user that will call the data-interface
          end;


          -- If that still does not work, get the DDL printed through the dbms window and execute it manually
          begin
              xxschema1."xxPub3".init('Create',  'Views'           , null, 'PrintDdl');
              xxschema1."xxPub3".init('Create',  'PublicSynonyms'  , null, 'PrintDdl');
              xxschema1."xxPub3".init('Create',  'PrivateSynonyms' , :pConnectUser, 'PrintDdl');
              xxschema1."xxPub3".init('Create',  'Privs'           , :pConnectUser, 'PrintDdl');
          end;


          -- If the data-interface is to be dropped, to ensure no orphaned dependencies, try
          begin
            xxschema1."xxPub3".init('Drop', 'DefaultObjectsAndPrivs' , 'xxschema1');
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

    vPackage                varchar2(32)  := 'xxPub3';
    vSchema                 varchar2(32)  := 'xxschema1';

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
        select 'pv2' Func from dual
        union all
        select 'pv1' Func from dual  ;

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

  v2 varchar2(32)  -- Description of v2
  v1 number  -- Variable description, from the description of the class attribute


  -- pr1
  procedure
    pr1 (
      /*
        Document the pr1 procedure here. This is an ordinary procedure, with no outputs.
      */
          p1                                          varchar2
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- fn1
  function
    fn1 (
      /*
        Document fn1 here. This is an ordinary function, returning a number.
      */

          p1                                          number
        , p2                                          number
    )
      return
        number
  is
  begin
    -- TODO: Implementation goes here
    return null;
  end;


  -- pr2
  procedure
    pr2 (
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pv2
  function
    pv2 (
        /*
          Developer NOTE: This function is generated to work closely with pv2RefC(). It is intended that only pv2RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          p1                                          number
        , p2                                          varchar2
    )
      return
        pv2Tab pipelined
   is
     vRow pv2Rec;

     vCursor sys_refcursor;

  begin


    vCursor :=
          pv2RefC (
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


  -- pv2
  function
    pv2 (
        /*
          Developer NOTE: This function is generated to work closely with pv2RefC(). It is intended that only pv2RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          pCursor      sys_refcursor
    )
      return
        pv2Tab pipelined
   is
     vRow pv2Rec;
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



  -- pv2RefC
  function
    pv2RefC (
          p1                                          number
        , p2                                          varchar2
    )
      return
        sys_refcursor
  is
    pv1Rec sys_refcursor;
  begin
    -- TODO: Implementation goes here, replacing the stubbed implementation below.

    -- Initial stubbed implementation, generates random data.  Allows a database team to work on an actual
    -- implementation whilst in parallel, those depending on the package can start writing their code.

    open
        pv1Rec
    for
        select
              dbms_random.string('U', dbms_random.value(1,32))
            , round(dbms_random.value * power(10, 0), 0)
            , round(dbms_random.value * power(10, 0), 0)
        from
              dual
        connect by
              level <= gRowLimit
        ;

    return pv1Rec;
  end;


  -- Setpv2
  function
    Setpv2 (
      /*
        Developer NOTE: This function is generated to work closely with pv2RefC(). It is intended that only pv2RefC() should be
        customized, and this function should not be modified from its generated form.
      */
          p1                                          number
        , p2                                          varchar2
    )
      return
        number
  is

  begin
    g1                             := p1;
    g2                             := p2;

    return 1;
  end;


  -- pv1
  function
    pv1 (
        /*
          Developer NOTE: This function is generated to work closely with pv1RefC(). It is intended that only pv1RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          p1                                          number
        , p2                                          varchar2
    )
      return
        pv1Tab pipelined
   is
     vRow pv1Rec;

     vCursor sys_refcursor;

  begin


    vCursor :=
          pv1RefC (
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


  -- pv1
  function
    pv1 (
        /*
          Developer NOTE: This function is generated to work closely with pv1RefC(). It is intended that only pv1RefC() should be
          customized, and this function should not be modified from its generated form.
        */
          pCursor      sys_refcursor
    )
      return
        pv1Tab pipelined
   is
     vRow pv1Rec;
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



  -- pv1RefC
  function
    pv1RefC (
      /*
        df1 is special, because:

         1. it is modeled to return a class, and
         2. the class has stereotype 'OracleDbRecType'.

        On account of those two special features, instead of an ordinary procedure being generated into the resulting database
        package, a set of specially forms procedures will included in the database package. Taken together, those procedures
        provide an approximation of a parameterized view. The documentation generated into the resulting package specification
        gives full details.
      */

          p1                                          number
        , p2                                          varchar2
    )
      return
        sys_refcursor
  is
    pv1Rec sys_refcursor;
  begin
    -- TODO: Implementation goes here, replacing the stubbed implementation below.

    -- Initial stubbed implementation, generates random data.  Allows a database team to work on an actual
    -- implementation whilst in parallel, those depending on the package can start writing their code.

    open
        pv1Rec
    for
        select
              dbms_random.string('U', dbms_random.value(1,32))
            , round(dbms_random.value * power(10, 0), 0)
            , round(dbms_random.value * power(10, 0), 0)
        from
              dual
        connect by
              level <= gRowLimit
        ;

    return pv1Rec;
  end;


  -- Setpv1
  function
    Setpv1 (
      /*
        Developer NOTE: This function is generated to work closely with pv1RefC(). It is intended that only pv1RefC() should be
        customized, and this function should not be modified from its generated form.
      */
          p1                                          number
        , p2                                          varchar2
    )
      return
        number
  is

  begin
    g1                             := p1;
    g2                             := p2;

    return 1;
  end;

end;
/

