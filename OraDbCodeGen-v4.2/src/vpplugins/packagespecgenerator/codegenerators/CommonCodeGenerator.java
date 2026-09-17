package vpplugins.packagespecgenerator.codegenerators;

import vpplugins.packagespecgenerator.model.objects.OracleDbFunction;
import vpplugins.packagespecgenerator.model.objects.OracleDbPackage;
import vpplugins.packagespecgenerator.model.objects.OracleDbProcedure;
import vpplugins.packagespecgenerator.model.objects.OracleDbRefCursor;
import vpplugins.packagespecgenerator.model.objects.OracleDbSubProgram;
import vpplugins.packagespecgenerator.model.objects.OracleDbSubProgramParameter;
import vpplugins.packagespecgenerator.util.CodeFormattingUtils;
import java.util.Iterator;

// import javax.annotation.processing.Generated;

import org.apache.log4j.Logger;

public abstract class CommonCodeGenerator implements CodeGenerator {
   static final Logger log = Logger.getLogger(CommonCodeGenerator.class);

   protected String generateStFunctionCode(OracleDbFunction oracleDbFunction, boolean takesRefCursorParam, boolean includeBody) {
      StringBuffer result = new StringBuffer();
      result.append("\n\n\n  -- " + oracleDbFunction.getName());
      result.append("\n  function");
      result.append("\n    " + oracleDbFunction.getName());
      if (oracleDbFunction.getParameters().size() != 0 || takesRefCursorParam) {
         result.append(" (");
      }

      int i;
      if (takesRefCursorParam) {
         result.append("\n        /*\n");
         if (!includeBody) {
            result.append(this.generateRefCursorParamStDocumentation(oracleDbFunction, 5));
         } else {
            result.append(this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 5));
         }

         result.append("\n        */");
         result.append("\n          pCursor      sys_refcursor");
      } else {
         result.append("\n        /*\n");
         if (!includeBody) {
            result.append(this.generateOtherParamsStDocumentation(oracleDbFunction, 5));
         } else {
            result.append(this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 5));
         }

         result.append("\n        */");

