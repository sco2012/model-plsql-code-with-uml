


  -- fn1
  create or replace function
    fn1 (
      /*
        Functional description of fn1.
      */

          p1                                          number    --Description of parameter p1
        , p2                                          number    --The descriptions of arguments may also get long. The
                                                                --code generator handles this by implementing wrapping,
                                                                --including maintaining indentation.
        , p3                                          varchar2
    )
      return
        number
  is
  begin
    -- TODO: Implementation goes here
    return null;
  end;
  /


