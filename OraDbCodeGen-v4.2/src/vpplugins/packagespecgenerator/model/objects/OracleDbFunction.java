package vpplugins.packagespecgenerator.model.objects;

public class OracleDbFunction extends OracleDbSubProgram {
   private String returnDataType;
   private OracleDbRefCursor returnRefCursor;
   private String returnTypeModifier;
   private String returnSetterName;

   public String getReturnDataType() {
      return this.returnDataType == null ? "" : this.returnDataType;
   }

   public void setReturnDataType(String returnDataType) {
      this.returnDataType = returnDataType;
   }

   public OracleDbRefCursor getReturnRefCursor() {
      return this.returnRefCursor;
   }

   public void setReturnRefCursor(OracleDbRefCursor returnRefCursor) {
      this.returnRefCursor = returnRefCursor;
   }

   public void setReturnTypeModifier(String returnTypeModifier) {
      this.returnTypeModifier = returnTypeModifier;
   }

   public String getReturnTypeModifier() {
      return this.returnTypeModifier;
   }

   public void setSetterName(String returnSetterName) {
      this.returnSetterName = returnSetterName;
   }

   public String getSetterName() {
      return this.returnSetterName;
   }
}
