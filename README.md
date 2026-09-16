
# Managing complex APIs with UML and generating scaffold code from them

This repo demonstrates:

1. **Modeling PL/SQL with UML classes**, from which **specifications** can be generated, and from which **scafolding-code** can be generated, and the perfect transfer of documentation (descriptions) from the model to the code.

1. Parallelization of the development process by using model generated code.

1. Examples of **advanced (and well formed) Oracle PL/SQL** database packages.

## Modeling PL/SQL with UML classes

This repo contains the **html generated from a UML model**.  Github only shows html sources, and does not render the html, so it is best viewed here:

<a  href="https://raw.githack.com/sco2012/model-plsql-code-with-uml/main/html/index.html" target="_BLANK">
  Open link1
</a>
<br/>
or
<br/>

<a  href="https://rawcdn.githack.com/sco2012/model-plsql-code-with-uml/c03baa8fb6acd3a29e9648104a8fecbdf0e61f5a/html/index.html" target="_blank">
  Open link2
</a>

## RefC functions

Note the SQL that is generated into the function `fn4RefC`:
```
        select
              round(dbms_random.value * power(10, 0), 0)
            , dbms_random.string('U', dbms_random.value(1,32))
            , dbms_random.string('U', dbms_random.value(1,1024))
        from
              dual
        connect by
              level <= gRowLimit
```            
This is generated from the model, based on the structure of the refcursor specified in the model.  The SQL illustrated above returns random data, to a set number of rows, set by a constant in the package specification.  It falls to a developer to replace the generated SQL with an actually useful SQL statement.  The random data complies with the structure specified as the output of the refcursor from the model.

*Why go to the effort of generating such an SQL statement, if it is to be replace by the developer?*

To allow the default generated PL/SQL to be useable immediately by upstream code that needs to consume from it, allowing for the parallelization of the development of any upstream client with the coding of the actual SQL statement, thus making for a more efficient development process.

### Getting the structure of the returned data correct

A major source of errors when coding PL/SQL that returns refcursors, is getting the structure of the SQL correct - in order words, getting the order and type of fields exactly as the consuming code expects.  Being able to model this code significantly reduces the error rate of these codes.


## Advanced PL/SQL code

The **PL/SQL code `xxschema."xxpub2"`** (see [spec](plsql/Modeling-PLSQL-code-using-UML-classes/Returning-ref-cursors/xxschema.xxpub2.pks.sql) and [body](plsql/Modeling-PLSQL-code-using-UML-classes/Returning-ref-cursors/xxschema.xxpub2.pkb.sql)) illustrates:

- Well documented code, including samples of how to use each of the functions within the package specification.
- The use of pipeline functions (see `fn4`).
- The use of record types.
- Modularisation: `fn4RefC` returns a refcursor, and `fn4` takes a refcursor as its input.  A Java client (eg. an app) can handle loosely bound data types, such as from `fn4Rec`, but SQL client (eg. a separate database package) requires a tightly bound return type, which is what `fn4` provides, whilst reusing the SQL imbebbed within `fn4Rec`.  Thus, the underlying query does not have to be implemented in two separate pieces of code.

