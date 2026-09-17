package vpplugins.packagespecgenerator.model.objects;

import vpplugins.packagespecgenerator.model.enums.AccessModifier;
import java.util.LinkedList;

public abstract class OracleDbSubProgram extends OracleDbEntity {
   protected AccessModifier accessModifier;
   protected boolean isForwardDeclared = false;
   protected boolean isDeprecated = false;
   @SuppressWarnings({ "rawtypes", "unchecked" })
   protected LinkedList<OracleDbSubProgramParameter> parameters = new LinkedList();
   private String owningPackage;

   public AccessModifier getAccessModifier() {
      return this.accessModifier;
   }

   public void setAccessModifier(AccessModifier accessModifier) {
      this.accessModifier = accessModifier;
   }

   public boolean isForwardDeclared() {
      return this.isForwardDeclared;
   }

   public void setForwardDeclared(boolean forwardDeclared) {
      this.isForwardDeclared = forwardDeclared;
   }

   public boolean isDeprecated() {
      return this.isDeprecated;
   }

   public void setDeprecated(boolean deprecated) {
      this.isDeprecated = deprecated;
   }

   public LinkedList<OracleDbSubProgramParameter> getParameters() {
      return this.parameters;
   }

   public void addParameter(OracleDbSubProgramParameter parameter) {
      this.parameters.add(parameter);
   }

   public void addParameters(LinkedList<OracleDbSubProgramParameter> parameters) {
      this.parameters.addAll(parameters);
   }

   public String getOwningPackage() {
      return this.owningPackage == null ? "" : this.owningPackage;
   }

   public void setOwningPackage(String owningSchema) {
      this.owningPackage = owningSchema;
   }
}
