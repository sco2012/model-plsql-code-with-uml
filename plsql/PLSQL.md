## PL/SQL generated from models

This folder holds the PL/SQL code generated from the models illustrated here:

<a  href="https://raw.githack.com/sco2012/model-plsql-code-with-uml/main/html/index.html" target="_BLANK">
   link1
</a>
or
<a  href="https://rawcdn.githack.com/sco2012/model-plsql-code-with-uml/9b1ecd5a07eb2f1fbb60b646443db55570ea7deb/html/index.html" target="_blank">
   link2
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
