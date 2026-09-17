package vpplugins.packagespecgenerator.model.objects;

import vpplugins.packagespecgenerator.model.enums.AccessModifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class OracleDbPackage extends OracleDbEntity {
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private Map<OracleDbVariable, AccessModifier> packageVariables = new HashMap();
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private LinkedList<OracleDbSubProgram> subPrograms = new LinkedList();

   public LinkedList<OracleDbSubProgram> getSubPrograms() {
      return this.subPrograms;
   }

   public void addPackageVariables(Map<OracleDbVariable, AccessModifier> packageVariables) {
      this.packageVariables.putAll(packageVariables);
   }

   public void addPackageSubPrograms(LinkedList<OracleDbSubProgram> subPrograms) {
      this.subPrograms.addAll(subPrograms);
   }

   public void addPackageVariable(OracleDbVariable packageVariable, AccessModifier accessModifier) {
      this.packageVariables.put(packageVariable, accessModifier);
   }

   public Map<OracleDbVariable, AccessModifier> getPackageVariables() {
      return this.packageVariables;
   }

   public void addPackageSubProgram(OracleDbSubProgram subProgram) {
      this.subPrograms.add(subProgram);
   }

   public String getFullyQualifiedName() {
      if (this.getOwningSchema() == "") {
         return   "\"" + this.getName() + "\"";
      } else {
         return this.getOwningSchema() + ".\"" + this.getName() + "\"";
      }
   }

   public String getFullyQualifiedStName() {
      if (this.getOwningSchema() == "") {
         return   "\"" + this.getName() + "\"";
      } else {
         return this.getOwningSchema() + ".\"" + this.getName() + "St\"";
      }

   }
}
