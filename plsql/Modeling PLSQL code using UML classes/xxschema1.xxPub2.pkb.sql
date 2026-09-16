create package body xxschema1."xxPub2"
as
  v1 number  -- Variable description, from the description of the class attribute
  v2 varchar2(32)  -- Description of v2


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


  -- fn2_no_descrip
  function
    fn2_no_descrip (
      /*

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


  -- pr2_has_descrip
  procedure
    pr2_has_descrip (
      /*
        pr2 description
      */
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pr3_no_descrip
  procedure
    pr3_no_descrip (
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pr3_deprec_no_descrip2
  procedure
    pr3_deprec_no_descrip2 (
      /*
        [DEPRECATED]


      */
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pr3_deprec_has_descrip
  procedure
    pr3_deprec_has_descrip (
      /*
        [DEPRECATED]

        This proc is deprecated, and also has a description that must show up in the code comments.
      */
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pr4_had_had_descrip_now_empty
  procedure
    pr4_had_had_descrip_now_empty (
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;



  -- pr4_space_only_descrip
  procedure
    pr4_space_only_descrip (
      /*
        .
      */
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;


end;
/
