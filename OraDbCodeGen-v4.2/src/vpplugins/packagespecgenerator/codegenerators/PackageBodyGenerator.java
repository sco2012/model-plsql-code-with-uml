package vpplugins.packagespecgenerator.codegenerators;

import vpplugins.packagespecgenerator.adapters.OracleDbPackageAdapter;
import vpplugins.packagespecgenerator.model.enums.AccessModifier;
import vpplugins.packagespecgenerator.model.enums.ModelStereotypes;
import vpplugins.packagespecgenerator.model.objects.OracleDbFunction;
import vpplugins.packagespecgenerator.model.objects.OracleDbPackage;
import vpplugins.packagespecgenerator.model.objects.OracleDbProcedure;
import vpplugins.packagespecgenerator.model.objects.OracleDbSubProgram;
import vpplugins.packagespecgenerator.model.objects.OracleDbVariable;
import vpplugins.packagespecgenerator.util.CodeFormattingUtils;
import java.util.Iterator;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class PackageBodyGenerator extends CommonCodeGenerator {
   static final Logger log = Logger.getLogger(PackageBodyGenerator.class);

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public String getCode(OracleDbPackageAdapter adapter) {
      OracleDbPackage oracleDbPackage = adapter.getOracleDbPackage();
      
      // The modeled Class may have a stereoType of OracleDbPackage), which signifies that
      // the generated code should be wrapped in a database package contruct.  If not, the generated code will produce standalone
      // procs and fns.
      Boolean isDbPackage = (oracleDbPackage.getSpecialStereoTypeName() == ModelStereotypes.ORACLE_DB_PACKAGE.getStereotype());
      boolean includeInitBuffer = false;
      
      StringBuffer codeBuffer = new StringBuffer();
      StringBuffer codeHeaderBuffer = new StringBuffer();
      StringBuffer codeInitBuffer = new StringBuffer();

      if (isDbPackage) {
         
         codeHeaderBuffer.append("create package body " + oracleDbPackage.getFullyQualifiedName());
         codeHeaderBuffer.append("\nas");

         codeInitBuffer.append("\n  /*\n");
         codeInitBuffer.append("\n      Certain views, synonyms and privileges need to be in place to fully implement the \"data-interface\".  Before calling this ");
         codeInitBuffer.append("\n      data-interface for the first time, or if the structure of any data returned by any \"ref-cursor\" function changed, the dependant ");
         codeInitBuffer.append("\n      objects must be created / recreated.  The init() proc does the necessary initialization / re-initialization.  Have a privileged ");
         codeInitBuffer.append("\n      user or DBA execute this ::");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n          -- With :pConnectUser being the database user that will call the data-interface");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n          -- First try this as a DBA");
         codeInitBuffer.append("\n          begin");
         codeInitBuffer.append("\n            " + oracleDbPackage.getFullyQualifiedName() + ".init('Create', 'DefaultObjectsAndPrivs' , :pConnectUser); --:pConnectUser is user that will call the data-interface");
         codeInitBuffer.append("\n          end;");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n          -- If public synonyms are not allowed");
         codeInitBuffer.append("\n          begin");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'Views');");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'PrivateSynonyms' , :pConnectUser); --:pConnectUser is user that will call the data-interface");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'Privs'           , :pConnectUser); --:pConnectUser is user that will call the data-interface");
         codeInitBuffer.append("\n          end;");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n          -- If that still does not work, get the DDL printed through the dbms window and execute it manually");
         codeInitBuffer.append("\n          begin");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'Views'           , null, 'PrintDdl');");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'PublicSynonyms'  , null, 'PrintDdl');");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'PrivateSynonyms' , :pConnectUser, 'PrintDdl');");
         codeInitBuffer.append("\n              " + oracleDbPackage.getFullyQualifiedName() + ".init('Create',  'Privs'           , :pConnectUser, 'PrintDdl');");
         codeInitBuffer.append("\n          end;");
         codeInitBuffer.append("\n          ");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n          -- If the data-interface is to be dropped, to ensure no orphaned dependencies, try");
         codeInitBuffer.append("\n          begin");
         codeInitBuffer.append("\n            " + oracleDbPackage.getFullyQualifiedName() + ".init('Drop', 'DefaultObjectsAndPrivs' , '" + oracleDbPackage.getOwningSchema() + "');");
         codeInitBuffer.append("\n          end;");
         codeInitBuffer.append("\n          ");
         codeInitBuffer.append("\n  */");
         codeInitBuffer.append("\n");
         codeInitBuffer.append("\n  gRowLimit number := 15;");
         codeInitBuffer.append("\n");

         Iterator packageVariables = oracleDbPackage.getPackageVariables().entrySet().iterator();

         while(packageVariables.hasNext()) {
            Entry<OracleDbVariable, AccessModifier> entry = (Entry)packageVariables.next();
            AccessModifier packageVariableAccessModifier = (AccessModifier)entry.getValue();
            if (packageVariableAccessModifier == AccessModifier.PRIVATE) {
               OracleDbVariable packageVariable = (OracleDbVariable)entry.getKey();

               codeBuffer.append("\n  " + packageVariable.getName() + " " + packageVariable.getDataType() + "");
               if (!packageVariable.getDefaultValue().isEmpty()) {
                  codeBuffer.append(" := " + CodeFormattingUtils.wrapLiteralValueByDataType(packageVariable.getDefaultValue(), packageVariable.getDataType()));
               }
               if (!packageVariable.getDescription().isEmpty()) {
                  codeBuffer.append("  -- " + packageVariable.getDescription().replace("\n", "\n  --"));
               }
            }
         }

         // codeBuffer.append(";\n -- XXX");  // xxx:4
         codeInitBuffer.append("\n" + this.generateInitProc(oracleDbPackage, true));
      }
      Iterator subPrograms = oracleDbPackage.getSubPrograms().iterator();

      OracleDbSubProgram subProgram;
      while(subPrograms.hasNext()) {
         subProgram = (OracleDbSubProgram)subPrograms.next();
         if (subProgram instanceof OracleDbFunction && ((OracleDbFunction)subProgram).getReturnRefCursor() != null) {
            includeInitBuffer=true; 
            OracleDbFunction currentFunction = (OracleDbFunction)subProgram;
            codeBuffer.append(super.generateStFunctionCode(currentFunction, false, true));
            codeBuffer.append(super.generateStFunctionCode(currentFunction, true, true));
         }

         if (subProgram instanceof OracleDbProcedure) {
            codeBuffer.append(super.generateProcedureCode((OracleDbProcedure)subProgram, true, !isDbPackage, oracleDbPackage.getOwningSchema()));
         } else if (subProgram instanceof OracleDbFunction && ((OracleDbFunction)subProgram).getReturnRefCursor() != null) {
            codeBuffer.append(super.generateFunctionCode(oracleDbPackage, (OracleDbFunction)subProgram, false, true, true, !isDbPackage, oracleDbPackage.getOwningSchema()));
            if (subProgram.getParameters().size() != 0) {
               codeBuffer.append(super.generateFunctionCode(oracleDbPackage, (OracleDbFunction)subProgram, true, true, true, !isDbPackage, oracleDbPackage.getOwningSchema()));
            }
         } else if (subProgram instanceof OracleDbFunction) {
            codeBuffer.append(super.generateFunctionCode(oracleDbPackage, (OracleDbFunction)subProgram, false, true, false, !isDbPackage, oracleDbPackage.getOwningSchema()));
         }
      }

      for(subPrograms = oracleDbPackage.getSubPrograms().iterator(); subPrograms.hasNext(); subProgram = (OracleDbSubProgram)subPrograms.next()) {
      }

      if (isDbPackage) {
         codeBuffer.append("\n\nend;\n/\n");
      }
      if (includeInitBuffer) {
         codeHeaderBuffer.append(codeInitBuffer);
      }
      return codeHeaderBuffer.toString() + codeBuffer.toString();
   }
}