         for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
            result.append(this.generateSubProgramParameterString(oracleDbFunction, i, !takesRefCursorParam, includeBody));
         }
      }

      if (oracleDbFunction.getParameters().size() != 0 || takesRefCursorParam) {
         result.append("\n    )");
      }

      result.append("\n      return");
      result.append("\n        " + oracleDbFunction.getName() + "Tab pipelined");
      if (includeBody) {
         if (takesRefCursorParam) {
            result.append("\n   is");
            result.append("\n     vRow " + oracleDbFunction.getName() + "Rec" + ";");
            result.append("\n  begin");
            result.append("\n    loop");
            result.append("\n        fetch");
            result.append("\n              pCursor");
            result.append("\n        into");
            result.append("\n              vRow");
            result.append("\n        ;");
            result.append("\n        exit when pCursor%notfound;");
            result.append("\n        pipe row(vRow);");
            result.append("\n    end loop;");
            result.append("\n    close pCursor;");
            result.append("\n    return;");
            result.append("\n  end;\n");
         } else {
            result.append("\n   is");
            result.append("\n     vRow " + oracleDbFunction.getName() + "Rec" + ";");
            result.append("\n");
            result.append("\n     vCursor sys_refcursor;");
            result.append("\n");
            result.append("\n  begin");
            result.append("\n\n");
            result.append("\n    vCursor :=");
            result.append("\n          " + oracleDbFunction.getName() + "RefC (");

            for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
               OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
               if (i == 0) {
                  result.append("\n                " + parameter.getName() + " => " + parameter.getName());
               } else {
                  result.append("\n              , " + parameter.getName() + " => " + parameter.getName());
               }
            }

            result.append("\n          );");
            result.append("\n");
            result.append("\n    loop");
            result.append("\n        fetch");
            result.append("\n              vCursor");
            result.append("\n        into");
            result.append("\n              vRow");
            result.append("\n        ;");
            result.append("\n        exit when vCursor%notfound;");
            result.append("\n        pipe row(vRow);");
            result.append("\n    end loop;");
            result.append("\n    close vCursor;");
            result.append("\n    return;");
            result.append("\n  end;");
         }
      } else {
         result.append("\n  ;");
      }

      return result.toString();
   }

   protected String generateProcedureCode(OracleDbProcedure oracleDbProcedure, boolean includeBody, boolean isStandAlone,String owningSchema) {
      
      StringBuffer result = new StringBuffer();
      boolean includeCommentSection = false;

      result.append("\n\n\n  -- " + oracleDbProcedure.getName());
      if (owningSchema != "") {
         owningSchema = owningSchema + ".";
      }
      if (isStandAlone) {
         result.append("\n  create or replace procedure \n");
         if (oracleDbProcedure.getParameters().size() != 0) {
            result.append("    " + owningSchema + oracleDbProcedure.getName() + " (");
         } else {
            result.append("    " + owningSchema + oracleDbProcedure.getName());
         }
      } else {
         result.append("\n  procedure\n");
         if (oracleDbProcedure.getParameters().size() != 0) {
            result.append("    "  + oracleDbProcedure.getName() + " (");
         } else {
            result.append("    "  + oracleDbProcedure.getName());
         }
      }
      // result.append("\n      -- /* method type: procedure */ \n");

      if (oracleDbProcedure.isDeprecated() || oracleDbProcedure.getDescription() != "") {
         includeCommentSection = true;
      }

      if (includeCommentSection) {
         result.append("\n      /*\n");
      }
      if (oracleDbProcedure.isDeprecated()) {
         result.append("        [DEPRECATED]\n\n");
      }

      result.append(CodeFormattingUtils.docWordWrap(oracleDbProcedure.getDescription(), 120, 8)); // xxx:
      if (includeCommentSection) {
         result.append("\n      */");
      }

      for(int i = 0; i < oracleDbProcedure.getParameters().size(); ++i) {
         result.append(this.generateSubProgramParameterString(oracleDbProcedure, i, false, includeBody));
      }

      if (oracleDbProcedure.getParameters().size() != 0) {
         result.append("\n    )");
      }

      if (includeBody) {
         result.append("\n  is");
         result.append("\n  begin");
         result.append("\n    -- TODO: Implementation goes here");

         result.append("\n    null;");
         result.append("\n  end;\n");
      } else {
         result.append("\n  ;");
      }
      if (isStandAlone) result.append("\n  /\n\n");


      return result.toString();
   }

   protected String generateFunctionCode(OracleDbPackage oracleDbPackage, OracleDbFunction oracleDbFunction, boolean isConjunctiveFilterFn, boolean includeBody, boolean returnsRefCursor, boolean isStandAlone,String owningSchema) {
      
      StringBuffer result = new StringBuffer();
      String OracleFunctionName;
      boolean showDeveloperNote = false;
      boolean descripQuotesNeeded = false;
      String detectedFunctionType = "";
      String schemaClause = "";


      // Figure out if developer-notes need to be shown
      // Strongly-type wrapper and pipelined functions are handled by the generateStFunctionCode() method
      // xxx:1
      if (returnsRefCursor) {
         if (isConjunctiveFilterFn) {
            // Conjunctive function
            showDeveloperNote = true;
            detectedFunctionType = "Conjunctive function";
         } else {
            // RefC function
            showDeveloperNote = false;
            detectedFunctionType = "RefC function";
         }
      } else {
         showDeveloperNote = false;
         detectedFunctionType = "Ordinary function";
      }

      // Circumstances under which comment quotes are going to be needed
      if (oracleDbFunction.isDeprecated() && !includeBody) {
         descripQuotesNeeded = true;  
      }
      if (showDeveloperNote) {
         descripQuotesNeeded = true;  
      }
      // if (oracleDbFunction.getDescription() != "" && detectedFunctionType == "Ordinary function" && !includeBody) {
      //    descripQuotesNeeded = true;  
      // }
      if (oracleDbFunction.getDescription() != "" && detectedFunctionType == "RefC function" && !includeBody) {
         descripQuotesNeeded = true;  
      }

      // Append dot notation to schema name
      if (owningSchema != "") {
         owningSchema = owningSchema + ".";
      }

      // Figure out the function's name
      if (returnsRefCursor) {
         if (isConjunctiveFilterFn) {
            OracleFunctionName = oracleDbFunction.getSetterName();
         } else {
            OracleFunctionName = oracleDbFunction.getName() + "RefC";
         }
      } else {
         OracleFunctionName = oracleDbFunction.getName();
      }

      // function header
      if (isStandAlone) {
         result.append("\n\n\n  -- " + OracleFunctionName);
         result.append("\n  create or replace function \n");
         schemaClause = owningSchema;
      } else {
         result.append("\n\n\n  -- " + OracleFunctionName);
         result.append("\n  function\n");
         schemaClause = "";
      }

      if (oracleDbFunction.getParameters().size() != 0) {
         result.append("    " + schemaClause + OracleFunctionName + " (");
      } else {
         result.append("    " + schemaClause + OracleFunctionName);
      }

      // Show user-doc at the beginnning
      if (isStandAlone && oracleDbFunction.getDescription() != "") {
         result.append("\n      /* \n"+CodeFormattingUtils.docWordWrap(oracleDbFunction.getDescription(),120, 8)+"\n      */\n"); 
      } else if (returnsRefCursor && !isConjunctiveFilterFn && oracleDbFunction.getDescription() != "") {
         result.append("\n      /* \n"+CodeFormattingUtils.docWordWrap(oracleDbFunction.getDescription(),120, 8)+"\n      */\n"); 
      } else if (detectedFunctionType == "Ordinary function" ) {
         result.append("\n      /* \n"+CodeFormattingUtils.docWordWrap(oracleDbFunction.getDescription(),120, 8)+"\n      */\n"); 
      }
   
      // For dbugging, show the detected function type
      // result.append("\n      -- /* method type: " + detectedFunctionType + "*/ \n");

      // Show opening code-quote
      if (descripQuotesNeeded) {
         result.append("\n      /* \n");
      }

      // Show deprecated warning
      if (oracleDbFunction.isDeprecated()) {
         result.append("        [DEPRECATED]\n\n");
      }

      // Show generated documentation
      if (!includeBody) { 
         if (oracleDbFunction.getReturnRefCursor() != null && returnsRefCursor) {
            if (isConjunctiveFilterFn) {
               result
                  .append(this.generateConjunctiveFilteringFnDocumentation(oracleDbFunction, 8));
            } else {
               result
                  .append(this.generateRefCursorDocumentation(oracleDbFunction.getReturnRefCursor(), 6))
                  .append(this.generateSampleRefCodeCallDocumentation(oracleDbFunction, 4));
            }
         }
      }

      // Show developer note
      if (showDeveloperNote) {
         result.append(this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 4));
      }

      // Close comment-quote
      if (descripQuotesNeeded) {
         result.append("\n      */");
      }

      // Generate the spec code or the body code

      int i;
      for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
         result.append(this.generateSubProgramParameterString(oracleDbFunction, i, false, includeBody));
      }

      if (oracleDbFunction.getParameters().size() != 0) {
         result.append("\n    )");
      }

      result.append("\n      return");
      if (oracleDbFunction.getReturnRefCursor() != null && !isConjunctiveFilterFn) {
         result.append("\n        sys_refcursor");
      } else if (oracleDbFunction.getReturnRefCursor() != null && isConjunctiveFilterFn) {
         result.append("\n        number");
      } else {
         result.append("\n        " + oracleDbFunction.getReturnDataType() + " " + oracleDbFunction.getReturnTypeModifier());
      }

      if (includeBody) {
         if (!isConjunctiveFilterFn) {
            if (oracleDbFunction.getReturnRefCursor() != null) {
               OracleDbRefCursor refCursor = oracleDbFunction.getReturnRefCursor();
               result.append("\n  is");
               result.append("\n    " + refCursor.getName() + " sys_refcursor;");
               result.append("\n  begin");
               result.append(this.generateRefCursorImplementationStub(refCursor));
               result.append("\n\n    return " + refCursor.getName() + ";");
            } else {
               result.append("\n  is");
               result.append("\n  begin");
               result.append("\n    -- TODO: Implementation goes here");
               result.append("\n    return null;");
            }

            result.append("\n  end;");
            if (isStandAlone) result.append("\n  /\n\n");
         } else {
            result.append("\n  is");
            result.append("\n");
            result.append("\n  begin");

            for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
               OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
               result.append("\n    " + CodeFormattingUtils.rPad(parameter.getName().replaceFirst("p", "g") ,30)+ " := " + parameter.getName() + ";");
            }
            result.append("\n\n    return 1;");
            result.append("\n  end;");
         }
      } else {
         result.append("\n  ;");
      }

      return result.toString();
   }

   protected String generateRecordAndTableTypesForRefCursor(OracleDbRefCursor oracleDbRefCursor, OracleDbFunction oracleDbFunction) {
      StringBuffer result = new StringBuffer();
      result.append("\n  type " + oracleDbFunction.getName() + "Rec is record (");

      for(int i = 0; i < oracleDbRefCursor.getColumns().size(); ++i) {
         OracleDbRefCursor.OracleDbRefCursorColumn currentColumn = (OracleDbRefCursor.OracleDbRefCursorColumn)oracleDbRefCursor.getColumns().get(i);
         result.append(currentColumn.getName() + "  " + currentColumn.getDataType());
         if (i != oracleDbRefCursor.getColumns().size() - 1) {
            result.append(", ");
         }
      }

      result.append(");");
      result.append("\n  type " + oracleDbFunction.getName() + "Tab is table of " + oracleDbFunction.getName() + "Rec;\n");
      return result.toString();
   }

   protected String generateRefCursorDocumentation(OracleDbRefCursor refCursor, int LineTab) {
      String s1="";
      // Prepare list of fields
      @SuppressWarnings("rawtypes")
      Iterator columns = refCursor.getColumns().iterator();
      int ColumnNameWidth = 28;
      int ColumnTypeWidth = 12;
      int ColumnDescriptionWidth = 32;
      while(columns.hasNext()) {
         OracleDbRefCursor.OracleDbRefCursorColumn column = (OracleDbRefCursor.OracleDbRefCursorColumn)columns.next();
         String ColumnName = column.getName().toUpperCase();
         String ColumnType = CodeFormattingUtils.covertDbDataTypeToJavaDataType(column.getDataType());
         String ColumnDescription = column.getDescription();
         s1=s1+"\n" + CodeFormattingUtils.rPad(ColumnName,ColumnNameWidth) + "  " +  CodeFormattingUtils.rPad(ColumnType,ColumnTypeWidth) +  "  " + CodeFormattingUtils.rPad(ColumnDescription,ColumnDescriptionWidth);
      }

      // Prepare multirecord term
      String s2="";
      if (refCursor.isReturnsMultipleRows()) {
         s2="\n\n        Returns Multiple Records";
      }

      String s3 = 
         "Returns a ref-cursor, which when executed returns records in the form ::\n"+
         "\n"+CodeFormattingUtils.rPad("COLUMN", ColumnNameWidth) + "  " + CodeFormattingUtils.rPad("TYPE", ColumnTypeWidth) + "  " + CodeFormattingUtils.rPad("DESCRIP", ColumnDescriptionWidth)+
         "\n"+CodeFormattingUtils.rPad("----------------------------", ColumnNameWidth)+ "  " + CodeFormattingUtils.rPad("----------------", ColumnTypeWidth) + "  " + CodeFormattingUtils.rPad("--------------------------------", ColumnDescriptionWidth)+
         s1+s2
      ;

      return CodeFormattingUtils.lPad(s3, 8);
   }

   protected String generateSampleRefCodeCallDocumentation(OracleDbFunction oracleDbFunction, int LineTab) {
      // Build list of arguments
      int i;
      String s1 = "";
      for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
         OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
         if (i == 0) {
            s1=s1+"\n            " + parameter.getName() + " => ??";
         } else {
            s1=s1+"\n          , " + parameter.getName() + " => ??";
         }
      }

      String s2 =
         "\n\nFollowing is an example of calling this function in PL/SQL ::\n\n"+
         "declare\n"+
         "  vRow     " + oracleDbFunction.getOwningSchema() + "." + oracleDbFunction.getOwningPackage() + "." + oracleDbFunction.getName() + "Rec\n"+
         "  vCursor sys_refcursor;\n"+
         "begin\n"+
         "  pCursor :=\n"+
         "        " + oracleDbFunction.getName() + "RefC ("+
         s1+"\n"+
         "        );\n"+
         "  loop\n"+
         "      fetch pCursor into vRow;\n"+
         "      exit when pCursor%notfound;\n"+
         "      -- Do useful stuff\n"+
         "  end loop;\n"+
         "  close pCursor;\n"+
         "end;"
         ;
      return CodeFormattingUtils.docWordWrap(s2, 120, 2*LineTab).toString();
   }

   protected String generateConjunctiveFilteringFnDocumentation(OracleDbFunction oracleDbFunction, int LineTab) {


      // Build parameter clause
      int i;
      String s1 = "";
      for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
         OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
         if (i == 0) {
            s1=s1+parameter.getName() + " => ??";
         } else {
            s1=s1+", " + parameter.getName() + " => ??";
         }
      }

      String s2 =
         "Following is an example of calling this function in PL/SQL ::\n"+
         "    select \n"+ 
         "          * \n"+ 
         "    from \n"+ 
         "          " + oracleDbFunction.getName()+"\n"+
         "    where \n"+ 
         "          " + oracleDbFunction.getOwningSchema() + "." + oracleDbFunction.getOwningPackage() + "." + oracleDbFunction.getSetterName() +  
         "( \n            "+s1+"\n          ) = 1\n;\n\n"+
         "Sets filtering criteria (by setting package global variables for the session).  Always returns \"1\".\n\n";
         // this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 0)
         // ;
      return CodeFormattingUtils.docWordWrap(s2, 120, LineTab);
   }

   protected String generateOtherParamsStDocumentation(OracleDbFunction oracleDbFunction, int LineTab) {
      // Build where clause, including a db link
      OracleDbSubProgramParameter parameter;
      String s1 = "";
      int i;
      if (oracleDbFunction.getParameters().size() != 0) {
         s1=s1+ "    where\n";
         s1=s1+"          " + oracleDbFunction.getOwningSchema() + "." + oracleDbFunction.getOwningPackage() + "." + oracleDbFunction.getSetterName() + "@dblink(";
         for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
            parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
            if (i == 0) {
               s1=s1+parameter.getName() + " => ??";
            } else {
               s1=s1+", " + parameter.getName() + " => ??";
            }
         }
         s1=s1+") = 1";
      }

      String s2=
         "Wrapper that allows the results of " + oracleDbFunction.getName() + "RefC() to be fetched using the form ::\n\n"+
         "    select\n"+
         "          *\n"+
         "    from\n"+
         "          " + oracleDbFunction.getName()+ "\n"+
         s1.replace("@dblink", "")+
         "    ;\n\n"+
         "Note the use of the conjunctive function Set" + oracleDbFunction.getName() + "()\n\n"+
         "Allows the function to be executed over a database link ::\n\n"+
         "    select\n"+
         "          *\n"+
         "    from\n"+
         "          " + oracleDbFunction.getName() + "@dblink\n"+
         s1+
         "\n\n"+
         "Sets filtering criteria (by setting package global variables for the session).  Always returns \"1\"\n\n";
         // this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 0)
         // ;
      return CodeFormattingUtils.docWordWrap(s2, 120, 2*LineTab).toString();

   }

   protected String generateRefCursorParamStDocumentation(OracleDbFunction oracleDbFunction, int LineTab) {
      int i;

      // Pre-build a list of arguments
      StringBuffer s1 = new StringBuffer();
      for(i = 0; i < oracleDbFunction.getParameters().size(); ++i) {
         OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbFunction.getParameters().get(i);
         if (i == 0) {
            s1.append(parameter.getName() + " => ??");
         } else {
            s1.append(", " + parameter.getName() + " => ??");
         }
      }

      String s2 = 
         "Strong-type wrapper for " + oracleDbFunction.getName() + "RefC() that allows the form ::\n\n" +
         "    select\n"+
         "          *\n"+
         "    from\n"+
         "          " + oracleDbFunction.getOwningSchema() + "." + oracleDbFunction.getOwningPackage() + "." + oracleDbFunction.getName() + "(\n"+
         "                " + oracleDbFunction.getOwningSchema() + "." + oracleDbFunction.getOwningPackage() + "." + oracleDbFunction.getName() + "RefC(\n"+
         "                      "+s1+"\n"+
         "                )\n"+
         "          )\n"+
         "    ;\n\n"+
         "Because this function returns a pipe-lined function it can not be called over a database link.\n\n";
         // this.generateDeveloperNote(oracleDbFunction, oracleDbFunction.getReturnRefCursor(), 0)
         // ;
      return CodeFormattingUtils.docWordWrap(s2, 120, 2*LineTab).toString();
   }

   protected String generateSubProgramParameterString(OracleDbSubProgram oracleDbSubProgram, int paramIndex, boolean takesRefCursorParam, boolean includeBody) {
      StringBuffer result = new StringBuffer();
      OracleDbSubProgramParameter parameter = (OracleDbSubProgramParameter)oracleDbSubProgram.getParameters().get(paramIndex);
      String parameterDataTypeName = parameter.getDataType();
      if (parameterDataTypeName.indexOf(40) >= 0) {
         parameterDataTypeName = parameterDataTypeName.substring(0, parameterDataTypeName.indexOf(40) - 1);
      }

      String NoCopyModifier = parameter.isNoCopy() ? "nocopy " : "";
      if (paramIndex == 0) {
         result.append("\n          ");
      } else {
         result.append("\n        , ");
      }
      result.append(
            CodeFormattingUtils.rPad(parameter.getName(),32) + 
            CodeFormattingUtils.rPad(parameter.getDirection(),8) + 
            CodeFormattingUtils.rPad(NoCopyModifier,4) + 
            CodeFormattingUtils.rPad(parameterDataTypeName,8)
      );

      if (!takesRefCursorParam || !includeBody) {
         if (takesRefCursorParam && !includeBody) {
            result.append(" := " + parameter.getName().replaceFirst("p", "g"));
         } else if (!parameter.getDefaultValue().isEmpty()) {
            result.append(" := " + parameter.getDefaultValue());
         }
      }

      if (parameter.getDescription() != "") {
         int lPadSpaces = result.length(); 
         if (lPadSpaces > 80) {
            lPadSpaces = 80;
            result
               .append("\n")
               .append(
                  CodeFormattingUtils.docWordWrap(parameter.getDescription(),120 - lPadSpaces -2 , lPadSpaces + 2 + 1,"--")
               );
         } else {
         result
            .append("  ")
            .append(
               CodeFormattingUtils.docWordWrap(parameter.getDescription(),120 - lPadSpaces -2 , lPadSpaces + 2 + 1,"--")
               .replaceFirst("^\\s+", "")
            );
         }
      }
      return result.toString();
   }

   protected String generateDeveloperNote(OracleDbSubProgram oracleDbSubprogram, OracleDbRefCursor oracleDbRefCursor, int LineTab) {
      String s1 =
         "Developer NOTE: This function is generated to work closely with "+oracleDbSubprogram.getName() + "RefC(). "+
         "It is intended that only "+oracleDbSubprogram.getName() + "RefC() should be customized, and this function should not be modified from "+
         "its generated form."
      ;
      return CodeFormattingUtils.docWordWrap(s1,120,2*LineTab);
   }

   protected String generateRefCursorImplementationStub(OracleDbRefCursor refCursor) {
      StringBuffer stub = new StringBuffer();
      
      stub.append("\n    -- TODO: Implementation goes here, replacing the stubbed implementation below.\n");
      stub.append("\n    -- Initial stubbed implementation, generates random data.  Allows a database team to work on an actual");
      stub.append("\n    -- implementation whilst in parallel, those depending on the package can start writing their code.\n");
      stub.append("\n    open");
      stub.append("\n        " + refCursor.getName());
      stub.append("\n    for");
      stub.append("\n        select");

      int counter;
      for(counter = 0; counter < refCursor.getColumns().size(); ++counter) {
         OracleDbRefCursor.OracleDbRefCursorColumn currentColumn = (OracleDbRefCursor.OracleDbRefCursorColumn)refCursor.getColumns().get(counter);
         String columnName = currentColumn.getName();
         String columnType = currentColumn.getDataType();
         String columnPrecision = "";
         if (columnType.indexOf("(") != -1) {
            columnPrecision = columnType.substring(columnType.indexOf("(") + 1, columnType.indexOf(")"));
            columnType = currentColumn.getDataType().substring(0, currentColumn.getDataType().indexOf("("));
         }

         if (counter == 0) {
            stub.append("\n              " + this.generateRandomizedSelectColumns(columnName, columnType, columnPrecision));
         } else {
            stub.append("\n            , " + this.generateRandomizedSelectColumns(columnName, columnType, columnPrecision));
         }
      }

      if (counter == 0) {
         stub.append("\n              *");
      }

      stub.append("\n        from");
      stub.append("\n              dual");
      stub.append("\n        connect by");
      stub.append("\n              level <= gRowLimit");
      stub.append("\n        ;");
      return stub.toString();
   }

   protected String generateRandomizedSelectColumns(String pColumnName, String pColumnType, String pPrecision) {
      String columnType = CodeFormattingUtils.covertDbDataTypeToJavaDataType(pColumnType).replace("(", "").replace(")", "");
      String randomizedColumn = "";
      int length = 0;
      int decimal = 0;
      if (pPrecision.indexOf(44) != -1) {
         length = Integer.parseInt(pPrecision.substring(0, pPrecision.indexOf(44)));
         decimal = Integer.parseInt(pPrecision.substring(pPrecision.indexOf(44) + 1));
      } else if (pPrecision != null || pPrecision != "") {
         try {
            length = Integer.parseInt(pPrecision);
         } catch (Exception err) {
         }
      }

      byte columnTypeCode = -1;
      switch(columnType.hashCode()) {
      case -1808118735:
         if (columnType.equals("String")) {
            columnTypeCode = 0;
         }
         break;
      case -672261858:
         if (columnType.equals("Integer")) {
            columnTypeCode = 1;
         }
         break;
      case 2073533:
         if (columnType.equals("Blob")) {
            columnTypeCode = 5;
         }
         break;
      case 2122702:
         if (columnType.equals("Date")) {
            columnTypeCode = 2;
         }
         break;
      case 842475649:
         if (columnType.equals("Timestamp w/ Time Zone")) {
            columnTypeCode = 3;
         }
         break;
      case 2059094262:
         if (columnType.equals("Timestamp")) {
            columnTypeCode = 4;
         }
      }

      switch(columnTypeCode) {
      case 0:
         randomizedColumn = CodeFormattingUtils.rPad("dbms_random.string('U', dbms_random.value(1," + length + "))",100);
         break;
      case 1:
         randomizedColumn = CodeFormattingUtils.rPad("round(dbms_random.value * power(10, " + length + "), " + decimal + ")",100);
         break;
      case 2:
         randomizedColumn = CodeFormattingUtils.rPad("to_date(trunc(dbms_random.value(2455000, 2460000)),'J')",100);
         break;
      case 3:
         randomizedColumn = CodeFormattingUtils.rPad("to_timestamp_tz(to_date(trunc(dbms_random.value(2455000, 2460000)),'J'), 'DD-MON-YYYY HH:MI AM')",100);
         break;
      case 4:
         randomizedColumn = CodeFormattingUtils.rPad("to_timestamp(to_date(trunc(dbms_random.value(2455000, 2460000)),'J'), 'DD-MON-YYYY HH:MI AM')",100);
         break;
      case 5:
         randomizedColumn = CodeFormattingUtils.rPad("to_blob(utl_raw.cast_to_raw(dbms_random.string('U', 1000000)))",100);
         break;
      default:
         randomizedColumn = "'Need Column Type' Error";
      }

      return randomizedColumn;
   }

   protected String generateInitProc(OracleDbPackage oracleDbPackage, boolean isBody) {
      StringBuffer codeBuffer = new StringBuffer();
      int counter = 0;
      boolean hasRefCursor = false;
      @SuppressWarnings("rawtypes")
      Iterator subPrograms = oracleDbPackage.getSubPrograms().iterator();

      OracleDbSubProgram subProgram;
      while(subPrograms.hasNext()) {
         subProgram = (OracleDbSubProgram)subPrograms.next();
         if (subProgram instanceof OracleDbFunction && ((OracleDbFunction)subProgram).getReturnRefCursor() != null) {
            hasRefCursor = true;
         }
      }

      codeBuffer.append("\n  -- init");
      codeBuffer.append("\n  procedure");
      codeBuffer.append("\n    init (");
      codeBuffer.append("\n      /*");
      if (!isBody) {
         codeBuffer.append("\n          Data-interfaces have dependencies on certain views, synonyms and privileges.  This proc executes the DDL to create ");
         codeBuffer.append("\n          those dependencies.  It can also be used to remove them.  The pMode argument (either \"Create\" or \"Drop\") together");
         codeBuffer.append("\n          with the pEntity argument (\"DefaultObjectsAndPrivs\", \"Views\", \"PublicSynonyms\",");
         codeBuffer.append("\n");
         codeBuffer.append("\n          \"PrivateSynonyms\" or \"Privs\") is used to create or drop dependencies");
         codeBuffer.append("\n");
         codeBuffer.append("\n          -- In most instances this proc (executed by a DBA) is sufficient");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'DefaultObjectsAndPrivs' , :pOWNER);");
         codeBuffer.append("\n");
         codeBuffer.append("\n          -- If public synonyms are not allowed");
         codeBuffer.append("\n          -- Where :pOWNER is the database user that owns the data-interface and :pUSER is the user that will execute the data-interface");
         codeBuffer.append("\n");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'Views' , :pOWNER);");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'PrivateSynonyms' , :pUSER);");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'Privs' , :pUSER);");
         codeBuffer.append("\n");
         codeBuffer.append("\n          -- If that still does not work, get all the DDL and execute it manually (use either public or private synonyms, not both)");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'Views' , :pOWNER, 'PrintDdl');");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'PublicSynonyms' , :pOWNER, 'PrintDdl');");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'PrivateSynonyms' , :pUSER, 'PrintDdl');");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'Privs' , :pUSER ,'PrintDdl');");
         codeBuffer.append("\n");
         codeBuffer.append("\n           -- If the data-interface is to be dropped, to ensure no orphaned dependencies, try");
         codeBuffer.append("\n          " + oracleDbPackage.getFullyQualifiedName() + ".init('Drop' , 'DefaultObjectsAndPrivs' , '" + oracleDbPackage.getOwningSchema() + "');");
         codeBuffer.append("\n");
      } else {
         codeBuffer.append("\n        \"OraDbCodeGen\" housekeeping code.");
      }

      codeBuffer.append("\n      */");
      codeBuffer.append("\n          pMode      varchar2              -- {\"Create\", \"Drop\"}");
      codeBuffer.append("\n        , pEntity    varchar2 default null -- {\"DefaultObjectsAndPrivs\", \"Views\", \"PublicSynonyms\", \"PrivateSynonyms\", \"Privs\"}");
      codeBuffer.append("\n        , pUser      varchar2 default null -- When pEntity is {\"Privs\"} then pUser should be set to the database user that will execute the ");
      codeBuffer.append("\n                                           -- data-interface");
      codeBuffer.append("\n                                           -- When pEntity is {\"DefaultObjectsAndPrivs\", \"Views\", \"PrivateSynonyms\"}  then pUser should");
      codeBuffer.append("\n                                           -- be set to the owner of the data-interface");
      codeBuffer.append("\n                                           -- When pEntity is {\"PubicSynonyms\"}  pUser is ignored");
      codeBuffer.append("\n        , pPrintDdl  varchar2 default null -- {\"PrintDdl\", \"\"}");
      codeBuffer.append("\n                                           -- When set to \"PrintDdl\" any DDL that would usually be executed is printed instead");
      codeBuffer.append("\n    )");
      if (isBody) {
         codeBuffer.append("\n  is");
         codeBuffer.append("\n");
         codeBuffer.append("\n    vPackage                varchar2(32)  := '" + oracleDbPackage.getName().replace("\"", "") + "';");
         codeBuffer.append("\n    vSchema                 varchar2(32)  := '" + oracleDbPackage.getOwningSchema() + "';");
         codeBuffer.append("\n");
         codeBuffer.append("\n    vSynonymEnd             varchar2(2000);");
         codeBuffer.append("\n    vViewEnd                varchar2(2000);");
         codeBuffer.append("\n");
         codeBuffer.append("\n    vEntity                 varchar2(30);");
         codeBuffer.append("\n    vMode                   varchar2(30);");
         codeBuffer.append("\n    vPrintDdl               boolean;");
         codeBuffer.append("\n    vUser                   varchar2(30)  := pUser;");
         codeBuffer.append("\n");
         codeBuffer.append("\n    NotAValidMode           exception;");
         codeBuffer.append("\n    NotAValidEntity         exception;");
         codeBuffer.append("\n    NotAValidPrintCmd       exception;");
         codeBuffer.append("\n");
         if (hasRefCursor) {
            codeBuffer.append("\n    cursor");
            codeBuffer.append("\n        DataInterfaceFunctions");
            codeBuffer.append("\n    is");
            subPrograms = oracleDbPackage.getSubPrograms().iterator();

            while(subPrograms.hasNext()) {
               subProgram = (OracleDbSubProgram)subPrograms.next();
               if (subProgram instanceof OracleDbFunction && ((OracleDbFunction)subProgram).getReturnRefCursor() != null) {
                  if (counter == 0) {
                     codeBuffer.append("\n        select '" + subProgram.getName() + "' Func from dual");
                  } else {
                     codeBuffer.append("\n        union all");
                     codeBuffer.append("\n        select '" + subProgram.getName() + "' Func from dual");
                  }

                  ++counter;
               }
            }

            codeBuffer.append("  ;\n");
         }

         codeBuffer.append("\n    procedure");
         codeBuffer.append("\n      RunDdl (");
         codeBuffer.append("\n            /*");
         codeBuffer.append("\n               Either Run the DDL, or print it to the DBMS Output");
         codeBuffer.append("\n            */");
         codeBuffer.append("\n            pCode   varchar2              -- The code that to be executed or printed");
         codeBuffer.append("\n          , pPrint  boolean default false -- If set to true, prints the code instead of executing it");
         codeBuffer.append("\n      )");
         codeBuffer.append("\n    is");
         codeBuffer.append("\n    begin");
         codeBuffer.append("\n      if not pPrint then");
         codeBuffer.append("\n        execute immediate(pCode);");
         codeBuffer.append("\n      elsif pPrint then");
         codeBuffer.append("\n        dbms_output.put_line(pCode || ';');");
         codeBuffer.append("\n      end if;");
         codeBuffer.append("\n    exception");
         codeBuffer.append("\n      when others then");
         codeBuffer.append("\n        raise_application_error(-20101, sqlerrm || ' when executing [' || pCode || ']');");
         codeBuffer.append("\n    end;");
         codeBuffer.append("\n");
         codeBuffer.append("\n  begin");
         codeBuffer.append("\n    -- Check if either create or drop has been passed");
         codeBuffer.append("\n    case upper(pMode)");
         codeBuffer.append("\n      when 'CREATE' then");
         codeBuffer.append("\n        vMode := 'create or replace';");
         codeBuffer.append("\n      when 'DROP' then");
         codeBuffer.append("\n        vMode := 'drop';");
         codeBuffer.append("\n      else");
         codeBuffer.append("\n        raise NotAValidMode;");
         codeBuffer.append("\n    end case;");
         codeBuffer.append("\n    case upper(pEntity)");
         codeBuffer.append("\n      when 'DEFAULTOBJECTSANDPRIVS' then");
         codeBuffer.append("\n        vEntity := 'DefaultObjectsAndPrivs';");
         codeBuffer.append("\n      when 'VIEWS' then");
         codeBuffer.append("\n        vEntity := 'Views';");
         codeBuffer.append("\n      when 'PUBLICSYNONYMS' then");
         codeBuffer.append("\n        vEntity := 'PublicSynonyms';");
         codeBuffer.append("\n      when 'PRIVATESYNONYMS' then");
         codeBuffer.append("\n        vEntity := 'PrivateSynonyms';");
         codeBuffer.append("\n      when 'PRIVS' then");
         codeBuffer.append("\n        vEntity := 'Privs';");
         codeBuffer.append("\n      else");
         codeBuffer.append("\n        raise NotAValidEntity;");
         codeBuffer.append("\n    end case;");
         codeBuffer.append("\n    -- Print DDL instead of executing it");
         codeBuffer.append("\n    case upper(pPrintDdl)");
         codeBuffer.append("\n      when 'PRINTDDL' then");
         codeBuffer.append("\n        vPrintDdl := true;");
         codeBuffer.append("\n      when 'DROP' then");
         codeBuffer.append("\n        vPrintDdl := false;");
         codeBuffer.append("\n      else");
         codeBuffer.append("\n        if (pPrintDdl is null) then");
         codeBuffer.append("\n          vPrintDdl := false;");
         codeBuffer.append("\n        else");
         codeBuffer.append("\n          raise NotAValidPrintCmd;");
         codeBuffer.append("\n        end if;");
         codeBuffer.append("\n    end case;");
         codeBuffer.append("\n");
         codeBuffer.append("\n    -- If creating, add the extra information a create or replace statement needs to generate synonyms");
         codeBuffer.append("\n    if vMode = 'create or replace' then");
         codeBuffer.append("\n      vSynonymEnd := ' for ' || vSchema || '.\"' || vPackage || '\"';");
         codeBuffer.append("\n    end if;");
         codeBuffer.append("\n");
         codeBuffer.append("\n    -- Public Synonyms");
         codeBuffer.append("\n    if vEntity = 'PublicSynonyms' or vEntity = 'DefaultObjectsAndPrivs' then");
         codeBuffer.append("\n      RunDdl(vMode || ' public synonym ' || vPackage || vSynonymEnd, vPrintDdl);");
         codeBuffer.append("\n      RunDdl(vMode || ' public synonym \"' || vPackage || '\"' || vSynonymEnd, vPrintDdl);");
         codeBuffer.append("\n    end if;");
         codeBuffer.append("\n");
         codeBuffer.append("\n    -- Private Synonyms");
         codeBuffer.append("\n    if vEntity = 'PrivateSynonyms' and vUser is not null then");
         codeBuffer.append("\n      RunDdl(vMode || ' synonym ' || vUser || '.' || vPackage || vSynonymEnd, vPrintDdl);");
         codeBuffer.append("\n      RunDdl(vMode || ' synonym ' || vUser || '.\"' || vPackage || '\"' || vSynonymEnd, vPrintDdl);");
         codeBuffer.append("\n    end if;");
         codeBuffer.append("\n");
         codeBuffer.append("\n    -- Grant privileges");
         codeBuffer.append("\n    if (vEntity = 'Privs' or vEntity = 'DefaultObjectsAndPrivs') and vUser is not null and vMode = 'create or replace' then");
         codeBuffer.append("\n      RunDdl('grant execute on ' || vSchema || '.\"' || vPackage || '\" to ' || vUser, vPrintDdl);");
         codeBuffer.append("\n    end if;");
         codeBuffer.append("\n");
         if (hasRefCursor) {
            codeBuffer.append("\n    for r in DataInterfaceFunctions loop");
            codeBuffer.append("\n      -- If creating, add the extra information a create or replace statement needs to generate synonyms");
            codeBuffer.append("\n      if vMode = 'create or replace' then");
            codeBuffer.append("\n        vSynonymEnd := ' for ' || vSchema || '.\"' || r.Func || '\"';");
            codeBuffer.append("\n        vViewEnd    := ' as select * from table(' || vSchema || '.\"' || vPackage || '\".' || r.Func || ')';");
            codeBuffer.append("\n      end if;");
            codeBuffer.append("\n      -- Run DDL for Views");
            codeBuffer.append("\n      if vEntity = 'Views' or vEntity = 'DefaultObjectsAndPrivs' then");
            codeBuffer.append("\n        RunDdl(vMode || ' view ' || vSchema || '.\"' || r.Func || '\"' || vViewEnd, vPrintDdl);");
            codeBuffer.append("\n      end if;");
            codeBuffer.append("\n      -- Run DDL for Public Synonyms");
            codeBuffer.append("\n      if vEntity = 'PublicSynonyms' or vEntity = 'DefaultObjectsAndPrivs' then");
            codeBuffer.append("\n        RunDdl(vMode || ' public synonym ' || r.Func || vSynonymEnd, vPrintDdl);");
            codeBuffer.append("\n        RunDdl(vMode || ' public synonym \"' || r.Func || '\"' || vSynonymEnd, vPrintDdl);");
            codeBuffer.append("\n      end if;");
            codeBuffer.append("\n        -- Run DDL for Private Synonyms");
            codeBuffer.append("\n      if vEntity = 'PrivateSynonyms' and vUser is not null then");
            codeBuffer.append("\n        RunDdl(vMode || ' synonym ' || vUser || '.' || r.Func || vSynonymEnd, vPrintDdl);");
            codeBuffer.append("\n        RunDdl(vMode || ' synonym ' || vUser || '.\"' || r.Func || '\"' || vSynonymEnd, vPrintDdl);");
            codeBuffer.append("\n      end if;");
            codeBuffer.append("\n      -- Run DDL for granting privileges");
            codeBuffer.append("\n      if (vEntity = 'Privs' or vEntity = 'DefaultObjectsAndPrivs') and vUser is not null and vMode = 'create or replace' then");
            codeBuffer.append("\n        RunDdl('grant select on ' || vSchema || '.\"' || r.Func || '\" to ' || vUser, vPrintDdl);");
            codeBuffer.append("\n      end if;");
            codeBuffer.append("\n    end loop;");
            codeBuffer.append("\n");
         }

         codeBuffer.append("\n  exception");
         codeBuffer.append("\n    when NotAValidMode then");
         codeBuffer.append("\n      dbms_output.put_line('The only acceptable values for pMode are : {\"Create\", \"Drop\"}');");
         codeBuffer.append("\n    when NotAValidEntity then");
         codeBuffer.append("\n      dbms_output.put_line('The only acceptable values for pEntity are : {\"DefaultObjectsAndPrivs\", \"Views\", \"PublicSynonyms\", \"PrivateSynonyms\", \"Privs\"}');");
         codeBuffer.append("\n    when NotAValidPrintCmd then");
         codeBuffer.append("\n      dbms_output.put_line('The only acceptable values for pPrintDdl are : {\"PrintDdl\"} or leave it null');");
         codeBuffer.append("\n");
         codeBuffer.append("\n  end;");
      } else {
         codeBuffer.append("\n  ;");
      }

      codeBuffer.append("\n");
      return codeBuffer.toString();
   }

}
