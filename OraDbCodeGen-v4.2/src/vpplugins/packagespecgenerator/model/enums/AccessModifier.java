package vpplugins.packagespecgenerator.model.enums;

public enum AccessModifier {
   PUBLIC("public"),
   PRIVATE("private");

   private String accessModifier;

   private AccessModifier(String accessModifier) {
      this.accessModifier = accessModifier;
   }

   public String getAccessModifier() {
      return this.accessModifier;
   }

   public void setAccessModifier(String accessModifier) {
      this.accessModifier = accessModifier;
   }
}
