package vpplugins.packagespecgenerator.ui.builders;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import org.apache.log4j.Logger;

public class OutputWindowBuilder {
   static final Logger log = Logger.getLogger(OutputWindowBuilder.class);
   private static int StandardWidth = 1100;
   private static int StandardHeight = 300;
   private static int maxRows = 40;
   private static Dimension maxTextArea;
   private JTabbedPane TabPane = new JTabbedPane();
   private JPanel SearchContainer = new JPanel(new FlowLayout(0));
   private JSplitPane LayoutContainer = new JSplitPane(0);
   private JTextArea PackageSpecificationDisplayArea = this.BuildDefaultTextArea();
   private JTextArea PackageBodyDisplayArea = this.BuildDefaultTextArea();
   private SearchFunction searcher = new SearchFunction();

   public OutputWindowBuilder() {
      this.TabPane.add(this.wrapTextAreaInScrollPane(this.PackageSpecificationDisplayArea, "Package Specification"));
      this.TabPane.add(this.wrapTextAreaInScrollPane(this.PackageBodyDisplayArea, "Package Body"));
      this.SearchContainer.add(this.searcher.SearchBar);
      this.SearchContainer.add(this.searcher.PreviousSearchResultButton);
      this.SearchContainer.add(this.searcher.NextSearchResultButton);
      this.SearchContainer.add(this.searcher.SearchMessage);
      this.LayoutContainer.setDividerLocation(0.5D);
      this.LayoutContainer.setTopComponent(this.TabPane);
      this.LayoutContainer.setBottomComponent(this.SearchContainer);
      this.searcher.setSelectedArea(this.selectedTabArea());
      this.TabPane.addChangeListener((e) -> {
         this.searcher.setSelectedArea(this.selectedTabArea());
         this.searcher.currentSearchResultIndex = -1;
         this.searcher.selectedResult = 0;
         this.searcher.getMatches();
      });
   }

   public OutputWindowBuilder setPackageSpecification(String packageSpecificationText) {
      this.PackageSpecificationDisplayArea.setText(packageSpecificationText);
      return this;
   }

   public OutputWindowBuilder setPackageBody(String packageBodyText) {
      this.PackageBodyDisplayArea.setText(packageBodyText);
      return this;
   }

   public JSplitPane builder() {
      this.PackageSpecificationDisplayArea.setCaretPosition(0);
      this.PackageBodyDisplayArea.setCaretPosition(0);
      return this.LayoutContainer;
   }

   private JScrollPane wrapTextAreaInScrollPane(JTextArea pTextArea, String pName) {
      JScrollPane vScrollPane = new JScrollPane(pTextArea);
      vScrollPane.setMaximumSize(maxTextArea);
      vScrollPane.setVerticalScrollBarPolicy(22);
      vScrollPane.setName(pName);
      return vScrollPane;
   }

   public JTextArea BuildDefaultTextArea() {
      JTextArea vTextArea = new JTextArea();
      vTextArea.setSize(StandardWidth, StandardHeight);
      vTextArea.setMaximumSize(maxTextArea);
      vTextArea.setEditable(true);
      vTextArea.setWrapStyleWord(true);
      vTextArea.setLineWrap(true);
      vTextArea.setRows(maxRows);
      vTextArea.setFont(new Font("Monospaced", 0, 12));
      return vTextArea;
   }

   public JTextArea selectedTabArea() {
      JTextArea selectedArea = this.PackageSpecificationDisplayArea;
      if (this.TabPane.getSelectedIndex() == 0) {
         selectedArea = this.PackageSpecificationDisplayArea;
      } else if (this.TabPane.getSelectedIndex() == 1) {
         selectedArea = this.PackageBodyDisplayArea;
      }

      return selectedArea;
   }

   static {
      maxTextArea = new Dimension(StandardWidth, StandardHeight);
   }
}
