


  -- xxfn1
  create or replace function
    xxfn1 (
      /*
        xxfn1 functional and technical documentation
      */

          p1                              in out      varchar2 := 'A rather long default value in order to assess how wrapping is handled'
                                                                                 --p1 single line description. Actually,
                                                                                 --it may wrap to more than one line
    )
      return
        varchar2
  is
  begin
    -- TODO: Implementation goes here
    return null;
  end;
  /




  -- xxfn2
  create or replace function
    xxfn2 (
      /*
        xxfn1 functional and technical documentation
      */

          p1                              in out      number   := 12  --p1 description. Modified to be a long
                                                                      --description, that will wrap in order to test the
                                                                      --wrapping code of the code generator.

                                                                      --Also, a second paragraph has been added to see
                                                                      --how that formats.
        , p2                                          varchar2  --Parameters can also have long descriptions. When they
                                                                --do, their descriptions should wrap in order to
                                                                --preserve neat code.

                                                                --Parameter descriptions may also need to be multi-line
                                                                --and may need to be multi-paragraphs.

                                                                --In such cases, the paragraphing should be preserved
                                                                --within the generated code
    )
      return
        varchar2
  is
  begin
    -- TODO: Implementation goes here
    return null;
  end;
  /




  -- xxpr2_no_descrip
  create or replace procedure
    xxpr2_no_descrip (
          p1                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;

  /




  -- xxpr2_has_descrip
  create or replace procedure
    xxpr2_has_descrip (
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

  /




  -- xxpr3_no_descrip
  create or replace procedure
    xxpr3_no_descrip (
          p1                              in out      varchar2
        , p2                              in out      number
    )
  is
  begin
    -- TODO: Implementation goes here
    null;
  end;

  /




  -- xxpr3_deprec_no_descrip2
  create or replace procedure
    xxpr3_deprec_no_descrip2 (
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

  /




  -- xxpr3_deprec_has_descrip
  create or replace procedure
    xxpr3_deprec_has_descrip (
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

  /


