package vpplugins.packagespecgenerator.model.enums;

public enum ModelStereotypes {
   REF_CURSOR("OracleDbRecType"),
   ORACLE_DB_PACKAGE("OracleDbPackage"),
   DEPRECATED("Deprecated");

   private String stereotype;

   private ModelStereotypes(String stereotype) {
      this.stereotype = stereotype;
   }

   public String getStereotype() {
      return this.stereotype;
   }

   public void setStereotype(String stereotype) {
      this.stereotype = stereotype;
   }
}
