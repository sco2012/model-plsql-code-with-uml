create or replace package xxschema1."xxPub2"
as



  -- pr1
  procedure
    pr1 (
      /*
        Document the pr1 procedure here. This is an ordinary procedure, with no outputs.
      */
          p1                                          varchar2
    )
  ;


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
  ;


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
  ;

end;
/

